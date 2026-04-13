package Entita;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Enemy {


    private int x;
    private int y;
    private static final int WIDTH  = 80;
    private static final int HEIGHT = 50;

    // --- Pohyb ---
    private int speedX;
    private double speedY     = 0;
    private double waveOffset = 0;
    private static final double WAVE_AMP   = 1.5;
    private static final double WAVE_SPEED = 0.07;

    // --- Sprite / animace ---
    private BufferedImage spriteSheet;
    private int frameIndex   = 0;
    private int frameTimer   = 0;
    private static final int FRAME_DELAY = 6;   // snímky za update
    private static final int FRAME_COUNT = 4;   // počet snímků v sheetu (uprav pokud je jich víc)
    private int frameWidth;                      // šířka jednoho snímku — počítá se z obrázku

    // --- Stav ---
    private boolean active = true;             // false = mimo obrazovku, lze odstranit

    // =========================================================
    //  KONSTRUKTOR
    //  startX, startY  — počáteční pozice (typicky WIDTH + 50, výška letu)
    //  speed           — rychlost pohybu doleva
    // =========================================================
    public Enemy(int startX, int startY, int speed) {
        this.x      = startX;
        this.y      = startY;
        this.speedX = speed;
        this.waveOffset = Math.random() * Math.PI * 2; // náhodná fáze vlnění

        loadSprite();
    }

    // =========================================================
    //  NAČTENÍ SPRITU
    // =========================================================
    private void loadSprite() {
        spriteSheet = tryLoad("falconAnim.png");

        if (spriteSheet != null) {
            frameWidth = spriteSheet.getWidth() / FRAME_COUNT;
            if (frameWidth <= 0) frameWidth = spriteSheet.getWidth();
        }
    }

    private BufferedImage tryLoad(String name) {
        // 1. Classpath (resources Root v IntelliJ)
        URL url = getClass().getClassLoader().getResource(name);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("Chyba čtení: " + e.getMessage());
            }
        }

        // 2. Záloha — relativní cesty
        String[] paths = {
                "src/main/resources/" + name,
                "resources/" + name,
                "assets/" + name,
                name
        };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) return ImageIO.read(f);
            } catch (IOException ignored) {}
        }

        System.err.println("NENALEZEN: " + name);
        return null;
    }

    // =========================================================
    //  UPDATE — volat každý tick
    // =========================================================
    public void update() {
        // Pohyb doleva
        x -= speedX;

        // Vlnivý pohyb nahoru/dolů
        waveOffset += WAVE_SPEED;
        y += (int)(Math.sin(waveOffset) * WAVE_AMP);

        // Animace
        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            frameIndex = (frameIndex + 1) % FRAME_COUNT;
        }

        // Deaktivuj když zmizí z obrazovky
        if (x + WIDTH < 0) {
            active = false;
        }
    }

    // =========================================================
    //  DRAW — volat v paintComponent()
    // =========================================================
    public void draw(Graphics2D g) {
        if (!active) return;

        if (spriteSheet != null) {
            int srcX = frameIndex * frameWidth;
            // Ochrana proti přetečení
            if (srcX + frameWidth > spriteSheet.getWidth()) srcX = 0;

            g.drawImage(spriteSheet,
                    x, y, x + WIDTH, y + HEIGHT,
                    srcX, 0, srcX + frameWidth, spriteSheet.getHeight(),
                    null);
        } else {
            // Záloha — červený trojúhelník (sokol)
            g.setColor(new Color(180, 40, 40));
            int[] px = { x, x + WIDTH, x + WIDTH / 2 };
            int[] py = { y + HEIGHT / 2, y + HEIGHT / 2, y };
            g.fillPolygon(px, py, 3);
        }
    }


    public Rectangle getHitbox() {
        int margin = 10;
        return new Rectangle(x + margin, y + margin, WIDTH - 2 * margin, HEIGHT - 2 * margin);
    }

    // =========================================================
    //  GETTERY
    // =========================================================
    public boolean isActive() {
        return active;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}