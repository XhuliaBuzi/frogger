package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Object extends JLabel {
    private final String direction;
    private final int speed = 1;
    private final int diference_ForLogs;
    private int x;

    public Object(String image, int x, int y, int width, int height, String d, int diference_ForLogs1) {
        this.direction = d;
        this.diference_ForLogs = diference_ForLogs1;
        this.setSize(width, height);
        this.setLocation(x, y);
        setIcon(image);
        getSpeed();
    }

    private void setIcon(String iconImage) {
        ImageIcon imageIcon = new ImageIcon(iconImage);
        Image image = imageIcon.getImage();
        Image i = image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(i);
        this.setIcon(imageIcon);
    }

    public int getSpeed() {
        Random random = new Random();
        int vleX = random.nextInt(10);
        if (vleX <= 2) {
            vleX = 2;
        }
        if (direction.equals("R")) {
            x = getX() - (getWidth() - diference_ForLogs) * vleX;
        } else {
            x = getX() + (getWidth() - diference_ForLogs) * vleX;
        }
        return x;
    }

    public int move(int vlera) {
        int y = getY();
        if (direction.equals("R")) {
            x += 1;
            vlera += 2;
            if (x <= (800 + getWidth())) {
                setLocation(x, y);
            } else {
                setLocation(0, y);
                getSpeed();
            }
        } else {
            x -= 1;
            vlera -= 2;
            if (x >= -getWidth()) {
                setLocation(x, y);
            } else {
                setLocation(800, y);
                getSpeed();
            }
        }
        return vlera;
    }
}
