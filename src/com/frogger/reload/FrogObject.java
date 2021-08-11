package com.frogger.reload;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class FrogObject extends JLabel {

    private final int width = 80;
    private final int height = 60;
    private int speed = 1;
    private String direction = "R";
    private Class objectType;

    public FrogObject(int y, Class objectType) {
        speed = getSpeed();
        this.setBounds(0, y, width, height);
        this.objectType = objectType;
        setDirection();
    }

    private void setIcon(String carImage) {
        ImageIcon imageIcon = new ImageIcon(carImage);
        Image image = imageIcon.getImage();
        Image i = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(i);
        this.setIcon(imageIcon);
    }

    private void setDirection() {
        Random random = new Random();
        if (random.nextInt(2) == 0) {
            this.setLocation(0, getY());
            direction = "R";

            if (objectType.equals(FrogCar.class)) {
                setIcon("car.png");
            } else {
                setIcon("logs.gif");
            }

        } else {
            this.setLocation(800, getY());
            direction = "L";

            if (objectType.equals(FrogCar.class)) {
                setIcon("bus.gif");
            } else {
                setIcon("logs.gif");
            }
        }
    }

    private int getSpeed() {
        Random random = new Random();
        int speed = random.nextInt(20);
        if (speed > 0) {
            return speed;
        }
        return 1;
    }

    public void move() {

        int y = getY();

        if (direction.equals("R")) {

            int x = getX() + speed;

            if (x <= 800) {
                setLocation(x, y);
            } else {
                setLocation(0, y);
                speed = getSpeed();
                setDirection();
            }
        } else {

            int x = getX() - speed;

            if (x >= 0) {
                setLocation(x, y);
            } else {
                setLocation(800, y);
                speed = getSpeed();
                setDirection();
            }
        }
    }
}
