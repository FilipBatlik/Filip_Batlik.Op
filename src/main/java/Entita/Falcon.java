package Entita;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Falcon {

    private int x;
    private int y;
    private static final int WIDTH  = 80;
    private static final int HEIGHT = 50;

    private final int speedX;
    private double waveOffset;
    private static final double WAVE_AMP   = 1.5;
    private static final double WAVE_SPEED = 0.07;

    private BufferedImage spriteSheet;
    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY = 6;
    private static final int FRAME_COUNT = 4;
    private int frameWidth = 16;

    private boolean active = true;


    public Falcon(int startX, int startY, int speed) {
        this.x          = startX;
        this.y          = startY;
        this.speedX     = speed;
        this.waveOffset = Math.random() * Math.PI * 2;
        loadSprite();
    }


    private void loadSprite() {
        spriteSheet = tryLoad("falconAnim.png");
        if (spriteSheet != null) {
            frameWidth = spriteSheet.getWidth() / FRAME_COUNT;
            if (frameWidth <= 0) frameWidth = spriteSheet.getWidth();
            System.out.println("falconAnim.png načten, frameWidth: " + frameWidth);
        }
    }

    private BufferedImage tryLoad(String name) {
        URL url = getClass().getClassLoader().getResource(name);
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
        System.err.println("NENALEZEN: " + name);
        return null;
    }

    public void update() {
        x -= speedX;

        waveOffset += WAVE_SPEED;
        y += (int)(Math.sin(waveOffset) * WAVE_AMP);

        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            frameIndex = (frameIndex + 1) % FRAME_COUNT;
        }

        if (x + WIDTH < 0) active = false;
    }


    public void draw(Graphics g) {
        if (!active) return;
        Graphics2D g2 = (Graphics2D) g;


        if (spriteSheet != null) {
            int srcX = frameIndex * frameWidth;
            if (srcX + frameWidth > spriteSheet.getWidth()) srcX = 0;
            g2.drawImage(spriteSheet,
                    x, y, x + WIDTH, y + HEIGHT,
                    srcX, 0, srcX + frameWidth, spriteSheet.getHeight(),
                    null);
        } else {
            g2.setColor(new Color(180, 40, 40));
            int[] px = { x, x + WIDTH, x + WIDTH / 2 };
            int[] py = { y + HEIGHT / 2, y + HEIGHT / 2, y };
            g2.fillPolygon(px, py, 3);
        }
    }


    public Rectangle getHitbox() {
        int margin = 10;
        return new Rectangle(x + margin, y + margin,
                WIDTH - 2 * margin, HEIGHT - 2 * margin);
    }

    public boolean isActive() { return active; }
    public int getX()         { return x; }
    public int getY()         { return y; }
}