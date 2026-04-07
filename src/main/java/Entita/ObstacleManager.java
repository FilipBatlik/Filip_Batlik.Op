package Entita;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ObstacleManager {

    private ArrayList<Obstacle> obstacles;
    private Random random = new Random();

    private int spawnTimer = 0;

    public ObstacleManager(ArrayList<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void update() {
        spawnTimer++;

        if (spawnTimer > 90) {
            spawnObstacle();
            spawnTimer = 0;
        }

        Iterator<Obstacle> it = obstacles.iterator();

        while (it.hasNext()) {
            Obstacle o = it.next();
            o.update();

            if (o.getX() < -50) {
                it.remove();
            }
        }
    }

    private void spawnObstacle() {
        obstacles.add(new Obstacle(800, 300));
    }
}