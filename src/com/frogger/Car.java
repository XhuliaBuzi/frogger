package com.frogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class Car extends JPanel implements ActionListener {
    ImageIcon imageIcon_CAR;
    Timer timer_Car;
    int x=0,y=500,velX=2,velY=2;
    public void paintComponent(Graphics g){
        super.paintComponent (g);
        Graphics2D graphics2D=(Graphics2D) g;
        imageIcon_CAR = new ImageIcon ("car.png");
        Image image1=imageIcon_CAR.getImage ().getScaledInstance (120,80,Image.SCALE_SMOOTH);
        ImageIcon CAR_ImageIcon=new ImageIcon (image1);
        Image car_Image=CAR_ImageIcon.getImage ();
        graphics2D.drawImage (car_Image,x,y,this);
        graphics2D.drawImage (car_Image,x,y+200,this);
        timer_Car =new Timer (500,this);
        timer_Car.start ();
    }


    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(x==800){
            x=-2;
        }
        x+=velX;
        repaint ();
    }
}
