package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Logs extends JPanel implements ActionListener {
    ImageIcon imageIcon;
    Timer t;
    int x = 0, y = 0, velX = 2, velY = 2;

    public void paintComponent(Graphics g) {
        super.paintComponent (g);
        Graphics2D graphics2D = (Graphics2D) g;
        imageIcon = new ImageIcon ("logs.gif");
        Image image1 = imageIcon.getImage ().getScaledInstance (120, 80, Image.SCALE_SMOOTH);
        ImageIcon Logs_ImageIcon = new ImageIcon (image1);
        Image Logs_Image1 = Logs_ImageIcon.getImage ();
        graphics2D.drawImage (Logs_Image1, x, y, this);
        graphics2D.drawImage (Logs_Image1, x, y + 200, this);
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
        }
        x += velX;
        repaint ();
    }
}
