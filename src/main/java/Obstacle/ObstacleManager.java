package Obstacle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObstacleManager {

    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Random random = new Random();

    private int timer     = 0;
    private int nextSpawn = 90;

    private final int screenWidth;
    private final int groundY;
    private final int baseSpeed;

    private static final int ANIMAL_COUNT = 7;

    public ObstacleManager(int screenWidth, int groundY, int baseSpeed) {
        this.screenWidth = screenWidth;
        this.groundY     = groundY;
        this.baseSpeed   = baseSpeed;
        Obstacle.loadSheet();
    }

    // =========================================================
    //  UPDATE — volat každý tick
    // =========================================================
    public void update(int score) {
        int speed = baseSpeed + score / 500;

        timer++;
        if (timer >= nextSpawn) {
            timer     = 0;
            nextSpawn = 80 + random.nextInt(120);
            int spriteIndex = random.nextInt(ANIMAL_COUNT);
            obstacles.add(new Obstacle(screenWidth + 50, groundY, spriteIndex, speed));
        }

        obstacles.removeIf(o -> !o.isActive());
        for (Obstacle o : obstacles) {
            o.update();
        }
    }

    // =========================================================
    //  DRAW — volat v paintComponent()
    // =========================================================
    public void draw(Graphics g) {
        for (Obstacle o : obstacles) {
            o.draw(g);
        }
    }

    // =========================================================
    //  KOLIZE — vrátí true pokud se hráč dotkl překážky
    // =========================================================
    public boolean checkCollision(Rectangle playerHitbox) {
        for (Obstacle o : obstacles) {
            if (o.getHitbox().intersects(playerHitbox)) {
                return true;
            }
        }
        return false;
    }

    // =========================================================
    //  RESET
    // =========================================================
    public void reset() {
        obstacles.clear();
        timer     = 0;
        nextSpawn = 90;
    }
}