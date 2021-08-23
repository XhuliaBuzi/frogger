package com.frogger;
import javax.swing.*;
import java.awt.*;
import java.util.Random;
public class Object extends JLabel {
    private int speed = 1, x, diference_ForLogs;
    private final String direction;
    public Object(String image, int x, int y, int width, int height, String d, int diference_ForLogs1) {
        this.direction = d;
        this.diference_ForLogs = diference_ForLogs1;
        this.setSize (width, height);
        this.setLocation (x, y);
        setIcon (image);
        getSpeed();
    }
    private void setIcon(String iconImage) {
        ImageIcon imageIcon = new ImageIcon (iconImage);
        Image image = imageIcon.getImage ();
        Image i = image.getScaledInstance (getWidth (), getHeight (), Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon (i);
        this.setIcon (imageIcon);
    }
    private void getSpeed() {
        Random random = new Random ();
        int vleX = random.nextInt (10);
        if (vleX == 0) {
            vleX = 1;
        }
        if (direction.equals ("R")) {
            x = getX () - (getWidth () - diference_ForLogs) * vleX;
        } else {
            x = getX () + (getHeight () - diference_ForLogs) * vleX;
        }

    }
    public void move() {
        int y = getY ();
        if (direction.equals ("R")) {
            x += 1;
            if (x <= (800 + getWidth ())) {
                setLocation (x, y);
            } else {
                setLocation (0, y);
                getSpeed();
            }
        } else {
            x -= 1;
            if (x >= -getWidth ()) {
                setLocation (x, y);
            } else {
                setLocation (800, y);
                getSpeed();
            }
        }
    }
}
