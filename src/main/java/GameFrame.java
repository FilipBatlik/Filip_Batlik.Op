import UI.Background;
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

    private enum GameState { MENU, PLAYING, GAME_OVER }
    private GameState state = GameState.MENU;

    private final Timer timer;
    private final Background background;

    private BufferedImage imgOstrichSheet;
    private BufferedImage imgOstrichHead;
    private BufferedImage imgRock1;
    private BufferedImage imgRock2;
    private BufferedImage imgRock3;
    private BufferedImage imgTree1;
    private BufferedImage imgTree2;

    private int playerX      = 200;
    private int playerY;
    private int playerWidth  = 64;
    private int playerHeight = 80;
    private double velocityY = 0;
    private boolean onGround = true;
    private static final double GRAVITY    = 0.8;
    private static final double JUMP_FORCE = -16;

    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY = 6;
    private static final int FRAME_COUNT = 8;
    private static final int FRAME_WIDTH = 64;

    private final List<Obstacle> obstacles = new ArrayList<>();
    private int obstacleTimer  = 0;
    private int nextObstacleIn = 90;
    private final Random random = new Random();

    private int score      = 0;
    private int highScore  = 0;
    private int scoreTimer = 0;

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
    //  NAČTENÍ OBRÁZKŮ — přes classpath (resources složka)
    // =========================================================
    private void loadImages() {
        imgOstrichSheet = tryLoad("OstrichRun.png");
        imgOstrichHead  = tryLoad("Ostrich with its head down.png");
        imgRock1        = tryLoad("Rock1.png");
        imgRock2        = tryLoad("Rock 2.png");
        imgRock3        = tryLoad("Rock3.png");
        imgTree1        = tryLoad("tree1.png");
        imgTree2        = tryLoad("tree2.png");
    }

    private BufferedImage tryLoad(String name) {
        // 1. Classpath — resources Root v IntelliJ
        URL url = getClass().getClassLoader().getResource(name);
        if (url != null) {
            try {
                System.out.println("Načten: " + url);
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("Chyba čtení: " + e.getMessage());
            }
        }

        // 2. Záloha — relativní cesta na disku
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

    // =========================================================
    //  HERNÍ SMYČKA
    // =========================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.PLAYING) update();
        repaint();
    }

    private void update() {
        background.update();

        // Fyzika hráče
        if (!onGround) velocityY += GRAVITY;
        playerY += (int) velocityY;

        if (playerY >= background.getGroundY()) {
            playerY   = background.getGroundY();
            velocityY = 0;
            onGround  = true;
        }

        // Animace
        if (onGround) {
            frameTimer++;
            if (frameTimer >= FRAME_DELAY) {
                frameTimer = 0;
                frameIndex = (frameIndex + 1) % FRAME_COUNT;
            }
        } else {
            frameIndex = 1;
        }

        // Spawn překážek
        obstacleTimer++;
        if (obstacleTimer >= nextObstacleIn) {
            obstacleTimer  = 0;
            nextObstacleIn = 80 + random.nextInt(120);
            spawnObstacle();
        }

        // Pohyb + kolize
        obstacles.removeIf(o -> o.x + o.width < 0);
        for (Obstacle o : obstacles) {
            o.x -= background.getGroundSpeed() + score / 500;
            if (collides(o)) {
                state = GameState.GAME_OVER;
                if (score > highScore) highScore = score;
                return;
            }
        }

        // Skóre
        scoreTimer++;
        if (scoreTimer >= 6) {
            scoreTimer = 0;
            score++;
        }
    }

    private void spawnObstacle() {
        int type = random.nextInt(5);
        int w, h;
        BufferedImage img;
        switch (type) {
            case 0:  img = imgRock1; w = 48; h = 48;  break;
            case 1:  img = imgRock2; w = 56; h = 56;  break;
            case 2:  img = imgRock3; w = 56; h = 60;  break;
            case 3:  img = imgTree1; w = 50; h = 100; break;
            default: img = imgTree2; w = 60; h = 110; break;
        }
        obstacles.add(new Obstacle(WIDTH + 50, background.getGroundY() - h + 10, w, h, img));
    }

    private boolean collides(Obstacle o) {
        int margin = 10;
        Rectangle pRect = new Rectangle(playerX + margin, playerY - playerHeight + margin,
                playerWidth - 2 * margin, playerHeight - 2 * margin);
        Rectangle oRect = new Rectangle(o.x + margin, o.y + margin,
                o.width - 2 * margin, o.height - 2 * margin);
        return pRect.intersects(oRect);
    }

    // =========================================================
    //  KRESLENÍ
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        background.draw(g2);

        switch (state) {
            case MENU:
                drawMenu(g2);
                break;
            case PLAYING:
                drawObstacles(g2);
                drawPlayer(g2);
                drawHUD(g2);
                break;
            case GAME_OVER:
                drawObstacles(g2);
                drawPlayer(g2);
                drawHUD(g2);
                drawGameOver(g2);
                break;
        }
    }

    private void drawObstacles(Graphics2D g) {
        for (Obstacle o : obstacles) {
            if (o.image != null) {
                g.drawImage(o.image, o.x, o.y, o.width, o.height, null);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(o.x, o.y, o.width, o.height);
            }
        }
    }

    private void drawPlayer(Graphics2D g) {
        int drawX = playerX;
        int drawY = playerY - playerHeight;

        if (state == GameState.GAME_OVER && imgOstrichHead != null) {
            g.drawImage(imgOstrichHead, drawX, drawY, playerWidth, playerHeight, null);
            return;
        }

        if (imgOstrichSheet != null) {
            int srcX   = frameIndex * FRAME_WIDTH;
            int sheetW = imgOstrichSheet.getWidth();
            if (srcX + FRAME_WIDTH > sheetW) srcX = 0;
            g.drawImage(imgOstrichSheet,
                    drawX, drawY, drawX + playerWidth, drawY + playerHeight,
                    srcX, 0, srcX + FRAME_WIDTH, imgOstrichSheet.getHeight(), null);
        } else {
            g.setColor(new Color(100, 80, 160));
            g.fillRect(drawX, drawY, playerWidth, playerHeight);
        }
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
                    velocityY = JUMP_FORCE;
                    onGround  = false;
                }
                break;
            case GAME_OVER:
                if (k == KeyEvent.VK_R) startGame();
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
        playerY        = background.getGroundY();
        velocityY      = 0;
        onGround       = true;
        score          = 0;
        scoreTimer     = 0;
        obstacleTimer  = 0;
        nextObstacleIn = 90;
        frameIndex     = 0;
        state          = GameState.PLAYING;
    }

    // =========================================================
    //  VNITŘNÍ TŘÍDA
    // =========================================================
    private static class Obstacle {
        int x, y, width, height;
        BufferedImage image;

        Obstacle(int x, int y, int w, int h, BufferedImage img) {
            this.x      = x;
            this.y      = y;
            this.width  = w;
            this.height = h;
            this.image  = img;
        }
    }
}