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

            for (Car car : board.getCars()) {
                TimeUnit.MICROSECONDS.sleep(delay);
                car.move();
                if (car.getBounds().intersects(board.getFrog().getBounds())){
                    board.getFrog().restart();
                    System.out.println("game over");
                }
            }
            for (Logs log : board.getLogs()) {
                TimeUnit.MICROSECONDS.sleep(delay);
                log.move();
            }
            if (board.getFrog().getY() < 530 && board.getFrog().getY() > 210) {
                boolean ok=false;
                for (Logs log : board.getLogs()) {
                    if (log.getBounds().intersects(board.getFrog().getBounds())) {
                        ok = true;
                        System.out.println("\n"+"attached"+"\n");
                        board.getFrog().attach(log);
                        board.getFrog().update();
                    }
                    if (ok == false) {
//                    board.getFrog().restart();
                        System.out.print(log.getBounds().intersects(board.getFrog().getBounds()));
                    }
                }
            }

            for (Bus bus : board.getBuses()) {
                TimeUnit.MICROSECONDS.sleep(delay);
                bus.move();
                if (bus.getBounds().intersects(board.getFrog().getBounds())){
                    board.getFrog().restart();
                    System.out.println("game over");
                }
            }

    }}

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

