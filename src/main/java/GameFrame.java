import Entita.Falcon;
import Entita.Rabbit;
import UI.Background;
import Obstacle.ObstacleManager;

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

    private final int WIDTH;
    private final int HEIGHT;

    private static final int FPS = 60;


    private static final int OSTRICH_W = 96;
    private static final int OSTRICH_H = 96;

    private BufferedImage imgOstrichRun;
    private BufferedImage imgOstrichJump;
    private BufferedImage imgOstrichDeath;
    private int runFrameCount = 4, runFrameW = 16;
    private int jumpFrameCount = 2, jumpFrameW = 16;
    private int deathFrameCount = 1, deathFrameW = 16;


    private static final double OSTRICH_GRAVITY    = 0.8;
    private static final double OSTRICH_JUMP_FORCE = -18;

    private enum GameState { MENU, CHAR_SELECT, PLAYING, DYING, GAME_OVER }
    private GameState state = GameState.MENU;


    private enum CharChoice { OSTRICH, RABBIT }
    private CharChoice chosenChar = CharChoice.OSTRICH;


    private final Timer           gameTimer;
    private final Background      background;
    private final ObstacleManager obstacleManager;

    private Rabbit rabbit;


    private final int playerX = 200;
    private int       playerY;
    private double    velocityY = 0;
    private boolean   onGround  = true;


    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY       = 6;
    private static final int DEATH_FRAME_DELAY = 8;


    private final List<Falcon> falcons    = new ArrayList<>();
    private int enemyTimer  = 0;
    private int nextEnemyIn = 300;

    private final Random random = new Random();


    private int score      = 0;
    private int highScore  = 0;
    private int scoreTimer = 0;


    public GameFrame(int width, int height) {
        this.WIDTH  = width;
        this.HEIGHT = height;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        background      = new Background(WIDTH, HEIGHT);
        playerY         = background.getGroundY();
        obstacleManager = new ObstacleManager(WIDTH, background.getGroundY(), background.getGroundSpeed());

        loadOstrichImages();

        gameTimer = new Timer(1000 / FPS, this);
        gameTimer.start();
    }


    private void loadOstrichImages() {
        imgOstrichRun   = tryLoad("OstrichRun.png");
        imgOstrichJump  = tryLoad("OstrichJump-Sheet.png");
        imgOstrichDeath = tryLoad("OstrichDeath.png");

        if (imgOstrichRun   != null) { runFrameW   = imgOstrichRun.getHeight();   runFrameCount   = imgOstrichRun.getWidth()   / runFrameW;   }
        if (imgOstrichJump  != null) { jumpFrameW  = imgOstrichJump.getHeight();  jumpFrameCount  = imgOstrichJump.getWidth()  / jumpFrameW;  }
        if (imgOstrichDeath != null) { deathFrameW = imgOstrichDeath.getHeight(); deathFrameCount = imgOstrichDeath.getWidth() / deathFrameW; }
    }

    private BufferedImage tryLoad(String name) {
        URL url = getClass().getClassLoader().getResource(name);
        if (url != null) {
            try { return ImageIO.read(url); }
            catch (IOException e) { System.err.println("Sprite error: " + e.getMessage()); }
        }
        String[] paths = { "src/main/resources/" + name, "resources/" + name, "assets/" + name, name };
        for (String p : paths) {
            java.io.File f = new java.io.File(p);
            if (f.exists()) try { return ImageIO.read(f); } catch (IOException ignored) {}
        }
        return null;
    }


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

        if (chosenChar == CharChoice.RABBIT) {

            rabbit.update(background.getGroundY());

            obstacleManager.update(score);
            if (obstacleManager.checkCollision(rabbit.getHitbox())) {
                rabbit.startDying();
                state = GameState.DYING;
                return;
            }

            falcons.removeIf(f -> !f.isActive());
            for (Falcon f : falcons) {
                f.update();
                if (f.getHitbox().intersects(rabbit.getHitbox())) {
                    rabbit.startDying();
                    state = GameState.DYING;
                    return;
                }
            }

        } else {

            if (!onGround) velocityY += OSTRICH_GRAVITY;
            playerY += (int) velocityY;
            if (playerY >= background.getGroundY()) {
                playerY   = background.getGroundY();
                velocityY = 0;
                onGround  = true;
            }

            frameTimer++;
            if (frameTimer >= FRAME_DELAY) {
                frameTimer = 0;
                frameIndex = (frameIndex + 1) % (onGround ? runFrameCount : jumpFrameCount);
            }

            obstacleManager.update(score);
            if (obstacleManager.checkCollision(getOstrichHitbox())) { startDying(); return; }

            falcons.removeIf(f -> !f.isActive());
            for (Falcon f : falcons) {
                f.update();
                if (f.getHitbox().intersects(getOstrichHitbox())) { startDying(); return; }
            }
        }


        enemyTimer++;
        if (enemyTimer >= nextEnemyIn) {
            enemyTimer  = 0;
            nextEnemyIn = 250 + random.nextInt(200);
            spawnFalcon();
        }

        scoreTimer++;
        if (scoreTimer >= 6) { scoreTimer = 0; score++; }
    }

    private void updateDying() {
        if (chosenChar == CharChoice.RABBIT) {
            rabbit.update(background.getGroundY());
            if (rabbit.isDead()) {
                state = GameState.GAME_OVER;
                if (score > highScore) highScore = score;
            }
        } else {
            frameTimer++;
            if (frameTimer >= DEATH_FRAME_DELAY) {
                frameTimer = 0;
                if (frameIndex < deathFrameCount - 1) {
                    frameIndex++;
                } else {
                    state = GameState.GAME_OVER;
                    if (score > highScore) highScore = score;
                }
            }
        }
    }

    private void startDying() {
        state      = GameState.DYING;
        frameIndex = 0;
        frameTimer = 0;
    }

    private void spawnFalcon() {
        int flyH  = background.getGroundY() - OSTRICH_H - 80 - random.nextInt(100);
        int speed = background.getGroundSpeed() + 1 + random.nextInt(3);
        falcons.add(new Falcon(WIDTH + 50, flyH, speed));
    }

    private Rectangle getOstrichHitbox() {
        int m = 12;
        return new Rectangle(playerX + m, playerY - OSTRICH_H + m, OSTRICH_W - 2*m, OSTRICH_H - 2*m);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        background.draw(g2);

        switch (state) {
            case MENU:        drawMenu(g2);       break;
            case CHAR_SELECT: drawCharSelect(g2); break;
            case PLAYING:
            case DYING:
                obstacleManager.draw(g2);
                drawFalcons(g2);
                drawCurrentPlayer(g2);
                drawHUD(g2);
                break;
            case GAME_OVER:
                obstacleManager.draw(g2);
                drawFalcons(g2);
                drawCurrentPlayer(g2);
                drawHUD(g2);
                drawGameOver(g2);
                break;
        }
    }

    private void drawFalcons(Graphics2D g) {
        for (Falcon f : falcons) f.draw(g);
    }

    private void drawCurrentPlayer(Graphics2D g) {
        if (chosenChar == CharChoice.RABBIT) {
            rabbit.draw(g);
        } else {
            drawOstrich(g);
        }
    }

    private void drawOstrich(Graphics2D g) {
        int drawX = playerX;
        int drawY = playerY - OSTRICH_H;

        BufferedImage sheet; int fw, fc, idx;
        switch (state) {
            case DYING: case GAME_OVER:
                sheet = imgOstrichDeath; fw = deathFrameW; fc = deathFrameCount;
                idx   = (state == GameState.GAME_OVER) ? fc - 1 : frameIndex;
                break;
            default:
                if (onGround) { sheet = imgOstrichRun;  fw = runFrameW;  fc = runFrameCount;  }
                else           { sheet = imgOstrichJump; fw = jumpFrameW; fc = jumpFrameCount; }
                idx = frameIndex;
        }

        if (sheet != null) {
            int srcX = Math.min(idx * fw, sheet.getWidth() - fw);
            g.drawImage(sheet, drawX, drawY, drawX + OSTRICH_W, drawY + OSTRICH_H,
                    srcX, 0, srcX + fw, sheet.getHeight(), null);
        } else {
            g.setColor(new Color(100, 80, 160));
            g.fillRect(drawX, drawY, OSTRICH_W, OSTRICH_H);
        }
    }


    private void drawHUD(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.setColor(Color.WHITE);
        g.drawString("SKÓRE: " + score,    30,          50);
        g.drawString("MAX: "   + highScore, WIDTH - 220, 50);


        if (chosenChar == CharChoice.RABBIT && rabbit != null) {
            drawCooldownBar(g);
        }
    }



    private void drawCooldownBar(Graphics2D g) {
        int cx = 60;
        int cy = HEIGHT - 70;
        int r  = 28;

        float fraction = rabbit.getCooldownFraction(); // 0.0 = ready, 1.0 = just jumped


        g.setColor(new Color(0, 0, 0, 140));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);


        if (fraction > 0f) {
            Color arcColor = fraction > 0.5f
                    ? new Color(220, 60, 60)
                    : new Color(220, 140, 30);
            g.setColor(arcColor);
            int arcDeg = (int)(fraction * 360);
            g.fillArc(cx - r, cy - r, r * 2, r * 2, 90, arcDeg);
        }


        g.setStroke(new BasicStroke(3f));
        g.setColor(fraction == 0f ? new Color(80, 220, 80) : new Color(200, 200, 200));
        g.drawOval(cx - r, cy - r, r * 2, r * 2);
        g.setStroke(new BasicStroke(1f));


        int iconX = cx - 6;
        int iconY = cy - 8;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("↑", iconX, iconY + 16);

        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        String label = fraction == 0f ? "READY" : String.format("%.1fs", fraction * Rabbit.JUMP_COOLDOWN / 60f);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, cx - fm.stringWidth(label) / 2, cy + r + 18);
    }


    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 72));
        drawCentered(g, "OSTRICH RUNNER", HEIGHT / 2 - 80);
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        drawCentered(g, "MEZERNÍK / ŠIPKA NAHORU – start", HEIGHT / 2 + 10);
        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        drawCentered(g, "Po stisknutí vyber postavu", HEIGHT / 2 + 60);
    }


    private void drawCharSelect(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Serif", Font.BOLD, 56));
        g.setColor(Color.WHITE);
        drawCentered(g, "VYBER POSTAVU", HEIGHT / 2 - 120);

        int boxW = 280, boxH = 180, gap = 80;
        int totalW = boxW * 2 + gap;
        int startX = (WIDTH - totalW) / 2;
        int boxY   = HEIGHT / 2 - 80;



        boolean ostrichHover = (chosenChar == CharChoice.OSTRICH);
        drawCharBox(g, startX, boxY, boxW, boxH, "PŠTROS", "Klasická postava", "Skok: bez CD", ostrichHover, new Color(100, 80, 200));



        boolean rabbitHover = (chosenChar == CharChoice.RABBIT);
        drawCharBox(g, startX + boxW + gap, boxY, boxW, boxH, "KRÁLÍK", "Vyšší skok!", "CD: 1.5 sekundy", rabbitHover, new Color(200, 80, 120));

        g.setFont(new Font("Monospaced", Font.PLAIN, 26));
        g.setColor(Color.WHITE);
        drawCentered(g, "← → pro výběr,  ENTER pro potvrzení", HEIGHT / 2 + 140);
    }

    private void drawCharBox(Graphics2D g, int x, int y, int w, int h,
                             String name, String line1, String line2,
                             boolean selected, Color accent) {

        g.setColor(selected ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200)
                : new Color(30, 30, 30, 160));
        g.fillRoundRect(x, y, w, h, 20, 20);


        g.setStroke(new BasicStroke(selected ? 4f : 2f));
        g.setColor(selected ? Color.WHITE : new Color(150, 150, 150));
        g.drawRoundRect(x, y, w, h, 20, 20);
        g.setStroke(new BasicStroke(1f));


        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 34));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, x + (w - fm.stringWidth(name)) / 2, y + 55);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        fm = g.getFontMetrics();
        g.setColor(new Color(220, 220, 220));
        g.drawString(line1, x + (w - fm.stringWidth(line1)) / 2, y + 95);
        g.setColor(selected ? new Color(255, 230, 100) : new Color(180, 180, 180));
        g.drawString(line2, x + (w - fm.stringWidth(line2)) / 2, y + 125);

        if (selected) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            fm = g.getFontMetrics();
            g.drawString("✓  VYBRÁNO", x + (w - fm.stringWidth("✓  VYBRÁNO")) / 2, y + h - 20);
        }
    }


    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 64));
        drawCentered(g, "KONEC HRY", HEIGHT / 2 - 50);
        g.setFont(new Font("Monospaced", Font.PLAIN, 30));
        drawCentered(g, "Skóre: " + score,              HEIGHT / 2 + 20);
        drawCentered(g, "R – restart  |  ESC – konec",  HEIGHT / 2 + 70);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, y);
    }


    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        switch (state) {
            case MENU:
                if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_UP) {
                    state = GameState.CHAR_SELECT;
                }
                break;

            case CHAR_SELECT:
                if (k == KeyEvent.VK_LEFT  || k == KeyEvent.VK_A) chosenChar = CharChoice.OSTRICH;
                if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) chosenChar = CharChoice.RABBIT;
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE)        startGame();
                if (k == KeyEvent.VK_ESCAPE) System.exit(0);
                break;

            case PLAYING:
                if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_UP) {
                    if (chosenChar == CharChoice.RABBIT) {
                        rabbit.jump();
                    } else {
                        if (onGround) {
                            velocityY  = OSTRICH_JUMP_FORCE;
                            onGround   = false;
                            frameIndex = 0;
                        }
                    }
                }
                if (k == KeyEvent.VK_ESCAPE) System.exit(0);
                break;

            case GAME_OVER:
                if (k == KeyEvent.VK_R)      { state = GameState.CHAR_SELECT; resetCommon(); }
                if (k == KeyEvent.VK_ESCAPE)  System.exit(0);
                break;

            default: break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}


    private void startGame() {
        resetCommon();

        if (chosenChar == CharChoice.RABBIT) {
            rabbit = new Rabbit(playerX, background.getGroundY());
        } else {
            rabbit    = null;
            playerY   = background.getGroundY();
            velocityY = 0;
            onGround  = true;
        }

        state = GameState.PLAYING;
    }

    private void resetCommon() {
        obstacleManager.reset();
        falcons.clear();
        score       = 0;
        scoreTimer  = 0;
        enemyTimer  = 0;
        nextEnemyIn = 400;
        frameIndex  = 0;
        frameTimer  = 0;
    }
}