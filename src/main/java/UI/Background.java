package UI;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Background {

    private BufferedImage imgBackground;
    private BufferedImage imgGround;

    private final int panelWidth;
    private final int panelHeight;

    private int bgScrollX     = 0;
    private int groundScrollX = 0;

    private static final int BG_SPEED            = 2;
    private static final int GROUND_SPEED        = 5;
    public  static final int GROUND_Y            = 620;
    private static final int GROUND_STRIP_HEIGHT = 60;

    public Background(int panelWidth, int panelHeight) {
        this.panelWidth  = panelWidth;
        this.panelHeight = panelHeight;
        loadImages();
    }

    private void loadImages() {
        imgBackground = tryLoad("Backgrounds.png");
        imgGround     = tryLoad("Ground.png");
    }


    private BufferedImage tryLoad(String name) {

        URL url = getClass().getClassLoader().getResource(name);
        if (url != null) {
            try {
                System.out.println("Načten: " + url);
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("Chyba čtení: " + e.getMessage());
            }
        }

        String[] paths = {
                "src/main/resources/" + name,
                "resources/" + name,
                "assets/" + name,
                name
        };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    System.out.println("Načten ze souboru: " + f.getAbsolutePath());
                    return ImageIO.read(f);
                }
            } catch (IOException ignored) {}
        }

        System.err.println("NENALEZEN: " + name);
        return null;
    }

    public void update() {
        bgScrollX     = (bgScrollX     + BG_SPEED)     % panelWidth;
        groundScrollX = (groundScrollX + GROUND_SPEED) % panelWidth;
    }

    public void draw(Graphics2D g) {
        drawBackground(g);
        drawGround(g);
    }

    private void drawBackground(Graphics2D g) {
        if (imgBackground != null) {
            g.drawImage(imgBackground, -bgScrollX,             0, panelWidth, panelHeight, null);
            g.drawImage(imgBackground, panelWidth - bgScrollX, 0, panelWidth, panelHeight, null);
        } else {
            GradientPaint sky = new GradientPaint(
                    0, 0,           new Color(0xB0C8A0),
                    0, panelHeight, new Color(0xD4BA7A));
            g.setPaint(sky);
            g.fillRect(0, 0, panelWidth, panelHeight);
        }
    }

    private void drawGround(Graphics2D g) {
        int y = GROUND_Y;
        int h = GROUND_STRIP_HEIGHT;

        if (imgGround != null) {
            int tileW = imgGround.getWidth();
            if (tileW <= 0) tileW = panelWidth;
            int tilesNeeded = (panelWidth / tileW) + 2;
            int startX      = -(groundScrollX % tileW);
            for (int i = 0; i < tilesNeeded; i++) {
                g.drawImage(imgGround, startX + i * tileW, y, tileW, h, null);
            }
        } else {
            g.setColor(new Color(0x8B5E3C));
            g.fillRect(0, y, panelWidth, h);
            g.setColor(new Color(0x6B4020));
            g.fillRect(0, y, panelWidth, 4);
        }
    }

    public int getGroundY() {
        return GROUND_Y;
    }

    public int getGroundSpeed() {
        return GROUND_SPEED;
    }
}