package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Game extends JPanel {

    public void paintComponent(Graphics g) {
        super.paintComponent (g);
        this.setBackground (Color.BLACK);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor (Color.gray);
        graphics2D.drawLine (0, 0, 0, 1200);
        graphics2D.drawLine (0, 0, 1200, 0);
        graphics2D.drawLine (0, 800, 800, 800);
        graphics2D.drawLine (800, 0, 800, 800);
        graphics2D.setStroke (new BasicStroke (1));
        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 800; j++) {
                graphics2D.drawLine (i * 50, 0, i * 50, 800);
                graphics2D.drawLine (0, j * 50, 800, j * 50);
            }
        }


    }


}
