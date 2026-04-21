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
    private static final int GROUND_STRIP_HEIGHT = 60;

    // groundY je teď dynamické – platform bude vždy těsně nad spodním okrajem
    private final int groundY;

    public Background(int panelWidth, int panelHeight) {
        this.panelWidth  = panelWidth;
        this.panelHeight = panelHeight;
        // Platforma sedí tak, aby spodní vrstva dosáhla až na kraj panelu
        this.groundY     = panelHeight - GROUND_STRIP_HEIGHT - 40;
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
        int y = groundY;

        if (imgGround != null) {
            // === S texturou: hlavní scrollující pás ===
            int tileW = imgGround.getWidth();
            if (tileW <= 0) tileW = panelWidth;
            int tilesNeeded = (panelWidth / tileW) + 2;
            int startX      = -(groundScrollX % tileW);
            for (int i = 0; i < tilesNeeded; i++) {
                g.drawImage(imgGround, startX + i * tileW, y, tileW, GROUND_STRIP_HEIGHT, null);
            }

            // === Vrstva 2: tmavší zemina pod texturou ===
            g.setColor(new Color(0x5C3317));
            g.fillRect(0, y + GROUND_STRIP_HEIGHT, panelWidth, 20);

            // === Vrstva 3: nejhlubší hornina až ke spodnímu okraji ===
            g.setColor(new Color(0x3D1F0A));
            g.fillRect(0, y + GROUND_STRIP_HEIGHT + 20, panelWidth, panelHeight - (y + GROUND_STRIP_HEIGHT + 20));

        } else {
            // === Fallback bez textury – 4 barevné vrstvy ===

            // Vrstva 1: povrch (tráva/hlína)
            g.setColor(new Color(0x8B5E3C));
            g.fillRect(0, y, panelWidth, GROUND_STRIP_HEIGHT);

            // Tmavší linie na přechodu povrch → zemina
            g.setColor(new Color(0x6B4020));
            g.fillRect(0, y, panelWidth, 6);

            // Vrstva 2: zemina
            g.setColor(new Color(0x5C3317));
            g.fillRect(0, y + GROUND_STRIP_HEIGHT, panelWidth, 25);

            // Vrstva 3: hornina
            g.setColor(new Color(0x4A2F1A));
            g.fillRect(0, y + GROUND_STRIP_HEIGHT + 25, panelWidth, 15);

            // Vrstva 4: nejhlubší – až ke spodnímu okraji
            g.setColor(new Color(0x3D1F0A));
            g.fillRect(0, y + GROUND_STRIP_HEIGHT + 40, panelWidth,
                    panelHeight - (y + GROUND_STRIP_HEIGHT + 40));
        }
    }

    public int getGroundY() {
        return groundY;
    }

    public int getGroundSpeed() {
        return GROUND_SPEED;
    }
}