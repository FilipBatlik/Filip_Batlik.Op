package Obstacle;

import java.awt.*;

public class Falcon {

    int x;
    int y;
    int height;
    int width;
    Rectangle hitbox;

    public Falcon(int x, int y, int height, int width) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.hitbox = new Rectangle(x, y, width, height);
    }
    public void drawbird(Graphics g){

    }
}