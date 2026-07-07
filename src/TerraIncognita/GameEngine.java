package TerraIncognita;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Quản lý trạng thái game (State Machine).
 *
 * Các trạng thái: MENU → HUB → PLAYING (Dungeon) → RUN_SUMMARY → HUB → ... →
 * GAME_OVER
 *
 * Trách nhiệm:
 * - Chuyển đổi giữa các trạng thái (state transition)
 * - Gọi update/render đúng logic cho từng trạng thái
 * - Quản lý tài nguyên chung (player, map hiện tại...)
 */
public class GameEngine {

    private GameState currentState;
    private Player player;
    private InputHandler inputHandler;

    // TODO (GĐ2): GameMap currentMap
    // TODO (GĐ3): AssetLoader assetLoader, Renderer renderer
    // TODO (GĐ4): CombatSystem combatSystem
    // TODO (GĐ5): EventSystem eventSystem
    // TODO (GĐ6): SaveManager saveManager

    public GameEngine(InputHandler inputHandler) {
        this.inputHandler = inputHandler;

        // Khởi tạo Player
        this.player = new Player();
        // Đặt player ở giữa màn hình (tạm thời, chưa có map)
        this.player.setWorldX(Constants.SCREEN_WIDTH / 2.0 - Constants.TILE_SIZE / 2.0);
        this.player.setWorldY(Constants.SCREEN_HEIGHT / 2.0 - Constants.TILE_SIZE / 2.0);

        // Set trạng thái ban đầu = PLAYING (tạm thời, bỏ qua MENU ở GĐ1)
        this.currentState = GameState.PLAYING;
    }

    /**
     * Cập nhật logic theo trạng thái hiện tại.
     */
    public void update(double deltaTime) {
        switch (currentState) {
            case MENU:
                updateMenu(deltaTime);
                break;
            case PLAYING:
                updatePlaying(deltaTime);
                break;
            case PAUSED:
                // Không update logic khi pause
                break;
            case GAME_OVER:
                updateGameOver(deltaTime);
                break;
            default:
                break;
        }
    }

    /**
     * Vẽ mọi thứ theo trạng thái hiện tại.
     */
    public void render(Graphics2D g2d) {
        switch (currentState) {
            case MENU:
                renderMenu(g2d);
                break;
            case PLAYING:
                renderPlaying(g2d);
                break;
            case PAUSED:
                renderPlaying(g2d); // Vẽ game phía sau
                renderPauseOverlay(g2d);
                break;
            case GAME_OVER:
                renderGameOver(g2d);
                break;
            default:
                break;
        }
    }

    /**
     * Chuyển sang trạng thái mới.
     */
    public void changeState(GameState newState) {
        this.currentState = newState;
    }

    // =========================================
    // UPDATE riêng cho từng state
    // =========================================

    private void updateMenu(double deltaTime) {
        // TODO (GĐ6): Xử lý menu input
        // Tạm thời: nhấn ENTER → chuyển sang PLAYING
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            changeState(GameState.PLAYING);
        }
    }

    private void updatePlaying(double deltaTime) {
        // Xử lý di chuyển player
        boolean moved = false;

        if (inputHandler.isKeyPressed(KeyEvent.VK_UP) || inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            player.move(Direction.UP, deltaTime);
            moved = true;
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN) || inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            player.move(Direction.DOWN, deltaTime);
            moved = true;
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_LEFT) || inputHandler.isKeyPressed(KeyEvent.VK_A)) {
            player.move(Direction.LEFT, deltaTime);
            moved = true;
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_RIGHT) || inputHandler.isKeyPressed(KeyEvent.VK_D)) {
            player.move(Direction.RIGHT, deltaTime);
            moved = true;
        }

        if (!moved) {
            player.setIdle();
        }

        // Xử lý pause
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            changeState(GameState.PAUSED);
        }

        // Cập nhật player
        player.update(deltaTime);
    }

    private void updateGameOver(double deltaTime) {
        // Nhấn ENTER → quay lại menu
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            changeState(GameState.MENU);
        }
    }

    // =========================================
    // RENDER riêng cho từng state
    // =========================================

    private void renderMenu(Graphics2D g2d) {
        // Nền đen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(32f));
        String title = Constants.GAME_TITLE;
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (Constants.SCREEN_WIDTH - titleWidth) / 2, Constants.SCREEN_HEIGHT / 3);

        // Hướng dẫn
        g2d.setFont(g2d.getFont().deriveFont(16f));
        String hint = "Nhan ENTER de bat dau";
        int hintWidth = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (Constants.SCREEN_WIDTH - hintWidth) / 2, Constants.SCREEN_HEIGHT / 2);
    }

    private void renderPlaying(Graphics2D g2d) {
        // TODO (GĐ2): Vẽ map bằng Renderer
        // Nền tối (tạm thời mô phỏng dungeon)
        g2d.setColor(new Color(30, 30, 40));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Vẽ lưới tile nhạt (debug, để thấy rõ hơn)
        g2d.setColor(new Color(50, 50, 60));
        for (int x = 0; x < Constants.SCREEN_WIDTH; x += Constants.TILE_SIZE) {
            g2d.drawLine(x, 0, x, Constants.SCREEN_HEIGHT);
        }
        for (int y = 0; y < Constants.SCREEN_HEIGHT; y += Constants.TILE_SIZE) {
            g2d.drawLine(0, y, Constants.SCREEN_WIDTH, y);
        }

        // Vẽ Player (hình vuông màu xanh dương)
        // TODO (GĐ3): Thay bằng sprite animation
        int px = (int) player.getWorldX();
        int py = (int) player.getWorldY();

        // Thân player
        g2d.setColor(new Color(70, 130, 230));
        g2d.fillRect(px + 2, py + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);

        // Viền sáng
        g2d.setColor(new Color(120, 180, 255));
        g2d.drawRect(px + 2, py + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);

        // Mắt nhỏ (cho biết hướng nhìn)
        g2d.setColor(Color.WHITE);
        int eyeSize = 4;
        int eyeOffX = Constants.TILE_SIZE / 2 - eyeSize / 2;
        int eyeOffY = Constants.TILE_SIZE / 2 - eyeSize / 2;
        switch (player.getDirection()) {
            case UP:
                eyeOffY -= 5;
                break;
            case DOWN:
                eyeOffY += 5;
                break;
            case LEFT:
                eyeOffX -= 5;
                break;
            case RIGHT:
                eyeOffX += 5;
                break;
        }
        g2d.fillOval(px + eyeOffX, py + eyeOffY, eyeSize, eyeSize);

        // HUD tạm (góc trên trái)
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(14f));
        g2d.drawString("HP: " + player.getHp() + "/" + player.getMaxHp(), 10, 20);
        g2d.drawString("Pos: (" + player.getTileX() + ", " + player.getTileY() + ")", 10, 40);
        g2d.drawString("State: " + player.getState(), 10, 60);

        // Hướng dẫn điều khiển
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(g2d.getFont().deriveFont(12f));
    }

    private void renderPauseOverlay(Graphics2D g2d) {
        // Overlay mờ
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Text PAUSED
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(48f));
        String text = "PAUSED";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, Constants.SCREEN_HEIGHT / 2);

        g2d.setFont(g2d.getFont().deriveFont(16f));
        String hint = "ESC de tiep tuc";
        int hintWidth = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (Constants.SCREEN_WIDTH - hintWidth) / 2, Constants.SCREEN_HEIGHT / 2 + 40);
    }

    private void renderGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(50, 10, 10));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g2d.setColor(new Color(220, 50, 50));
        g2d.setFont(g2d.getFont().deriveFont(48f));
        String text = "GAME OVER";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, Constants.SCREEN_HEIGHT / 2);
    }

    // --- Getter ---
    public GameState getCurrentState() {
        return currentState;
    }

    public Player getPlayer() {
        return player;
    }
}
