package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Frog extends JPanel  {
    JLabel frog;
    ImageIcon imageIcon;
    Frog() {
        imageIcon = new ImageIcon ("f.png");
        Image image = imageIcon.getImage ();
        Image i = image.getScaledInstance (55, 55, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon (i);
        frog = new JLabel (imageIcon);
        frog.setLocation (400, 950);

    }
}