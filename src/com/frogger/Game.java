package com.frogger;

import javax.swing.*;
import java.awt.*;


public class Game extends JPanel {
    public int rightsize = 800;
    public int leftsize = 800;
    public int oneSize = 20;
    public int Size = (rightsize * leftsize) / 20;
    public int[][] boardSize = new int[Size][Size];
    public int[] roadSize = new int[10];
    public int[] riverSize = new int[10];
//
    public Game(ImageIcon i,int x,int y){
        this.xloc=x;
        this.yloc=y;
        this.i=i;
    }
    private JLabel movment;
private int xloc,yloc,speed;
private  ImageIcon i;
private final int width=16,height=16;
public int getX(){
    return this.xloc;
}
public  int getY(){
    return this.yloc;
}
public int getSpeed(){
    return this.speed;
}
public JLabel getMovment(){
    return  this.movment;
}
public void setX(int x){
    this.xloc=x;
}
    public void setY(int y){
        this.yloc=y;
    }
public void setupMovment(){
   movment=new JLabel();
    movment.setBounds(this.xloc,this.yloc,width,height);
movment.setIcon(this.i);
}
    public Game() {

    }


}
