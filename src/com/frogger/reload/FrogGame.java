package com.frogger.reload;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FrogGame extends JFrame implements KeyListener {

    private final FrogBoard board = new FrogBoard();

    public FrogGame() throws HeadlessException {
        this.setSize(800, 1000);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(board);
        this.addKeyListener(this);
        this.setVisible(true);
    }

    public void start() throws InterruptedException {

        int delay = 100;

        // Infinite Loop
        while (true) {
            // FOR-EACH Loop
            for (ArrayList<FrogCar> cars : board.getCars()) {
                for (FrogCar car : cars) {
                    TimeUnit.MICROSECONDS.sleep(delay);
                    car.move();
                }
            }
            for (FrogLog log : board.getLogs()) {
                TimeUnit.MICROSECONDS.sleep(delay);
                log.move();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        board.getFrog().jump(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println(board.getFrog().getLocation());
    }
}
