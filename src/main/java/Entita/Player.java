package Entita;

import java.awt.Graphics;
import java.awt.Rectangle;

public class Player {
    protected int x, y;
    protected int width, height;
    protected Rectangle hitbox;

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {

    }
}