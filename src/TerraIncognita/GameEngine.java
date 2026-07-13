package TerraIncognita;

import TerraIncognita.combat.CombatSystem;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.monster.Monster;
import TerraIncognita.entity.monster.SkeletonMonster;
import TerraIncognita.entity.monster.SlimeMonster;
import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.ui.InventoryUI;
import TerraIncognita.util.Constants;
 
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private InventoryUI inventoryUI;

    private AssetLoader assetLoader;

    // TODO (GĐ2): GameMap currentMap
    // TODO (GĐ3): AssetLoader assetLoader, Renderer renderer
    // TODO (GĐ4): CombatSystem combatSystem
    // TODO (GĐ5): EventSystem eventSystem
    // TODO (GĐ6): SaveManager saveManager

    public GameEngine(InputHandler inputHandler) {
        this.inputHandler = inputHandler;

        this.player = new Player();
        this.player.setWorldX(Constants.SCREEN_WIDTH / 2.0 - Constants.TILE_SIZE / 2.0);
        this.player.setWorldY(Constants.SCREEN_HEIGHT / 2.0 - Constants.TILE_SIZE / 2.0);

        this.assetLoader = new AssetLoader();
        this.assetLoader.loadAll();
        this.player.initAnimations(assetLoader);

        this.currentState = GameState.PLAYING;

        // Inventory UI
        this.inventoryUI = new InventoryUI();
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
            case INVENTORY:
                updateInventory(deltaTime);
                break;
            case PAUSED:
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
            case INVENTORY:
                renderPlaying(g2d);
                inventoryUI.render(g2d, player.getInventory(), 0, 0);
                break;
            case PAUSED:
                renderPlaying(g2d); 
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

        // Mở inventory
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_I)) {
            inventoryUI.toggle();
            if (inventoryUI.isOpen()) {
                changeState(GameState.INVENTORY);
            }
        }

        // Cập nhật player
        player.update(deltaTime);
    }

    /**
     * Update logic khi đang mở inventory.
     */
    private void updateInventory(double deltaTime) {
        // Đóng inventory
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_I) || inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            inventoryUI.toggle();
            changeState(GameState.PLAYING);
            return;
        }

        // Di chuyển cursor
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_UP)) {
            inventoryUI.moveCursor(Direction.UP, player.getInventory());
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            inventoryUI.moveCursor(Direction.DOWN, player.getInventory());
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_LEFT)) {
            inventoryUI.moveCursor(Direction.LEFT, player.getInventory());
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_RIGHT)) {
            inventoryUI.moveCursor(Direction.RIGHT, player.getInventory());
        }

        // Sử dụng item
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            int idx = inventoryUI.getSelectedIndex();
            player.getInventory().useItem(idx, player);
        }
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

        drawPlayer(g2d);

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

    private void drawPlayer(Graphics2D g2d) {
        int tileX = (int) player.getWorldX();
        int tileY = (int) player.getWorldY();
 
        Animation anim = player.getCurrentAnimation();
        BufferedImage frame = (anim != null) ? anim.getCurrentFrame() : null;
 
        int drawSize = Constants.PLAYER_SPRITE_SIZE;
        int drawX = tileX + Constants.TILE_SIZE / 2 - drawSize / 2;
        int drawY = tileY + Constants.TILE_SIZE - drawSize; // neo chân sprite vào đáy tile
 
        if (frame != null) {
            g2d.drawImage(frame, drawX, drawY, drawSize, drawSize, null);
        } else {
            // Fallback: hình vuông màu xanh nếu sprite chưa sẵn sàng
            g2d.setColor(player.isAttacking() ? new Color(230, 150, 60) : new Color(70, 130, 230));
            g2d.fillRect(tileX + 2, tileY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
            g2d.setColor(new Color(120, 180, 255));
            g2d.drawRect(tileX + 2, tileY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
        }
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
