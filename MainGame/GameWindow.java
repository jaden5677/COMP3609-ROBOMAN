package MainGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameWindow extends JFrame implements ActionListener {
    private GamePanel gamePanel;
    private JLabel statusL;
    private JButton startButton, PauseButton, ContinueButton, ExitButton;
    private JPanel buttonPanel;
    private JTextField PlayerStatus;

    public GameWindow() {
        setTitle("My Game");
        setSize(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT));
        add(gamePanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public void startGame() {
        setVisible(true);
        gamePanel.requestFocusInWindow();
        gamePanel.startGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
