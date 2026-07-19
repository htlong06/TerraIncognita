package TerraIncognita;

import TerraIncognita.util.Constants;

import javax.swing.JFrame;

/**
 * Entry point của game Terra Incognita.
 * Khởi tạo JFrame, gắn GamePanel, chạy chương trình.
 */
public class Main {

    public static void main(String[] args) {
        // Tạo JFrame
        JFrame frame = new JFrame(Constants.GAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Tạo GamePanel và thêm vào JFrame
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();

        // Căn giữa cửa sổ trên màn hình
        frame.setLocationRelativeTo(null);

        // Hiển thị
        frame.setVisible(true);

        // Yêu cầu focus để nhận input từ bàn phím
        gamePanel.requestFocusInWindow();

        // Khởi chạy game loop
        gamePanel.startGameThread();
    }
}