

import javax.swing.*;

public class Game extends JFrame {

    public Game() {
        setTitle("Ostrich run");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setVisible(true);

        GameFrame gameFrame = new GameFrame();
        add(gameFrame);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}