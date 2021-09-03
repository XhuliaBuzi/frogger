package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
public class Board extends JPanel {
    private final int height = 1000;
    private final Frog frog = new Frog ();
    private final ArrayList<Car> cars = new ArrayList<> ();
    private final ArrayList<Logs> logs = new ArrayList<> ();
    private final ArrayList<Bus> buses = new ArrayList<> ();
    private Image image;
    public Board() {
        this.setSize (800, height);
        this.setLayout (null);
        this.add (frog);
        addCars (6);
        addLogs (6, "R");
        addBus (6);
        this.setVisible (true);
    }
    private void addCars(int totalCars) {
        // FOR Loop
        for (int i = 1; i <= totalCars; i++) {
            Car car=new Car() ;
            cars.add (car);
            this.add (car);
        }
    }
    private void addBus(int totalBus) {
        // FOR Loop
        for (int i = 1; i <= totalBus; i++) {
            Bus bus = new Bus ();
            buses.add (bus);
            this.add (bus);
        }
    }
    private void addLogs(int totalLogs, String temp) {

        // FOR Loop
        int y_logs = 237;
        for (int j = 0; j < 3; j++) {

            for (int i = 1; i <= totalLogs; i++) {

                Logs log = new Logs (y_logs, temp);
                logs.add (log);
                this.add (log);
            }
            y_logs += 100;
            if (temp == "L") {
                temp = "R";
            } else {
                temp = "L";
            }
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder (g);
        Graphics2D graphics2D = (Graphics2D) g;
        image = new ImageIcon ("strit.png").getImage ();
        Image i = image.getScaledInstance (785, 962, Image.SCALE_SMOOTH);
        image = new ImageIcon (i).getImage ();
        graphics2D.drawImage (image, 0, 0, null);
    }
    public Frog getFrog() {
        return frog;
    }
    public ArrayList<Car> getCars() {
        return cars;
    }
    public ArrayList<Logs> getLogs() {
        return logs;
    }
    public ArrayList<Bus> getBuses() {
        return buses;
    }


}
