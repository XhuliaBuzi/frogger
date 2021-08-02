package com.frogger;

public class Main {
    public static void main(String[] args) {
        Car car=new Car ();
        Logs logs=new Logs ();
        MyBoard myBoard = new MyBoard ();
        myBoard.setVisible (true);
        myBoard.add (car);
    }

}
