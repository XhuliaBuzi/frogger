package com.frogger;
import java.awt.*;
import javax.swing.*;
//i didn't want to create two more classes
// but it wasn't working for mi to create them
// in the same class. i will work again with this

public class Color extends JPanel {

    Color(){
        this.setPreferredSize(new Dimension(800,800));
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setPaint(java.awt.Color.gray);
        g2D.setStroke(new BasicStroke(1));
        //it was to see all the positions
for (int i=0;i<800/20;i++) {
    g.drawLine(i*20,0,i*20,800);
    g.drawLine(0,i*20,800,i*20);
}

    }

}
