package utils;

import java.io.*;
import java.util.Scanner;


public class Score {

    private int currentScore;
    private int highScore;
    private final String fileName = "highscore.txt";

    public Score() {
        this.currentScore = 0;
        this.highScore = loadHighScore();
    }


    public void incrementScore(int amount) {
        currentScore += amount;
        if (currentScore > highScore) {
            highScore = currentScore;
        }
    }


    public void saveHighScore() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(highScore);
        } catch (IOException e) {
            System.err.println("Chyba při ukládání highscore");
        }
    }


    private int loadHighScore() {
        File file = new File(fileName);
        if (!file.exists()) return 0;

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
        } catch (FileNotFoundException e) {
            return 0;
        }
        return 0;
    }

    public void resetCurrentScore() {
        this.currentScore = 0;
    }


    public int getCurrentScore() { return currentScore; }
    public int getHighScore() { return highScore; }
}