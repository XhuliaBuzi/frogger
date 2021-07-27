package com.frogger;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;

public class Main {

    public Main() {
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.black);

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 800, 800);
        panel.setLayout(null);


    }


    public static void main(String[] args) {
        new Main();
    }

}
