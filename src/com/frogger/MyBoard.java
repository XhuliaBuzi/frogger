package com.frogger;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MyBoard extends JFrame implements KeyListener {
    Frog frog2 = new Frog ();
    JLabel Frod3 = frog2.frog;
    Car car = new Car ();

    Logs logs = new Logs ();
    JPanel panel = new JPanel ();

    MyBoard() {
        setSize (800, 1000);
        setResizable (false);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        this.add (Frod3);

        this.addKeyListener (this);

    }

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (Frod3.getX () == -50) {
            Frod3.setLocation (Frod3.getX () + 50, Frod3.getY ());
        } else if (Frod3.getY () == -50) {
            Frod3.setLocation (Frod3.getX (), Frod3.getY () + 50);
        } else if (Frod3.getX () == 850) {
            Frod3.setLocation (Frod3.getX () - 50, Frod3.getY ());
        } else if (Frod3.getY () == 1050) {
            Frod3.setLocation (Frod3.getX (), Frod3.getY () - 50);
        }
        switch (e.getKeyCode ()) {
            case 37: {
                Frod3.setLocation (Frod3.getX () - 50, Frod3.getY ());
                break;
            }
            case 38: {
                Frod3.setLocation (Frod3.getX (), Frod3.getY () - 50);
                break;
            }
            case 39: {
                Frod3.setLocation (Frod3.getX () + 50, Frod3.getY ());
                break;
            }
            case 40: {
                Frod3.setLocation (Frod3.getX (), Frod3.getY () + 50);
                break;
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }


}
