package com.frogger.reload;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FrogBoard extends JPanel {

    private final int height = 1000;

    private final FrogFrog frog = new FrogFrog();
    private final ArrayList<ArrayList<FrogCar>> cars = new ArrayList<>();
    private final ArrayList<FrogLog> logs = new ArrayList<>();

    public FrogBoard() {
        this.setSize(800, height);
        this.setBackground(Color.black);
        this.setLayout(null);

        this.add(frog);
        addCars(7);
        addLogs(6);

        this.setVisible(true);
    }

    private void addCars(int totalCars) {
        // FOR Loop
        for (int i = 1; i <= totalCars; i++) {

            ArrayList<FrogCar> extraCars = new ArrayList<>();

            for (int j = 0; j < 3; j++) {
                FrogCar extraCar = new FrogCar((height - 160) - (i * 60));
                extraCars.add(extraCar);
                this.add(extraCar);
            }

            cars.add(extraCars);
        }
    }

    private void addLogs(int totalLogs) {
        // FOR Loop
        for (int i = 1; i <= totalLogs; i++) {
            FrogLog log = new FrogLog((i * 60));
            logs.add(log);
            this.add(log);
        }
    }

    public FrogFrog getFrog() {
        return frog;
    }

    public ArrayList<ArrayList<FrogCar>> getCars() {
        return cars;
    }

    public ArrayList<FrogLog> getLogs() {
        return logs;
    }
}
