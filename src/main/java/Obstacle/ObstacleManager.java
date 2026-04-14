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

    public ObstacleManager(int screenWidth, int groundY, int baseSpeed) {
        this.screenWidth = screenWidth;
        this.groundY     = groundY;
        this.baseSpeed   = baseSpeed;
        Obstacle.loadSprites();
    }


    public void update(int score) {
        int speed = baseSpeed + score / 500;

        timer++;
        if (timer >= nextSpawn) {
            timer     = 0;
            nextSpawn = 80 + random.nextInt(120);


            Obstacle.Type type;
            switch (random.nextInt(3)) {
                case 0:  type = Obstacle.Type.FLY;    break;
                case 1:  type = Obstacle.Type.ATTACK;  break;
                default: type = Obstacle.Type.IDLE;    break;
            }

            obstacles.add(new Obstacle(screenWidth + 50, groundY, type, speed));
        }

        obstacles.removeIf(o -> !o.isActive());
        for (Obstacle o : obstacles) o.update();
    }

    public void draw(Graphics g) {
        for (Obstacle o : obstacles) o.draw(g);
    }


    public boolean checkCollision(Rectangle playerHitbox) {
        for (Obstacle o : obstacles) {
            if (o.getHitbox().intersects(playerHitbox)) return true;
        }
        return false;
    }


    public void reset() {
        obstacles.clear();
        timer     = 0;
        nextSpawn = 90;
    }
}