package com.frogger.dan;

import javax.swing.*;
import java.awt.*;

public class DanGame extends JFrame {

    private DanBoard board = new DanBoard();

    public DanGame(String title) throws HeadlessException {
        super(title);
        this.setSize(800, 1000);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(board);
        this.setVisible(true);
    }
}
