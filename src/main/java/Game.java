

import javax.swing.*;

public class Game extends JFrame {

    public Game() {
        setTitle("Hra");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 832);

        GameFrame gameFrame = new GameFrame();
        add(gameFrame);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}