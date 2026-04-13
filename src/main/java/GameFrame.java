import UI.Background;
import Entita.Enemy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameFrame extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 832;
    private static final int FPS    = 60;

    private static final int PLAYER_RENDER_WIDTH  = 90;
    private static final int PLAYER_RENDER_HEIGHT = 90;

    private enum GameState { MENU, PLAYING, DYING, GAME_OVER }
    private GameState state = GameState.MENU;

    private final Timer timer;
    private final Background background;

    // --- Sprite obrázky ---
    private BufferedImage imgOstrichRun;
    private BufferedImage imgOstrichJump;
    private BufferedImage imgOstrichDeath;

    private BufferedImage imgRock1;
    private BufferedImage imgRock2;
    private BufferedImage imgRock3;
    private BufferedImage imgTree1;
    private BufferedImage imgTree2;

    // Rozměry snímků
    private int runFrameCount   = 4;
    private int runFrameWidth   = 16;
    private int jumpFrameCount  = 2;
    private int jumpFrameWidth  = 16;
    private int deathFrameCount = 1;
    private int deathFrameWidth = 16;

    // --- Hráč ---
    private int playerX      = 200;
    private int playerY;
    private double velocityY = 0;
    private boolean onGround = true;
    private static final double GRAVITY    = 0.8;
    private static final double JUMP_FORCE = -18;

    // Animace
    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY       = 6;
    private static final int DEATH_FRAME_DELAY = 8; // trochu pomalejší animace smrti

    // --- Překážky ---
    private final List<Obstacle> obstacles = new ArrayList<>();
    private int obstacleTimer  = 0;
    private int nextObstacleIn = 90;

    // --- Nepřátelé ---
    private final List<Enemy> enemies = new ArrayList<>();
    private int enemyTimer  = 0;
    private int nextEnemyIn = 300;

    // --- Skóre ---
    private int score      = 0;
    private int highScore  = 0;
    private int scoreTimer = 0;

    private final Random random = new Random();

    // =========================================================
    //  KONSTRUKTOR
    // =========================================================
    public GameFrame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        background = new Background(WIDTH, HEIGHT);
        playerY    = background.getGroundY();

        loadImages();

        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    // =========================================================
    //  NAČTENÍ OBRÁZKŮ
    // =========================================================
    private void loadImages() {
        imgOstrichRun   = tryLoad("OstrichRun.png");
        imgOstrichJump  = tryLoad("OstrichJump-Sheet.png");
        imgOstrichDeath = tryLoad("OstrichDeath.png");

        if (imgOstrichRun != null) {
            runFrameWidth = imgOstrichRun.getHeight();
            runFrameCount = imgOstrichRun.getWidth() / runFrameWidth;
            System.out.println("OstrichRun: " + runFrameCount + " snímků");
        }
        if (imgOstrichJump != null) {
            jumpFrameWidth = imgOstrichJump.getHeight();
            jumpFrameCount = imgOstrichJump.getWidth() / jumpFrameWidth;
            System.out.println("OstrichJump: " + jumpFrameCount + " snímků");
        }
        if (imgOstrichDeath != null) {
            deathFrameWidth = imgOstrichDeath.getHeight();
            deathFrameCount = imgOstrichDeath.getWidth() / deathFrameWidth;
            System.out.println("OstrichDeath: " + deathFrameCount + " snímků");
        }

        imgRock1 = tryLoad("Rock1.png");
        imgRock2 = tryLoad("Rock 2.png");
        imgRock3 = tryLoad("Rock3.png");
        imgTree1 = tryLoad("tree1.png");
        imgTree2 = tryLoad("tree2.png");
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

    // =========================================================
    //  HERNÍ SMYČKA
    // =========================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (state) {
            case PLAYING: updatePlaying(); break;
            case DYING:   updateDying();   break;
            default: break;
        }
        repaint();
    }

    private void updatePlaying() {
        background.update();

        // Fyzika
        if (!onGround) velocityY += GRAVITY;
        playerY += (int) velocityY;
        if (playerY >= background.getGroundY()) {
            playerY   = background.getGroundY();
            velocityY = 0;
            onGround  = true;
        }

        // Animace běhu/skoku
        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            if (onGround) frameIndex = (frameIndex + 1) % runFrameCount;
            else          frameIndex = (frameIndex + 1) % jumpFrameCount;
        }

        // Překážky
        obstacleTimer++;
        if (obstacleTimer >= nextObstacleIn) {
            obstacleTimer  = 0;
            nextObstacleIn = 80 + random.nextInt(120);
            spawnObstacle();
        }
        obstacles.removeIf(o -> o.x + o.width < 0);
        for (Obstacle o : obstacles) {
            o.x -= background.getGroundSpeed() + score / 500;
            if (collidesWithPlayer(o.x, o.y, o.width, o.height)) {
                startDying(); return;
            }
        }

        // Nepřátelé
        enemyTimer++;
        if (enemyTimer >= nextEnemyIn) {
            enemyTimer  = 0;
            nextEnemyIn = 250 + random.nextInt(200);
            spawnEnemy();
        }
        enemies.removeIf(e -> !e.isActive());
        for (Enemy e : enemies) {
            e.update();
            if (e.getHitbox().intersects(getPlayerHitbox())) {
                startDying(); return;
            }
        }

        // Skóre
        scoreTimer++;
        if (scoreTimer >= 6) { scoreTimer = 0; score++; }
    }

    private void updateDying() {
        // Přehrává animaci smrti snímek po snímku
        frameTimer++;
        if (frameTimer >= DEATH_FRAME_DELAY) {
            frameTimer = 0;
            if (frameIndex < deathFrameCount - 1) {
                // Ještě nejsme na konci animace — pokračuj
                frameIndex++;
            } else {
                // Animace dokončena — přejdi do GAME_OVER
                state = GameState.GAME_OVER;
                if (score > highScore) highScore = score;
            }
        }
    }

    private void startDying() {
        state      = GameState.DYING;
        frameIndex = 0;
        frameTimer = 0;
    }

    private void spawnObstacle() {
        int type = random.nextInt(5);
        int w, h;
        BufferedImage img;
        switch (type) {
            case 0:  img = imgRock1; w = 60; h = 60;  break;
            case 1:  img = imgRock2; w = 70; h = 70;  break;
            case 2:  img = imgRock3; w = 70; h = 75;  break;
            case 3:  img = imgTree1; w = 60; h = 120; break;
            default: img = imgTree2; w = 70; h = 130; break;
        }
        obstacles.add(new Obstacle(WIDTH + 50, background.getGroundY() - h + 10, w, h, img));
    }

    private void spawnEnemy() {
        int flyHeight = background.getGroundY() - PLAYER_RENDER_HEIGHT - 80 - random.nextInt(100);
        int speed     = background.getGroundSpeed() + 1 + random.nextInt(3);
        enemies.add(new Enemy(WIDTH + 50, flyHeight, speed));
    }

    private Rectangle getPlayerHitbox() {
        int margin = 12;
        return new Rectangle(
                playerX + margin,
                playerY - PLAYER_RENDER_HEIGHT + margin,
                PLAYER_RENDER_WIDTH  - 2 * margin,
                PLAYER_RENDER_HEIGHT - 2 * margin);
    }

    private boolean collidesWithPlayer(int ox, int oy, int ow, int oh) {
        int margin = 10;
        return getPlayerHitbox().intersects(
                new Rectangle(ox + margin, oy + margin, ow - 2 * margin, oh - 2 * margin));
    }

    // =========================================================
    //  KRESLENÍ
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        background.draw(g2);

        switch (state) {
            case MENU:
                drawMenu(g2);
                break;
            case PLAYING:
                drawObstacles(g2);
                drawEnemies(g2);
                drawPlayer(g2);
                drawHUD(g2);
                break;
            case DYING:
                drawObstacles(g2);
                drawEnemies(g2);
                drawPlayer(g2);
                drawHUD(g2);
                break;
            case GAME_OVER:
                drawObstacles(g2);
                drawEnemies(g2);
                drawPlayer(g2);
                drawHUD(g2);
                drawGameOver(g2);
                break;
        }
    }

    private void drawPlayer(Graphics2D g) {
        int drawX = playerX;
        int drawY = playerY - PLAYER_RENDER_HEIGHT;

        BufferedImage sheet;
        int fw, fc, idx;

        switch (state) {
            case DYING:
            case GAME_OVER:
                sheet = imgOstrichDeath;
                fw    = deathFrameWidth;
                fc    = deathFrameCount;
                // Ve GAME_OVER zobraz poslední snímek
                idx = (state == GameState.GAME_OVER) ? fc - 1 : frameIndex;
                break;
            default:
                if (onGround) {
                    sheet = imgOstrichRun;
                    fw    = runFrameWidth;
                    fc    = runFrameCount;
                } else {
                    sheet = imgOstrichJump;
                    fw    = jumpFrameWidth;
                    fc    = jumpFrameCount;
                }
                idx = frameIndex;
                break;
        }

        if (sheet != null) {
            int srcX = idx * fw;
            if (srcX + fw > sheet.getWidth()) srcX = 0;
            g.drawImage(sheet,
                    drawX, drawY,
                    drawX + PLAYER_RENDER_WIDTH, drawY + PLAYER_RENDER_HEIGHT,
                    srcX, 0,
                    srcX + fw, sheet.getHeight(),
                    null);
        } else {
            g.setColor(new Color(100, 80, 160));
            g.fillRect(drawX, drawY, PLAYER_RENDER_WIDTH, PLAYER_RENDER_HEIGHT);
        }
    }

    private void drawObstacles(Graphics2D g) {
        for (Obstacle o : obstacles) {
            if (o.image != null) g.drawImage(o.image, o.x, o.y, o.width, o.height, null);
            else { g.setColor(Color.DARK_GRAY); g.fillRect(o.x, o.y, o.width, o.height); }
        }
    }

    private void drawEnemies(Graphics2D g) {
        for (Enemy e : enemies) e.draw(g);
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.drawString("SKÓRE: " + score,    30,          50);
        g.drawString("MAX: "   + highScore, WIDTH - 220, 50);
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 72));
        drawCentered(g, "PŠTROS RUNNER", HEIGHT / 2 - 60);
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        drawCentered(g, "Stiskni MEZERNÍK nebo ŠIPKU NAHORU pro start", HEIGHT / 2 + 30);
        if (highScore > 0) {
            g.setFont(new Font("Monospaced", Font.BOLD, 26));
            drawCentered(g, "Rekord: " + highScore, HEIGHT / 2 + 90);
        }
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 64));
        drawCentered(g, "KONEC HRY", HEIGHT / 2 - 50);
        g.setFont(new Font("Monospaced", Font.PLAIN, 30));
        drawCentered(g, "Skóre: " + score,      HEIGHT / 2 + 20);
        drawCentered(g, "Stiskni R pro restart", HEIGHT / 2 + 70);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, y);
    }

    // =========================================================
    //  KLÁVESNICE
    // =========================================================
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch (state) {
            case MENU:
                if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_UP) startGame();
                break;
            case PLAYING:
                if ((k == KeyEvent.VK_SPACE || k == KeyEvent.VK_UP) && onGround) {
                    velocityY  = JUMP_FORCE;
                    onGround   = false;
                    frameIndex = 0;
                }
                break;
            case GAME_OVER:
                if (k == KeyEvent.VK_R) startGame();
                break;
            default:
                break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    // =========================================================
    //  RESET
    // =========================================================
    private void startGame() {
        obstacles.clear();
        enemies.clear();
        playerY        = background.getGroundY();
        velocityY      = 0;
        onGround       = true;
        score          = 0;
        scoreTimer     = 0;
        obstacleTimer  = 0;
        nextObstacleIn = 90;
        enemyTimer     = 0;
        nextEnemyIn    = 300;
        frameIndex     = 0;
        frameTimer     = 0;
        state          = GameState.PLAYING;
    }

    // =========================================================
    //  VNITŘNÍ TŘÍDA — Překážka
    // =========================================================
    private static class Obstacle {
        int x, y, width, height;
        BufferedImage image;

        Obstacle(int x, int y, int w, int h, BufferedImage img) {
            this.x = x; this.y = y;
            this.width = w; this.height = h;
            this.image = img;
        }
    }
}