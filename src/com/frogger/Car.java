package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Car extends JPanel implements ActionListener {

    Timer timer_Car;
    int width_car = 80, height_car = 60, width_bus = 150, height_bus = 100;
    int x = 680, y = 750, y2 = y - 120, velX = 1;
    int xCAR3 = (x - (width_car * 3)), xCAR4 = (x - (width_car * 4)), xCAR8 = (x - (width_car * 7)), xCAR9 = (x - (width_car * 9)), xCAR10 = (x - (width_car * 10));
    int xBUS = 10, xBUS1 = xBUS + width_bus, xBUS3 = (xBUS + (width_bus * 4)), xBUS6 = (xBUS + (width_bus * 6)), xBUS7 = (xBUS + (width_bus * 7)), xBUS9 = (xBUS + (width_bus * 9)), xBUS10 = (xBUS + (width_bus * 10));

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        Image imageCar = new ImageIcon("car.png").getImage().getScaledInstance(width_car, height_car, Image.SCALE_SMOOTH);
        Image car_Image = new ImageIcon(imageCar).getImage();
        Image imageBus = new ImageIcon("bus.gif").getImage().getScaledInstance(width_bus, height_bus, Image.SCALE_SMOOTH);
        Image bus_Image = new ImageIcon(imageBus).getImage();
        // First Cars
        graphics2D.drawImage(car_Image, xCAR3, y, this);
        graphics2D.drawImage(car_Image, xCAR4, y, this);
        graphics2D.drawImage(car_Image, xCAR8, y, this);
        graphics2D.drawImage(car_Image, xCAR10, y, this);
        graphics2D.drawImage(car_Image, xCAR9, y, this);
        // Second Cars
        graphics2D.drawImage(bus_Image, xBUS1, y2, this);
        graphics2D.drawImage(bus_Image, xBUS3, y2, this);
        graphics2D.drawImage(bus_Image, xBUS6, y2, this);
        graphics2D.drawImage(bus_Image, xBUS7, y2, this);
        graphics2D.drawImage(bus_Image, xBUS10, y2, this);
        graphics2D.drawImage(bus_Image, xBUS9, y2, this);

        timer_Car = new Timer(1000, this);
        timer_Car.start();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (x == 800) {
            x = -width_car;
        } else if (xCAR3 == 800) {
            xCAR3 = -width_car;
        } else if (xCAR4 == 800) {
            xCAR4 = -width_car;
        } else if (xCAR8 == 800) {
            xCAR8 = -width_car;
        } else if (xCAR9 == 800) {
            xCAR9 = -width_car;
        } else if (xCAR10 == 800) {
            xCAR10 = -width_car;
        } else if (xBUS1 == -width_bus) {
            xBUS = 802;
        } else if (xBUS3 == -width_bus) {
            xBUS3 = 802;
        } else if (xBUS6 == -width_bus) {
            xBUS6 = 802;
        } else if (xBUS7 == -width_bus) {
            xBUS7 = 802;
        } else if (xBUS10 == -width_bus) {
            xBUS10 = 802;
        } else if (xBUS9 == -width_bus) {
            xBUS9 = 803;
        }
        xCAR3 += velX;
        xCAR4 += velX;
        xCAR8 += velX;
        xCAR9 += velX;
        xCAR10 += velX;

        xBUS1 -= velX;
        xBUS3 -= velX;
        xBUS6 -= velX;
        xBUS7 -= velX;
        xBUS9 -= velX;
        xBUS10 -= velX;
        repaint();
    }
}
