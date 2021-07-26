package com.frogger;
import javax.swing.*;
import java.awt.event.KeyAdapter;


public class Border extends JFrame {
    //i didn't want to create two more classes
// but it wasn't working for me to create them
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

    }

