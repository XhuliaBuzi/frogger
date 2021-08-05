package com.frogger.dan;

import javax.swing.*;

public class DanBoard extends JPanel {

    public DanBoard() {

        DanCar car1 = new DanCar();
        DanCar car2 = new DanCar();

        add(car1);
        add(car2);
    }
}
