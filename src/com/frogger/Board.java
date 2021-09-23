package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {
    public final int car_rows = 3, number_of_cars = 3;
    public final int logs_rows = 5, number_of_logs = 6;
    public final int buses_rows = 2, number_of_buses = 3;
    private final int height = 1000, width = 800;
    private final Frog frog = new Frog();
    private final Car[][] cars = new Car[car_rows][number_of_cars];
    private final Logs[][] logs = new Logs[logs_rows][number_of_logs];
    private final Bus[][] buses = new Bus[buses_rows][number_of_buses];
    private int log_height, car_height, bus_height, x_logs = 100;
    private Image image;

    public Board() {
        this.setSize(width, height);
        this.setLayout(null);
        this.add(frog);
        addCars();
        addLogs("R");
        addBus();
        this.setVisible(true);
    }

    private void addCars() {
        // FOR Loop
        int y_cars = 525;
        for (int i = 0; i < car_rows; i++) {
            for (int j = 0; j < number_of_cars; j++) {
                Car car = new Car(y_cars);
                cars[i][j] = car;
                this.add(car);
                car_height = car.getHeight();
            }
            y_cars += car_height * 2 + 5;
        }
    }

    private void addBus() {
        // FOR Loop
        int y_bus = 580;
        for (int i = 0; i < buses_rows; i++) {
            for (int j = 0; j < number_of_buses; j++) {
                Bus bus = new Bus(y_bus);
                buses[i][j] = bus;
                this.add(bus);
                bus_height = bus.getHeight();
            }
            y_bus += bus_height * 2 - 15;
        }
    }

    private void addLogs(String temp) {
        // FOR Loop
        int y_logs = 75;
        for (int i = 0; i < logs_rows; i++) {
            for (int j = 0; j < number_of_logs; j++) {
                if (temp == "R") {
                    x_logs = 800;
                } else {
                    x_logs = 0;
                }
                Logs log = new Logs(x_logs, y_logs, temp);
                log_height = log.getHeight();
                logs[i][j] = log;
                this.add(log);

            }
            y_logs += log_height - 2;
            if (temp == "L") {
                temp = "R";
            } else {
                temp = "L";
            }
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        Graphics2D graphics2D = (Graphics2D) g;
        image = new ImageIcon("strit.png").getImage();
        Image i = image.getScaledInstance(785, 962, Image.SCALE_SMOOTH);
        image = new ImageIcon(i).getImage();
        graphics2D.drawImage(image, 0, 0, null);
        //for the life of the frog
        image = new ImageIcon("zemra.gif").getImage();
        Image j = image.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        int life = getFrog().life, x = 750;
        while (life > 0) {
            image = new ImageIcon(j).getImage();
            graphics2D.drawImage(image, x, 20, null);
            life -= 1;
            x -= 30;
        }
    }

    public Frog getFrog() {
        return frog;
    }

    public Car[][] getCars() {
        return cars;
    }

    public Logs[][] getLogs() {
        return logs;
    }

    public Bus[][] getBuses() {
        return buses;
    }
}
