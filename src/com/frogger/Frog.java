package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Frog extends JLabel {
    public int life = 3;
    private final int width = 50;
    private final int height = 50;
    private final int x = 450;
    private final int y = 900;

    Frog() {
        ImageIcon imageIcon = new ImageIcon("f.png");
        Image image = imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(image));
        this.setBounds(x, y, width, height);
    }

    public void restart() {
        setLocation(450, 900);
        life -= 1;
        if (life == 0) {
            System.out.println("Game Over");
        }
    }

    public void jump(int keyCode) {
        int x = getX();
        int y = getY();
        switch (keyCode) {
            case 37 -> {
                x -= (width);
            }
            case 38 -> {
                y -= (width);
            }
            case 39 -> {
                x += (width);
            }
            case 40 -> {
                y += (width);
            }
        }
        if ((x >= 0 && x < (800 - width)) && (y >= 0 && y < (1000 - width))) {
            if (y == 0) {//it halp me for the end of the game
                restart();
            } else {
                setLocation(x, y);
            }
        }
    }

}