import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {

    public Game() {
        setTitle("Ostrich run");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);


        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenW = screen.width;
        int screenH = screen.height;

        setSize(screenW, screenH);
        setLocationRelativeTo(null);

        GameFrame gameFrame = new GameFrame(screenW, screenH);
        add(gameFrame);

        setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}