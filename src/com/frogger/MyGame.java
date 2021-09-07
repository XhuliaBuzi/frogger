package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;

public class MyGame extends JFrame implements KeyListener {
    private final Board board = new Board();

    public MyGame() throws HeadlessException {
        this.setSize(800, 1000);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(board);
        this.addKeyListener(this);
        this.setVisible(true);
    }

    public void start() throws InterruptedException {
        int delay = 100;
        while (true) {
            for (int i = 0; i < board.car_rows; i++) {
                for (int j = 0; j < board.number_of_cars; j++) {
                    Car car = board.getCars()[i][j];
                    TimeUnit.MICROSECONDS.sleep(delay);
                    car.move(0);
                    if (car.getBounds().intersects(board.getFrog().getBounds())) {
                        board.getFrog().restart();
                    }
                }
            }
            for (int i = 0; i < board.logs_rows; i++) {
                for (int j = 0; j < board.number_of_logs; j++) {
                    TimeUnit.MICROSECONDS.sleep(delay);
                    board.getLogs()[i][j].move(0);
                }
            }
            boolean ok = false;
            for (int i = 0; i < board.logs_rows; i++) {
                for (int j = 0; j < board.number_of_logs; j++) {
                    Logs log = board.getLogs()[i][j];
                    if (log.getBounds().intersects(board.getFrog().getBounds())) {
                        ok = true;
                        board.getFrog().setLocation(log.move(board.getFrog().getX()), board.getFrog().getY());
                        if (board.getFrog().getX() < 0 || board.getFrog().getX() > 800) {
                            board.getFrog().restart();
                        }
                    }
                }
            }
            if (board.getFrog().getY() < 450) {
                if (ok != true) {
                    board.getFrog().restart();
                }
            }
            for (int i = 0; i < board.buses_rows; i++) {
                for (int j = 0; j < board.number_of_buses; j++) {
                    Bus bus = board.getBuses()[i][j];
                    TimeUnit.MICROSECONDS.sleep(delay);
                    bus.move(0);
                    if (bus.getBounds().intersects(board.getFrog().getBounds())) {
                        board.getFrog().restart();
                    }
                }
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
    }
}

