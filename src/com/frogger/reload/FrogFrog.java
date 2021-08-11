package com.frogger.reload;

import javax.swing.*;
import java.awt.*;

public class FrogFrog extends JLabel {

    private final int width = 55;
    private final int height = 55;

    public FrogFrog() {

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
                x -= width;
            }
            case 38 -> {
                y -= height;
            }
            case 39 -> {
                x += width;
            }
            case 40 -> {
                y += height;
            }
        }

        if ((x >= 0 && x < (800 - width)) && (y >= 0 && y < (1000 - height))) {
            setLocation(x, y);
        }
    }
}
