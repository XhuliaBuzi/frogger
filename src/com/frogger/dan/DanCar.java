package com.frogger.dan;

import javax.swing.*;
import java.awt.*;

public class DanCar extends JLabel {

    public DanCar() {
        ImageIcon imageIcon = new ImageIcon("car.png");
        Image image = imageIcon.getImage();
        Image i = image.getScaledInstance(55, 30, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(i);
        setIcon(imageIcon);
    }
}
