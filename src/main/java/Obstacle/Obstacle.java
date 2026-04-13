package Obstacle;

import java.awt.*;

public class Obstacle {

    private int x, y;
    private int width = 30, height = 50;
    private int speed = 5;

    public Obstacle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x -= speed;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }

    public int getX() {
        return x;
    }
}