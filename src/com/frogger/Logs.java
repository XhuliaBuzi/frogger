package com.frogger;

import java.awt.*;

public class Logs extends Object {
    int y1;
    public Logs(int y, String direction) {
        super ("logs.gif", 750, y, 140, 100, direction, 40);
        this.y1=y;

    }
    public Rectangle bondsLogs(){
        return (new Rectangle(750,y1,140,100));
    }

}