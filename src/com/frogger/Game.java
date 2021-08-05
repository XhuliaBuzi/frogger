package com.frogger;

import javax.swing.*;
import java.awt.*;

public class Game extends JPanel {
    Image image;

    public void paintComponent(Graphics g) {
        super.paintComponent (g);
        Graphics2D graphics2D = (Graphics2D) g;
        image = new ImageIcon ("strit.png").getImage ();
        Image i = image.getScaledInstance (785, 962, Image.SCALE_SMOOTH);
        image = new ImageIcon (i).getImage ();
        graphics2D.drawImage (image, 0, 0, null);
    }
}
