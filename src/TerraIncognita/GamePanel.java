package TerraIncognita;

import TerraIncognita.util.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Panel chính chứa game loop.
 * Extends JPanel, implements Runnable để chạy vòng lặp game trên thread riêng.
 *
 * Trách nhiệm:
 * - Chạy game loop (~60 FPS)
 * - Gọi update() cập nhật logic mỗi frame
 * - Gọi paintComponent() vẽ mọi thứ lên màn hình
 */
public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private GameEngine gameEngine;
    private InputHandler inputHandler;
    private volatile boolean running;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        // Khởi tạo InputHandler và lắng nghe phím
        inputHandler = new InputHandler();
        addKeyListener(inputHandler);

        // Khởi tạo GameEngine
        gameEngine = new GameEngine(inputHandler);
    }

    /**
     * Bắt đầu game loop trên thread mới.
     */
    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Game loop chính — dùng delta time để đảm bảo tốc độ nhất quán
        double targetTime = 1_000_000_000.0 / Constants.TARGET_FPS; // nano giây / frame
        long previousTime = System.nanoTime();
        double deltaAccumulator = 0;

        // Đo FPS
        int frameCount = 0;
        long fpsTimer = System.currentTimeMillis();

        while (running) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - previousTime;
            previousTime = currentTime;

            deltaAccumulator += elapsed;

            // Update logic theo fixed timestep
            while (deltaAccumulator >= targetTime) {
                double deltaTime = targetTime / 1_000_000_000.0; // chuyển sang giây
                update(deltaTime);
                deltaAccumulator -= targetTime;
            }

            // Render
            repaint();
            frameCount++;

            // Hiển thị FPS mỗi giây (debug)
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                // System.out.println("FPS: " + frameCount);
                frameCount = 0;
                fpsTimer = System.currentTimeMillis();
            }

            // Sleep ngắn để không chiếm hết CPU
            try {
                long sleepTime = (long) ((targetTime - (System.nanoTime() - currentTime)) / 1_000_000);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cập nhật logic game mỗi frame.
     * 
     * @param deltaTime thời gian (giây) từ frame trước
     */
    public void update(double deltaTime) {
        // Cập nhật input handler (để isKeyJustPressed hoạt động đúng)
        inputHandler.update();
        // Cập nhật game engine
        gameEngine.update(deltaTime);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Bật anti-aliasing cho text đẹp hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Vẽ game
        gameEngine.render(g2d);

        g2d.dispose();
    }
}
