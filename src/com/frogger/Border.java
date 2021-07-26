package com.frogger;
import javax.swing.*;
import java.awt.event.KeyAdapter;


public class Border extends JFrame {
    //i didn't want to create two more classes
// but it wasn't working for mi to create them
// in the same class. i will work again with this.
Object object;
Color color;

    public Border() {
            color = new Color();
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setBackground(java.awt.Color.BLACK);
            this.add(color);
            this.setLocationRelativeTo(null);
            this.pack();
            this.setVisible(true);

        }























//        this.setSize(400,400);
//        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//this.setLocationRelativeTo(null);
//
//        this.setVisible(true);
//    }
//
//    public void PaintComponent(Graphics g){
//
//        Graphics2D g2d=(Graphics2D) g;
//        g2d.setColor(new Color(1234567));
//        g2d.drawLine(0,0,40,40);
    }

