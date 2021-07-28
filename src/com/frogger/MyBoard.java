package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
public class MyBoard extends JFrame implements KeyListener {
    JLabel label;
    MyBoard() {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        this.addKeyListener(this);
        label = new JLabel();
        label.setBounds(0, 0, 50, 50);
        label.setBackground(Color.red);
        label.setOpaque(true);
        this.add(label);
        this.setVisible(true);
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37: {
                label.setLocation(label.getX() - 20, label.getY());
                break;
            }
            case 38: {
                label.setLocation(label.getX(), label.getY() - 20);
                break;
            }
            case 39: {
                label.setLocation(label.getX() + 20, label.getY());
                break;
            }
            case 40: {
                label.setLocation(label.getX(), label.getY() + 20);
                break;
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
}
