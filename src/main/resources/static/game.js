// Frontend for the Java/Spring Boot Frogger backend.
//
// This file is a PURE RENDERER: it holds no game rules. Every position,
// collision, life, and score comes from the server over a WebSocket (see
// GameWebSocketHandler.java / GameEngine.java); this file just converts
// that JSON into a Three.js scene each tick, and sends "move"/"restart"
// messages back for input. The IIFE wrapper avoids leaking any of this
// into the global scope.
(() => {
  const BOARD_WIDTH = 800;
  const BOARD_HEIGHT = 1000;
  const RIVER_ZONE_MAX_Y = 410;
  const WORLD_SCALE = 1 / 40;

  // The server thinks in game-space pixels (0-800 by 0-1000, same as the
  // original board). Three.js wants small numbers for a comfortable scene,
  // so every position from the server is converted with these two
  // functions: game x/y -> world x/z (Three.js "z" is the board's vertical
  // axis here, since the camera looks down at an angle rather than
  // straight down - see the camera setup below).
  const toWorldX = (gameX) => (gameX - BOARD_WIDTH / 2) * WORLD_SCALE;
  const toWorldZ = (gameY) => (gameY - BOARD_HEIGHT / 2) * WORLD_SCALE;

  const canvas = document.getElementById("board");
  const scoreEl = document.getElementById("score");
  const livesEl = document.getElementById("lives");
  const levelEl = document.getElementById("level");
  const rewardToastEl = document.getElementById("rewardToast");
  const connStatusEl = document.getElementById("connStatus");
  const overlayEl = document.getElementById("overlay");
  const overlayMessageEl = document.getElementById("overlayMessage");
  const restartBtn = document.getElementById("restartBtn");
  const viewToggleBtn = document.getElementById("viewToggleBtn");

  // ---------- Three.js scene (rendering only; all game logic lives on the Java backend) ----------
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x0b1220);

  // Gradient sky sphere (light blue near the horizon, deep blue overhead) so
  // the space above the field is a real sky instead of flat empty color -
  // visible from all three camera views (top-down, frog's-eye, behind).
  function makeSky() {
    const skyGeo = new THREE.SphereGeometry(90, 32, 16);
    const skyMat = new THREE.ShaderMaterial({
      uniforms: {
        topColor: { value: new THREE.Color(0x1b3a6b) },
        bottomColor: { value: new THREE.Color(0x9ed4f0) },
        offset: { value: 8 },
        exponent: { value: 0.7 },
      },
      vertexShader: `
        varying vec3 vWorldPosition;
        void main() {
          vec4 worldPosition = modelMatrix * vec4(position, 1.0);
          vWorldPosition = worldPosition.xyz;
          gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
        }
      `,
      fragmentShader: `
        uniform vec3 topColor;
        uniform vec3 bottomColor;
        uniform float offset;
        uniform float exponent;
        varying vec3 vWorldPosition;
        void main() {
          float h = normalize(vWorldPosition + vec3(0.0, offset, 0.0)).y;
          gl_FragColor = vec4(mix(bottomColor, topColor, max(pow(max(h, 0.0), exponent), 0.0)), 1.0);
        }
      `,
      side: THREE.BackSide,
      depthWrite: false,
    });
    return new THREE.Mesh(skyGeo, skyMat);
  }
  scene.add(makeSky());

  // Orthographic camera sized to guarantee the whole board (start bank to
  // goal) is always inside the view, regardless of distance - a perspective
  // camera clipped the near edge (and the frog) at this viewing angle.
  const ASPECT = BOARD_WIDTH / BOARD_HEIGHT;
  const HALF_WIDTH = 10.6;
  const FRUSTUM_HEIGHT = (HALF_WIDTH * 2) / ASPECT;
  const VERT_CENTER = 0.4;
  const camera = new THREE.OrthographicCamera(
    -HALF_WIDTH, HALF_WIDTH,
    VERT_CENTER + FRUSTUM_HEIGHT / 2, VERT_CENTER - FRUSTUM_HEIGHT / 2,
    0.1, 200
  );
  const ELEVATION_DEG = 55;
  const CAMERA_DISTANCE = 30;
  const elevRad = (ELEVATION_DEG * Math.PI) / 180;
  const CAMERA_Y = Math.sin(elevRad) * CAMERA_DISTANCE;
  const CAMERA_BASE_Z = Math.cos(elevRad) * CAMERA_DISTANCE;
  camera.position.set(0, CAMERA_Y, CAMERA_BASE_Z);
  camera.lookAt(0, 0, 0);
  let followZ = 0; // updated to the frog's actual starting Z once the first state arrives
  // Positive offset biases the view toward the goal, so the frog sits lower
  // on screen and more of the road/river ahead is visible (instead of the
  // frog being dead-centered with half the view "wasted" behind it).
  const LOOKAHEAD_OFFSET = 8;

  // First-person view: perspective camera sat at the frog's eye height,
  // looking the way it's currently facing. Toggled with the view button.
  const fpvCamera = new THREE.PerspectiveCamera(75, ASPECT, 0.05, 150);
  let viewMode = "TOP"; // "TOP" | "FPV" | "BEHIND"

  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setSize(BOARD_WIDTH, BOARD_HEIGHT, false);
  renderer.shadowMap.enabled = true;

  scene.add(new THREE.AmbientLight(0xffffff, 0.55));
  const sun = new THREE.DirectionalLight(0xffffff, 0.9);
  sun.position.set(8, 18, 6);
  sun.castShadow = true;
  sun.shadow.mapSize.set(1024, 1024);
  sun.shadow.camera.left = -14;
  sun.shadow.camera.right = 14;
  sun.shadow.camera.top = 16;
  sun.shadow.camera.bottom = -16;
  scene.add(sun);

  function addBand(gameYStart, gameYEnd, color) {
    const zStart = toWorldZ(gameYStart);
    const zEnd = toWorldZ(gameYEnd);
    const depth = Math.abs(zEnd - zStart);
    const geo = new THREE.BoxGeometry(BOARD_WIDTH * WORLD_SCALE, 0.2, depth);
    const mat = new THREE.MeshStandardMaterial({ color, roughness: 0.9 });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(0, -0.1, (zStart + zEnd) / 2);
    mesh.receiveShadow = true;
    scene.add(mesh);
  }
  // A big grass "floor" under everything, well beyond the playable area, so the
  // follow-camera never scrolls past the edge into empty background.
  const floorGeo = new THREE.BoxGeometry(BOARD_WIDTH * WORLD_SCALE + 6, 0.14, 120);
  const floorMesh = new THREE.Mesh(floorGeo, new THREE.MeshStandardMaterial({ color: 0x2c7a45, roughness: 0.95 }));
  floorMesh.position.set(0, -0.15, 0);
  floorMesh.receiveShadow = true;
  scene.add(floorMesh);

  addBand(0, 58, 0x2fae5c);
  addBand(58, RIVER_ZONE_MAX_Y, 0x2a6fdb);
  addBand(RIVER_ZONE_MAX_Y, 480, 0x2f8f46);
  addBand(480, 870, 0x33363d);
  addBand(870, 1000, 0x2f8f46);

  // Arcade-style "home slot" lily pads across the goal strip.
  function addHomeSlots(gameYStart, gameYEnd, count) {
    const z = toWorldZ((gameYStart + gameYEnd) / 2);
    const padGeo = new THREE.CylinderGeometry(0.32, 0.32, 0.05, 16);
    const padMat = new THREE.MeshStandardMaterial({ color: 0x1f6b4a, roughness: 0.7 });
    const faceGeo = new THREE.SphereGeometry(0.14, 10, 8);
    const faceMat = new THREE.MeshStandardMaterial({ color: 0x4fd15b, roughness: 0.6 });
    const eyeGeo = new THREE.SphereGeometry(0.035, 6, 6);
    const eyeMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
    const pupilGeo = new THREE.SphereGeometry(0.018, 6, 6);
    const pupilMat = new THREE.MeshStandardMaterial({ color: 0x101010 });
    const usableWidth = BOARD_WIDTH * WORLD_SCALE * 0.85;
    for (let i = 0; i < count; i++) {
      const x = -usableWidth / 2 + (usableWidth * i) / (count - 1);
      const pad = new THREE.Mesh(padGeo, padMat);
      pad.position.set(x, 0.03, z);
      pad.receiveShadow = true;
      scene.add(pad);
      const face = new THREE.Mesh(faceGeo, faceMat);
      face.scale.set(1, 0.6, 0.85);
      face.position.set(x, 0.13, z);
      scene.add(face);
      [[-0.06, 0.19, z - 0.05], [0.06, 0.19, z - 0.05]].forEach(([ex, ey, ez]) => {
        const eye = new THREE.Mesh(eyeGeo, eyeMat);
        eye.position.set(ex, ey, ez);
        scene.add(eye);
        const pupil = new THREE.Mesh(pupilGeo, pupilMat);
        pupil.position.set(ex, ey, ez - 0.02);
        scene.add(pupil);
      });
    }
  }
  addHomeSlots(10, 48, 5);

  const carColors = [0xd13b3b, 0xe0a53b, 0x3b7fd1, 0xd13b9e];
  const busColor = 0xe0863b;
  const logPoleColors = [0x8a5a2b, 0x9c6a34, 0x7c4e24, 0xa5723d, 0x8f5c2c];

  const carBodyGeo = new THREE.BoxGeometry(1.9, 0.55, 1.2);
  const carCabinGeo = new THREE.BoxGeometry(1.0, 0.4, 1.0);
  const busBodyGeo = new THREE.BoxGeometry(2.6, 0.85, 1.3);
  const busStripeGeo = new THREE.BoxGeometry(2.62, 0.15, 1.32);
  const wheelGeo = new THREE.CylinderGeometry(0.22, 0.22, 0.22, 12);
  const bambooPoleGeo = new THREE.CylinderGeometry(0.14, 0.14, 3.4, 8);
  const bambooCrossbarGeo = new THREE.BoxGeometry(0.09, 0.13, 1.7);
  const frogBodyGeo = new THREE.SphereGeometry(0.5, 16, 12);
  const frogEyeGeo = new THREE.SphereGeometry(0.13, 8, 8);
  const frogPupilGeo = new THREE.SphereGeometry(0.06, 8, 8);
  const backThighGeo = new THREE.BoxGeometry(0.46, 0.14, 0.14);
  const backShinGeo = new THREE.BoxGeometry(0.5, 0.12, 0.12);
  const frontLegGeo = new THREE.BoxGeometry(0.36, 0.11, 0.11);
  const footGeo = new THREE.BoxGeometry(0.22, 0.07, 0.17);

  /** Low-poly car: box body + darker "cabin" box on top + 4 cylinder wheels. */
  function makeCarMesh(colorIndex) {
    const group = new THREE.Group();
    const body = new THREE.Mesh(
      carBodyGeo,
      new THREE.MeshStandardMaterial({ color: carColors[colorIndex % carColors.length], roughness: 0.5, metalness: 0.15 })
    );
    body.position.y = 0.35;
    body.castShadow = true;
    group.add(body);
    const cabin = new THREE.Mesh(carCabinGeo, new THREE.MeshStandardMaterial({ color: 0x1c2430, roughness: 0.3 }));
    cabin.position.set(-0.15, 0.72, 0);
    cabin.castShadow = true;
    group.add(cabin);
    const wheelMat = new THREE.MeshStandardMaterial({ color: 0x111318, roughness: 0.8 });
    [[-0.65, -0.55], [0.65, -0.55], [-0.65, 0.55], [0.65, 0.55]].forEach(([x, z]) => {
      const wheel = new THREE.Mesh(wheelGeo, wheelMat);
      wheel.rotation.x = Math.PI / 2;
      wheel.position.set(x, 0.14, z);
      wheel.castShadow = true;
      group.add(wheel);
    });
    return group;
  }

  /** Same idea as makeCarMesh but bigger, with a light stripe down the middle. */
  function makeBusMesh() {
    const group = new THREE.Group();
    const body = new THREE.Mesh(busBodyGeo, new THREE.MeshStandardMaterial({ color: busColor, roughness: 0.5, metalness: 0.1 }));
    body.position.y = 0.5;
    body.castShadow = true;
    group.add(body);
    const stripe = new THREE.Mesh(busStripeGeo, new THREE.MeshStandardMaterial({ color: 0xf5f0e6 }));
    stripe.position.y = 0.5;
    group.add(stripe);
    const wheelMat = new THREE.MeshStandardMaterial({ color: 0x111318, roughness: 0.8 });
    [[-0.85, -0.6], [0.85, -0.6], [-0.85, 0.6], [0.85, 0.6]].forEach(([x, z]) => {
      const wheel = new THREE.Mesh(wheelGeo, wheelMat);
      wheel.rotation.x = Math.PI / 2;
      wheel.position.set(x, 0.14, z);
      wheel.castShadow = true;
      group.add(wheel);
    });
    return group;
  }

  /**
   * A river "log" is drawn as a bamboo raft: 5 parallel poles spanning the
   * log's full in-game height (so the visual matches the actual hit-box
   * exactly - no ambiguity about whether the frog is "really" on it) plus
   * 2 dark crossbars near the ends, like tied rope lashings.
   */
  function makeLogMesh() {
    const group = new THREE.Group();
    const poleCount = 5;
    const spread = 1.5; // matches the log's real in-game height so the raft fills its own hit-box
    for (let i = 0; i < poleCount; i++) {
      const mat = new THREE.MeshStandardMaterial({ color: logPoleColors[i % logPoleColors.length], roughness: 0.65 });
      const pole = new THREE.Mesh(bambooPoleGeo, mat);
      pole.rotation.z = Math.PI / 2;
      pole.position.set(0, 0.1, -spread / 2 + (spread * i) / (poleCount - 1));
      pole.castShadow = true;
      pole.receiveShadow = true;
      group.add(pole);
    }
    const crossbarMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a, roughness: 0.8 });
    [-1.3, 1.3].forEach((x) => {
      const bar = new THREE.Mesh(bambooCrossbarGeo, crossbarMat);
      bar.position.set(x, 0.17, 0);
      bar.castShadow = true;
      group.add(bar);
    });
    return group;
  }

  // One side of legs (front + back); mirrored via negative X scale for the other side.
  // Returns pivots (for the jump swing) and segments (for the jump length
  // stretch - legs are 30% length at rest, full length mid-jump).
  function makeLegPair(bodyMat) {
    const side = new THREE.Group();

    // Back leg: thigh (hip) + shin (knee), bent outward and back - crouched-frog silhouette.
    const backHip = new THREE.Group();
    backHip.position.set(0.42, 0.1, 0.16);
    backHip.rotation.y = THREE.MathUtils.degToRad(-42);
    const backThighLen = backThighGeo.parameters.width;
    const backThighMesh = new THREE.Mesh(backThighGeo, bodyMat);
    backThighMesh.castShadow = true;
    backHip.add(backThighMesh);

    const backKnee = new THREE.Group();
    backKnee.rotation.y = THREE.MathUtils.degToRad(-52);
    backHip.add(backKnee);
    const backShinLen = backShinGeo.parameters.width;
    const backShinMesh = new THREE.Mesh(backShinGeo, bodyMat);
    backShinMesh.castShadow = true;
    backKnee.add(backShinMesh);

    const backFoot = new THREE.Mesh(footGeo, bodyMat);
    backFoot.castShadow = true;
    backKnee.add(backFoot);

    side.add(backHip);

    // Front leg: single short segment, angled outward and forward.
    const frontHip = new THREE.Group();
    frontHip.position.set(0.36, 0.13, -0.22);
    frontHip.rotation.y = THREE.MathUtils.degToRad(35);
    const frontLegLen = frontLegGeo.parameters.width;
    const frontLegMesh = new THREE.Mesh(frontLegGeo, bodyMat);
    frontLegMesh.castShadow = true;
    frontHip.add(frontLegMesh);

    const frontFoot = new THREE.Mesh(footGeo, bodyMat);
    frontFoot.scale.set(0.75, 1, 0.75);
    frontFoot.castShadow = true;
    frontHip.add(frontFoot);

    side.add(frontHip);

    const segments = [
      { mesh: backThighMesh, baseLen: backThighLen, joint: backKnee },
      { mesh: backShinMesh, baseLen: backShinLen, joint: backFoot },
      { mesh: frontLegMesh, baseLen: frontLegLen, joint: frontFoot },
    ];
    return { group: side, backHip, backKnee, frontHip, segments };
  }

  /**
   * Builds the frog: a flattened-sphere body, 2 eyes, and 4 jointed legs
   * (mirrored left/right from one makeLegPair() call via a -1 X scale).
   * Stores two lookup tables on the returned group's userData so the
   * per-frame animation in animate() can pose it without rebuilding
   * anything: legPivots (hip/knee rotations for the jump swing) and
   * legSegments (leg length, which shrinks to 30% at rest so the legs read
   * as "gathered" when idle - see the request that shaped this design).
   */
  function makeFrogMesh() {
    const group = new THREE.Group();
    const bodyMat = new THREE.MeshStandardMaterial({ color: 0x4fd15b, roughness: 0.6 });
    const legMat = new THREE.MeshStandardMaterial({ color: 0x3fae4a, roughness: 0.65 });

    const body = new THREE.Mesh(frogBodyGeo, bodyMat);
    body.scale.set(0.95, 0.6, 0.85);
    body.position.y = 0.3;
    body.castShadow = true;
    group.add(body);

    const rightLegs = makeLegPair(legMat);
    group.add(rightLegs.group);
    const leftLegs = makeLegPair(legMat);
    leftLegs.group.scale.x = -1;
    group.add(leftLegs.group);

    const eyeMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
    const pupilMat = new THREE.MeshStandardMaterial({ color: 0x101010 });
    [[-0.2, 0.56, -0.28], [0.2, 0.56, -0.28]].forEach(([x, y, z]) => {
      const eye = new THREE.Mesh(frogEyeGeo, eyeMat);
      eye.position.set(x, y, z);
      group.add(eye);
      const pupil = new THREE.Mesh(frogPupilGeo, pupilMat);
      pupil.position.set(x, y, z - 0.1);
      group.add(pupil);
    });

    // Rest (idle, legs gathered) vs. jump (legs kicked out) rotation for each pivot.
    group.userData.legPivots = [rightLegs, leftLegs].flatMap((side) => [
      { pivot: side.backHip, restY: side.backHip.rotation.y, jumpDeltaY: THREE.MathUtils.degToRad(-22) },
      { pivot: side.backKnee, restY: side.backKnee.rotation.y, jumpDeltaY: THREE.MathUtils.degToRad(30) },
      { pivot: side.frontHip, restY: side.frontHip.rotation.y, jumpDeltaY: THREE.MathUtils.degToRad(20) },
    ]);
    // Length of each leg segment: 30% at rest (idle), 100% at the peak of the jump.
    group.userData.legSegments = [...rightLegs.segments, ...leftLegs.segments];

    return group;
  }

  const frogMesh = makeFrogMesh();
  scene.add(frogMesh);

  // Death animation: frog squashes flat and a blood decal appears at the spot
  // it died (detected client-side as a lives decrease between server
  // states), held for a beat, then it pops back to normal at the start tile.
  const bloodMesh = new THREE.Mesh(
    new THREE.CircleGeometry(1.3, 24),
    new THREE.MeshBasicMaterial({ color: 0x8a1620, transparent: true, opacity: 0.85 })
  );
  bloodMesh.rotation.x = -Math.PI / 2;
  bloodMesh.position.y = 0.02;
  bloodMesh.visible = false;
  scene.add(bloodMesh);
  let isDying = false;
  let deathT = 0;
  const DEATH_STEP = 1 / 30; // ~0.5s
  let pendingFrogX = 0;
  let pendingFrogZ = 0;


  // Purely cosmetic client-side animation: a facing turn (left/right profile)
  // and a hop, triggered immediately on keypress for instant feedback.
  const FACING_ROTATION = { FORWARD: 0, BACKWARD: Math.PI, RIGHT: -Math.PI / 2, LEFT: Math.PI / 2 };
  const DIRECTION_TO_FACING = { UP: "FORWARD", DOWN: "BACKWARD", LEFT: "LEFT", RIGHT: "RIGHT" };
  let facing = "FORWARD";
  let jumping = false;
  let jumpT = 0;
  const JUMP_STEP = 1 / 12;
  const JUMP_HEIGHT = 0.35;

  /** Starts the hop animation and updates facing immediately, before the server even confirms the move. */
  function triggerJump(direction) {
    facing = DIRECTION_TO_FACING[direction] || facing;
    jumping = true;
    jumpT = 0;
  }

  // id -> mesh, rebuilt to match whatever the backend reports
  const entityMeshes = new Map();

  /**
   * Keeps entityMeshes in sync with the server's entity list: creates a
   * mesh the first time an id is seen, repositions existing ones, and
   * removes meshes for ids that vanished (this only happens on the one-time
   * density step-up reflow - see GameEngine.spawnAllLanes()).
   */
  function syncEntities(entities) {
    const seenIds = new Set();
    entities.forEach((entity, index) => {
      seenIds.add(entity.id);
      let mesh = entityMeshes.get(entity.id);
      if (!mesh) {
        mesh = entity.type === "LOG" ? makeLogMesh() : entity.type === "BUS" ? makeBusMesh() : makeCarMesh(index);
        scene.add(mesh);
        entityMeshes.set(entity.id, mesh);
      }
      mesh.position.x = toWorldX(entity.x + entity.width / 2);
      mesh.position.z = toWorldZ(entity.y + entity.height / 2);
    });
    // remove meshes for ids no longer present (e.g. after a restart with a different layout)
    for (const [id, mesh] of entityMeshes.entries()) {
      if (!seenIds.has(id)) {
        scene.remove(mesh);
        entityMeshes.delete(id);
      }
    }
  }

  // ---------- WebSocket connection to the Java backend ----------
  let socket;
  let latestState = null;

  /** ws:// on plain http, wss:// on https - same host/port the page was loaded from. */
  function wsUrl() {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    return `${protocol}//${window.location.host}/game`;
  }

  /** Opens the WebSocket and auto-reconnects (after 1.5s) if it drops - e.g. Render's free-tier cold start/sleep. */
  function connect() {
    socket = new WebSocket(wsUrl());

    socket.onopen = () => {
      connStatusEl.textContent = "Connected";
    };
    socket.onclose = () => {
      connStatusEl.textContent = "Disconnected \u2014 retrying\u2026";
      setTimeout(connect, 1500);
    };
    socket.onerror = () => {
      socket.close();
    };
    socket.onmessage = (event) => {
      latestState = JSON.parse(event.data);
      applyState(latestState);
    };
  }

  function sendAction(payload) {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(payload));
    }
  }

  let hasSeenFirstState = false;
  let lastRewardShown = null;
  let previousLives = null;

  /**
   * Called every time a new state snapshot arrives. Note the death
   * animation isn't a server event - the server has no such concept, it
   * just resets the frog's position instantly. Instead this detects a
   * death purely from the symptom (lives went down since the last state)
   * and, if so, freezes the frog's mesh at its last known position for the
   * squash+blood animation (see animate()) instead of snapping it straight
   * to the server's new (already-reset) position; pendingFrogX/Z hold that
   * new position until the animation finishes.
   */
  function applyState(state) {
    scoreEl.textContent = state.score;
    livesEl.textContent = state.lives;
    levelEl.textContent = state.level;

    syncEntities(state.entities);
    const newX = toWorldX(state.frogX + state.frogWidth / 2);
    const newZ = toWorldZ(state.frogY + state.frogHeight / 2);

    if (previousLives !== null && state.lives < previousLives && !isDying) {
      isDying = true;
      deathT = 0;
      bloodMesh.position.set(frogMesh.position.x, 0.02, frogMesh.position.z);
      bloodMesh.scale.set(0.01, 0.01, 0.01);
      bloodMesh.visible = true;
    }
    previousLives = state.lives;
    pendingFrogX = newX;
    pendingFrogZ = newZ;

    if (!isDying) {
      frogMesh.position.x = newX;
      frogMesh.position.z = newZ;
    }
    if (!hasSeenFirstState) {
      followZ = newZ; // avoid an initial glide-in on first connect
      hasSeenFirstState = true;
    }

    if (state.reward && state.reward !== lastRewardShown) {
      rewardToastEl.textContent = state.reward === "LIFE" ? "\ud83c\udf53 +1 Life!" : "+1";
      rewardToastEl.classList.add("show");
    } else if (!state.reward) {
      rewardToastEl.classList.remove("show");
    }
    lastRewardShown = state.reward;

    if (state.status === "GAME_OVER") {
      overlayMessageEl.textContent = `Game Over \u2014 Score: ${state.score}`;
      overlayEl.classList.remove("hidden");
    } else {
      overlayEl.classList.add("hidden");
    }
  }

  /**
   * The render loop (via requestAnimationFrame - runs every browser paint,
   * independent of the server's 50ms tick rate). Each frame:
   *  1. Positions whichever camera is active (TOP/FPV/BEHIND).
   *  2. Either plays the death squash+blood animation, or (normally) the
   *     jump hop + leg swing/stretch + facing rotation - never both at once.
   *  3. Renders the scene and schedules the next frame.
   */
  function animate() {
    // Smoothly scroll the camera to follow the frog's progress up the board,
    // so the far side visibly approaches as the frog advances.
    followZ += (frogMesh.position.z - followZ) * 0.1;
    const viewCenterZ = followZ - LOOKAHEAD_OFFSET;
    if (viewMode === "TOP") {
      camera.position.z = CAMERA_BASE_Z + viewCenterZ;
      camera.lookAt(0, 0, viewCenterZ);
    } else if (viewMode === "FPV") {
      const theta = FACING_ROTATION[facing];
      const forwardX = -Math.sin(theta);
      const forwardZ = -Math.cos(theta);
      fpvCamera.position.set(frogMesh.position.x, 0.55, frogMesh.position.z);
      fpvCamera.lookAt(
        fpvCamera.position.x + forwardX,
        fpvCamera.position.y - 0.05,
        fpvCamera.position.z + forwardZ
      );
    } else {
      // BEHIND: chase camera, elevated and pulled back from the frog, looking
      // well ahead so much more of the upcoming field is visible than FPV.
      const theta = FACING_ROTATION[facing];
      const forwardX = -Math.sin(theta);
      const forwardZ = -Math.cos(theta);
      fpvCamera.position.set(
        frogMesh.position.x - forwardX * 1.8,
        1.1,
        frogMesh.position.z - forwardZ * 1.8
      );
      fpvCamera.lookAt(
        frogMesh.position.x + forwardX * 4,
        0.3,
        frogMesh.position.z + forwardZ * 4
      );
    }

    if (isDying) {
      deathT = Math.min(1, deathT + DEATH_STEP);
      const squash = Math.min(deathT / 0.6, 1);
      frogMesh.scale.set(1 + 0.5 * squash, 1 - 0.85 * squash, 1 + 0.3 * squash);
      frogMesh.position.y = 0;
      const bloodScale = Math.min(deathT / 0.4, 1);
      bloodMesh.scale.set(bloodScale, bloodScale, bloodScale);
      if (deathT >= 1) {
        isDying = false;
        frogMesh.scale.set(1, 1, 1);
        frogMesh.position.x = pendingFrogX;
        frogMesh.position.z = pendingFrogZ;
        bloodMesh.visible = false;
      }
    } else {
      if (jumping) {
        jumpT = Math.min(1, jumpT + JUMP_STEP);
        if (jumpT >= 1) jumping = false;
      }
      const arc = Math.sin(Math.min(jumpT, 1) * Math.PI); // 0 -> 1 -> 0 hop shape
      frogMesh.position.y = arc * JUMP_HEIGHT;
      frogMesh.rotation.y = FACING_ROTATION[facing];
      const lengthFactor = 0.3 + 0.7 * arc; // 30% length at rest, full length mid-jump
      frogMesh.userData.legSegments.forEach(({ mesh, baseLen, joint }) => {
        const len = baseLen * lengthFactor;
        mesh.scale.x = lengthFactor;
        mesh.position.x = len / 2;
        joint.position.x = len;
      });
      frogMesh.userData.legPivots.forEach(({ pivot, restY, jumpDeltaY }) => {
        pivot.rotation.y = restY + jumpDeltaY * arc;
      });
    }

    renderer.render(scene, viewMode === "TOP" ? camera : fpvCamera);
    requestAnimationFrame(animate);
  }

  const KEY_TO_DIRECTION = { ArrowUp: "UP", ArrowDown: "DOWN", ArrowLeft: "LEFT", ArrowRight: "RIGHT" };
  window.addEventListener("keydown", (event) => {
    const direction = KEY_TO_DIRECTION[event.key];
    if (direction && !isDying) {
      event.preventDefault();
      triggerJump(direction);
      sendAction({ action: "move", direction });
    }
  });

  document.querySelectorAll(".dpad-btn").forEach((btn) => {
    const direction = btn.dataset.direction;
    btn.addEventListener(
      "touchstart",
      (event) => {
        event.preventDefault();
        if (isDying) return;
        triggerJump(direction);
        sendAction({ action: "move", direction });
      },
      { passive: false }
    );
    btn.addEventListener("click", () => {
      if (isDying) return;
      triggerJump(direction);
      sendAction({ action: "move", direction });
    });
  });

  restartBtn.addEventListener("click", () => sendAction({ action: "restart" }));
  const VIEW_MODES = ["TOP", "FPV", "BEHIND"];
  const VIEW_LABELS = { TOP: "View: Top-down", FPV: "View: Frog's-eye", BEHIND: "View: Behind" };
  viewToggleBtn.addEventListener("click", () => {
    viewMode = VIEW_MODES[(VIEW_MODES.indexOf(viewMode) + 1) % VIEW_MODES.length];
    viewToggleBtn.textContent = VIEW_LABELS[viewMode];
    frogMesh.visible = viewMode !== "FPV";
  });

  connect();
  requestAnimationFrame(animate);
})();
