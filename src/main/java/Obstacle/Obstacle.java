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
    private int width, height;
    private int speed;
    private boolean active = true;
    private BufferedImage image;

    // =========================================================
    //  STATICKÁ ČÁST — načtení Animals.png
    // =========================================================
    private static BufferedImage sheet;
    private static final List<BufferedImage> sprites = new ArrayList<>();
    private static boolean loaded = false;

    // Výřezy zvířat z Animals.png { x, y, w, h }
    private static final int[][] CROPS = {
            {  10, 370, 65, 50 },  // 0 — pásovec
            {  10, 420, 55, 35 },  // 1 — had
            {  10, 465, 65, 35 },  // 2 — ještěrka
            { 220, 520, 55, 70 },  // 3 — lama
            {  10, 560, 65, 70 },  // 4 — velbloud
            {  10, 635, 45, 65 },  // 5 — klokan
            { 590, 350, 65, 60 },  // 6 — lev
    };

    // Rozměry při vykreslení { w, h }
    private static final int[][] SIZES = {
            { 75, 55 },  // pásovec
            { 70, 40 },  // had
            { 75, 40 },  // ještěrka
            { 70, 90 },  // lama
            { 90, 90 },  // velbloud
            { 70, 85 },  // klokan
            { 90, 80 },  // lev
    };

    public static void loadSheet() {
        if (loaded) return;
        sheet = tryLoad("Animals.png");
        if (sheet == null) {
            System.err.println("Animals.png nenalezen!");
            loaded = true;
            return;
        }
        for (int[] c : CROPS) {
            if (c[0] + c[2] <= sheet.getWidth() && c[1] + c[3] <= sheet.getHeight()) {
                sprites.add(sheet.getSubimage(c[0], c[1], c[2], c[3]));
            } else {
                sprites.add(null);
            }
        }
        loaded = true;
        System.out.println("Animals.png načten, zvířat: " + sprites.size());
    }

    private static BufferedImage tryLoad(String name) {
        URL url = Obstacle.class.getClassLoader().getResource(name);
        if (url != null) {
            try { return ImageIO.read(url); }
            catch (IOException e) { System.err.println("Chyba: " + e.getMessage()); }
        }
        String[] paths = { "src/main/resources/" + name, "resources/" + name, name };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) return ImageIO.read(f);
            } catch (IOException ignored) {}
        }
        return null;
    }

    // =========================================================
    //  KONSTRUKTOR
    // =========================================================
    public Obstacle(int x, int groundY, int spriteIndex, int speed) {
        this.speed = speed;

        int w = 70, h = 70;
        if (spriteIndex < SIZES.length) {
            w = SIZES[spriteIndex][0];
            h = SIZES[spriteIndex][1];
        }

        this.width  = w;
        this.height = h;
        this.x      = x;
        this.y      = groundY - h + 10;
        this.image  = (spriteIndex < sprites.size()) ? sprites.get(spriteIndex) : null;
    }

    // =========================================================
    //  UPDATE
    // =========================================================
    public void update() {
        x -= speed;
        if (x + width < 0) active = false;
    }

    // =========================================================
    //  DRAW
    // =========================================================
    public void draw(Graphics g) {
        if (!active) return;
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }


    public Rectangle getHitbox() {
        int margin = 8;
        return new Rectangle(x + margin, y + margin,
                width - 2 * margin, height - 2 * margin);
    }

    public boolean isActive() { return active; }
    public int getX()         { return x; }
}