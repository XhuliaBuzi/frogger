package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Logs extends JPanel implements ActionListener {
    ImageIcon imageIcon;
    Timer t;
    int width = 120, anInt = 120 - 70, height = 80;
    int x = 678, y = 250, velX = 1, right = 23;
    int x2 = x - (anInt * 2), x3 = x - (anInt * 3), x4 = x - (anInt * 4), x7 = x - (anInt * 7), x8 = x - (anInt * 8), x9 = x - (anInt * 9), x10 = x - (anInt * 10), x13 = x - (anInt * 13), x14 = x - (anInt * 14);
    int right2 = right + (anInt * 2), right4 = right + (anInt * 4), right5 = right + (anInt * 5), right9 = right + (anInt * 9), right10 = right + (anInt * 10), right12 = right + (anInt * 12), right15 = right + (anInt * 15);

    public void paintComponent(Graphics g) {
        super.paintComponent (g);
        Graphics2D graphics2D = (Graphics2D) g;
        imageIcon = new ImageIcon ("logs.gif");
        Image image1 = imageIcon.getImage ().getScaledInstance (width, height, Image.SCALE_SMOOTH);
        ImageIcon Logs_ImageIcon = new ImageIcon (image1);
        Image Logs_Image1 = Logs_ImageIcon.getImage ();
        graphics2D.drawImage (Logs_Image1, x - anInt, y, this);//First Logs
        graphics2D.drawImage (Logs_Image1, x3, y, this);
        graphics2D.drawImage (Logs_Image1, x4, y, this);
        graphics2D.drawImage (Logs_Image1, x7, y, this);
        graphics2D.drawImage (Logs_Image1, x10, y, this);
        graphics2D.drawImage (Logs_Image1, x8, y, this);
        graphics2D.drawImage (Logs_Image1, x13, y, this);
        graphics2D.drawImage (Logs_Image1, right2, y + 100, this);//Second Logs
        graphics2D.drawImage (Logs_Image1, right4, y + 100, this);
        graphics2D.drawImage (Logs_Image1, right5, y + 100, this);
        graphics2D.drawImage (Logs_Image1, right9, y + 100, this);
        graphics2D.drawImage (Logs_Image1, right10 + 50, y + 100, this);
        graphics2D.drawImage (Logs_Image1, right12, y + 100, this);
        graphics2D.drawImage (Logs_Image1, right15, y + 100, this);
        graphics2D.drawImage (Logs_Image1, x, y + 200, this);// Third Logs
        graphics2D.drawImage (Logs_Image1, x2, y + 200, this);
        graphics2D.drawImage (Logs_Image1, x4, y + 200, this);
        graphics2D.drawImage (Logs_Image1, x7, y + 200, this);
        graphics2D.drawImage (Logs_Image1, x9, y + 200, this);
        graphics2D.drawImage (Logs_Image1, x14, y + 200, this);
        //Timer for starting the game.
        t = new Timer (500, this);
        t.start ();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (x == 800) {
            x = -2;
        } else if (x2 == 800) {
            x = -width;
        } else if (x3 == 800) {
            x3 = -width;
        } else if (x4 == 800) {
            x4 = -width;
        } else if (x7 == 800) {
            x7 = -width;
        } else if (x8 == 800) {
            x8 = -width;
        } else if (x10 == 800) {
            x10 = -width;
        } else if (x9 == 800) {
            x9 = -width;
        } else if (x13 == 800) {
            x13 = -width;
        } else if (x14 == 800) {
            x14 = -width;
        } else if (right2 == width) {
            right2 = 700;
        } else if (right4 == -width) {
            right4 = 800;
        } else if (right5 == -width) {
            right5 = 690;
        } else if (right10 == -width) {
            right10 = 700;
        } else if (right9 == -width) {
            right9 = 775;
        } else if (right12 == -width) {
            right12 = 680;
        } else if (right15 == -width) {
            right15 = 800;
        }
        x += velX;
        x2 += velX;
        x3 += velX;
        x4 += velX;
        x7 += velX;
        x10 += velX;
        x9 += velX;
        x8 += velX;
        x13 += velX;
        x14 += velX;
        right2 -= velX;
        right4 -= velX;
        right5 -= velX;
        right9 -= velX;
        right10 -= velX;
        right12 -= velX;
        right15 -= velX;
        repaint ();
    }
}
