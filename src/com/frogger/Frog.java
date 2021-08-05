package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Frog extends JPanel {
    JLabel frog;
    ImageIcon imageIcon;
    int x = 14, y = 5;

    Frog() {
        imageIcon = new ImageIcon("f.png");
        Image image = imageIcon.getImage();
        Image i = image.getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(i);
        frog = new JLabel(imageIcon);
        frog.setLocation(x, y);
    }
}