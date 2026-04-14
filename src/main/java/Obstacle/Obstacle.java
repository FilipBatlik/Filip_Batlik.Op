package Obstacle;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Obstacle {

    private int x, y;
    private static final int WIDTH  = 80;
    private static final int HEIGHT = 80;
    private int speed;
    private boolean active = true;

    private final List<BufferedImage> frames;
    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY = 6;

    public enum Type { FLY, ATTACK, IDLE }
    private final Type type;


    private static final List<BufferedImage> flyFrames    = new ArrayList<>();
    private static final List<BufferedImage> attackFrames = new ArrayList<>();
    private static final List<BufferedImage> idleFrames   = new ArrayList<>();
    private static boolean loaded = false;

    public static void loadSprites() {
        if (loaded) return;

        for (int i = 1; i <= 7; i++) {
            BufferedImage img = tryLoad(String.format("fly%02d.png", i));
            if (img != null) flyFrames.add(img);
        }

        for (int i = 1; i <= 10; i++) {
            BufferedImage img = tryLoad(String.format("attack%02d.png", i));
            if (img != null) attackFrames.add(img);
        }

        for (int i = 1; i <= 8; i++) {
            BufferedImage img = tryLoad(String.format("idle%02d.png", i));
            if (img != null) idleFrames.add(img);
        }

        loaded = true;
        System.out.println("fly: " + flyFrames.size()
                + ", attack: " + attackFrames.size()
                + ", idle: " + idleFrames.size());
    }

    private static BufferedImage tryLoad(String name) {
        URL url = Obstacle.class.getClassLoader().getResource(name);
        if (url != null) {
            try { return ImageIO.read(url); }
            catch (IOException e) { System.err.println("Chyba: " + e.getMessage()); }
        }
        String[] paths = { "src/main/resources/" + name, "resources/" + name, "assets/" + name, name };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) return ImageIO.read(f);
            } catch (IOException ignored) {}
        }
        return null;
    }


    public Obstacle(int x, int groundY, Type type, int speed) {
        this.type  = type;
        this.speed = speed;
        this.x     = x;

        switch (type) {
            case FLY:
                this.y      = groundY - HEIGHT - 20;
                this.frames = new ArrayList<>(flyFrames);
                break;
            case ATTACK:
                this.y      = groundY - HEIGHT + 10;
                this.frames = new ArrayList<>(attackFrames);
                break;
            case IDLE:
            default:
                this.y      = groundY - HEIGHT + 10;
                this.frames = new ArrayList<>(idleFrames);
                break;
        }
    }


    public void update() {
        x -= speed;
        if (x + WIDTH < 0) active = false;

        if (!frames.isEmpty()) {
            frameTimer++;
            if (frameTimer >= FRAME_DELAY) {
                frameTimer = 0;
                frameIndex = (frameIndex + 1) % frames.size();
            }
        }
    }


    public void draw(Graphics g) {
        if (!active) return;
        Graphics2D g2 = (Graphics2D) g;
        if (!frames.isEmpty()) {
            g2.drawImage(frames.get(frameIndex), x, y, WIDTH, HEIGHT, null);
        } else {
            g2.setColor(type == Type.FLY ? Color.BLUE : Color.RED);
            g2.fillRect(x, y, WIDTH, HEIGHT);
        }
    }


    public Rectangle getHitbox() {
        int margin = 10;
        return new Rectangle(x + margin, y + margin,
                WIDTH - 2 * margin, HEIGHT - 2 * margin);
    }

    public boolean isActive() { return active; }
    public int getX()         { return x; }
    public Type getType()     { return type; }
}