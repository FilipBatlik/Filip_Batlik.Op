package Entita;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;


public class Rabbit {


    public static final int RENDER_W = 72;
    public static final int RENDER_H = 72;

    public static final double GRAVITY    =  0.55;
    public static final double JUMP_FORCE = -22.0;

    public static final int JUMP_COOLDOWN = 100;

    private int jumpCooldownTimer = 0;



    private BufferedImage sheetRun;
    private BufferedImage sheetIdle;
    private BufferedImage sheetJump;
    private BufferedImage sheetDeath;

    private int runFrameCount   = 8;
    private int runFrameW       = 16;
    private int idleFrameCount  = 4;
    private int idleFrameW      = 16;
    private int jumpFrameCount  = 2;
    private int jumpFrameW      = 36;
    private int deathFrameCount = 2;
    private int deathFrameW     = 36;


    private int frameIndex = 0;
    private int frameTimer = 0;
    private static final int FRAME_DELAY       = 6;
    private static final int DEATH_FRAME_DELAY = 8;


    private int     x;
    private int     y;
    private double  velocityY = 0;
    private boolean onGround  = true;


    public enum State { IDLE, RUN, JUMP, DYING, DEAD }
    private State state = State.RUN;


    public Rabbit(int x, int groundY) {
        this.x = x;
        this.y = groundY;
        loadImages();
    }


    private void loadImages() {

        sheetRun   = tryLoad("Run.png");
        sheetIdle  = tryLoad("Idle.png");

        sheetJump  = tryLoad("RabbitJump-Sheet.png");
        sheetDeath = tryLoad("RabbitDeath.png");


        if (sheetRun != null) {
            runFrameW     = sheetRun.getHeight();
            runFrameCount = Math.max(1, sheetRun.getWidth() / runFrameW);
            System.out.println("Run.png  → " + runFrameCount + " frames, frameW=" + runFrameW);
        }
        if (sheetIdle != null) {
            idleFrameW     = sheetIdle.getHeight();
            idleFrameCount = Math.max(1, sheetIdle.getWidth() / idleFrameW);
            System.out.println("Idle.png → " + idleFrameCount + " frames, frameW=" + idleFrameW);
        }
        if (sheetJump != null) {
            jumpFrameW     = sheetJump.getHeight();
            jumpFrameCount = Math.max(1, sheetJump.getWidth() / jumpFrameW);
        }
        if (sheetDeath != null) {
            deathFrameW     = sheetDeath.getHeight();
            deathFrameCount = Math.max(1, sheetDeath.getWidth() / deathFrameW);
        }
    }

    private BufferedImage tryLoad(String name) {
        URL url = getClass().getClassLoader().getResource(name);
        if (url != null) {
            try { return ImageIO.read(url); }
            catch (IOException e) { System.err.println("Rabbit sprite error: " + e.getMessage()); }
        }
        String[] paths = { "src/main/resources/" + name, "resources/" + name, "assets/" + name, name };
        for (String path : paths) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                try { return ImageIO.read(f); }
                catch (IOException ignored) {}
            }
        }
        System.err.println("Rabbit sprite NOT FOUND: " + name);
        return null;
    }



    public void startIdle() {
        if (state == State.DYING || state == State.DEAD) return;
        state      = State.IDLE;
        frameIndex = 0;
        frameTimer = 0;
    }

    public boolean jump() {
        if (onGround && (state == State.RUN || state == State.IDLE) && jumpCooldownTimer <= 0) {
            velocityY         = JUMP_FORCE;
            onGround          = false;
            state             = State.JUMP;
            frameIndex        = 0;
            frameTimer        = 0;
            jumpCooldownTimer = JUMP_COOLDOWN;
            return true;
        }
        return false;
    }

    public int   getJumpCooldown()    { return jumpCooldownTimer; }
    public float getCooldownFraction(){ return (float) jumpCooldownTimer / JUMP_COOLDOWN; }

    public void startDying() {
        if (state == State.DYING || state == State.DEAD) return;
        state      = State.DYING;
        frameIndex = 0;
        frameTimer = 0;
    }

    public void reset(int groundY) {
        this.y            = groundY;
        velocityY         = 0;
        onGround          = true;
        state             = State.RUN;
        frameIndex        = 0;
        frameTimer        = 0;
        jumpCooldownTimer = 0;
    }




    public void update(int groundY) {
        switch (state) {
            case IDLE:
                updateIdle();
                break;
            case RUN:
            case JUMP:
                updateMovement(groundY);
                break;
            case DYING:
                updateDying();
                break;
            case DEAD:
                break;
        }
    }

    private void updateIdle() {
        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            frameIndex = (frameIndex + 1) % Math.max(idleFrameCount, 1);
        }
    }

    private void updateMovement(int groundY) {
        if (jumpCooldownTimer > 0) jumpCooldownTimer--;

        if (!onGround) {
            velocityY += GRAVITY;
            y += (int) velocityY;
        }

        if (y >= groundY) {
            y         = groundY;
            velocityY = 0;
            onGround  = true;
            state     = State.RUN;
        }

        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            int count = (state == State.JUMP) ? jumpFrameCount : runFrameCount;
            frameIndex = (frameIndex + 1) % Math.max(count, 1);
        }
    }

    private void updateDying() {
        frameTimer++;
        if (frameTimer >= DEATH_FRAME_DELAY) {
            frameTimer = 0;
            if (frameIndex < deathFrameCount - 1) {
                frameIndex++;
            } else {
                state = State.DEAD;
            }
        }
    }




    public void draw(Graphics2D g) {
        int drawX = x;
        int drawY = y - RENDER_H;

        BufferedImage sheet;
        int fw, idx;

        switch (state) {
            case IDLE:
                sheet = (sheetIdle != null) ? sheetIdle : sheetRun;
                fw    = (sheetIdle != null) ? idleFrameW : runFrameW;
                idx   = frameIndex;
                break;

            case DYING:
                sheet = sheetDeath;
                fw    = deathFrameW;
                idx   = frameIndex;
                break;

            case DEAD:
                sheet = sheetDeath;
                fw    = deathFrameW;
                idx   = deathFrameCount - 1;
                break;

            case JUMP:

                if (sheetJump != null) {
                    sheet = sheetJump;
                    fw    = jumpFrameW;
                } else {
                    sheet = sheetRun;
                    fw    = runFrameW;
                }
                idx = frameIndex;
                break;

            default:
                sheet = sheetRun;
                fw    = runFrameW;
                idx   = frameIndex;
                break;
        }

        if (sheet != null) {
            int srcX = idx * fw;
            if (srcX + fw > sheet.getWidth()) srcX = 0;
            g.drawImage(sheet,
                    drawX, drawY,
                    drawX + RENDER_W, drawY + RENDER_H,
                    srcX, 0, srcX + fw, sheet.getHeight(),
                    null);
        } else {

            g.setColor(new Color(230, 200, 170));
            g.fillRoundRect(drawX, drawY, RENDER_W, RENDER_H, 20, 20);
            g.setColor(Color.PINK);
            g.fillOval(drawX + RENDER_W / 2 - 8, drawY - 14, 8, 18);
            g.fillOval(drawX + RENDER_W / 2 + 2, drawY - 14, 8, 18);
        }
    }




    public Rectangle getHitbox() {
        int margin = 10;
        return new Rectangle(
                x + margin,
                y - RENDER_H + margin,
                RENDER_W  - 2 * margin,
                RENDER_H  - 2 * margin);
    }

    public boolean isOnGround()  { return onGround; }
    public boolean isDying()     { return state == State.DYING; }
    public boolean isDead()      { return state == State.DEAD; }
    public int     getX()        { return x; }
    public int     getY()        { return y; }
    public State   getState()    { return state; }
}