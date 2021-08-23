package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Frog extends JLabel {
    private int width = 55;
    private int height = 55;

    Frog() {
        ImageIcon imageIcon = new ImageIcon("f.png");
        Image image = imageIcon.getImage();
        Image i = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(i);
        setIcon(imageIcon);
        this.setBounds(450, 900, width, height);
    }

    public void jump(int keyCode) {
        int x = getX();
        int y = getY();
        switch (keyCode) {
            case 37 -> {
                x -= (width / 4);
            }
            case 38 -> {
                y -= (width / 4);
            }
            case 39 -> {
                x += (width / 4);
            }
            case 40 -> {
                y += (width / 4);
            }
        }
        if ((x >= 0 && x < (800 - width)) && (y >= 0 && y < (1000 - width))) {
            if (y == 237) {//it halp me for the end of the game
                if ((x > 47 && x < 86) || (x > 216 && x < 242) || (x > 372 && x < 411) || (x > 515 && x < 554) || (x > 671 && x < 710)) {
                    setLocation(x, y);
                }
            } else if (y == 133) {
                setLocation(450, 900);
            } else {
                setLocation(x, y);
            }
        }
    }
}