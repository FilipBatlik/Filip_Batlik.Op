package Menu;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {

    public MenuPanel(Runnable onStart) {

        setLayout(new BorderLayout());
        setBackground(Color.gray);
        JLabel title = new JLabel("Flappy ostrich", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 72));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(150, 0, 100, 0));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.GRAY);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 32));
        startButton.setFocusPainted(false);
        startButton.setBackground(Color.WHITE);
        startButton.setForeground(Color.BLACK);
        startButton.setPreferredSize(new Dimension(250, 80));






        startButton.addActionListener(e -> {
            if (onStart != null) {
                onStart.run();
            }
        });

        buttonPanel.add(startButton);

        // ===== Add to layout =====
        add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);


    }
}

