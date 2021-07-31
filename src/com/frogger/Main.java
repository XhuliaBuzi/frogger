package com.frogger;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Game game = new Game ();
        Car car=new Car ();
        MyBoard myBoard = new MyBoard ();
        myBoard.add (car);
        myBoard.setVisible (true);

    }

}
