package TerraIncognita;

import TerraIncognita.combat.CombatSystem;
import TerraIncognita.entity.Chest;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.monster.Monster;
import TerraIncognita.entity.monster.SkeletonMonster;
import TerraIncognita.entity.monster.SlimeMonster;
import TerraIncognita.economy.LootTable;
import TerraIncognita.economy.Shop;
import TerraIncognita.entity.npc.Merchant;
import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.Key;
import TerraIncognita.item.Potion;
import TerraIncognita.ui.InventoryUI;
import TerraIncognita.ui.ShopUI;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    private List<Chest> chests;
    private String pickupMessage;
    private double messageTimer;

    private Merchant merchant;
    private Shop activeShop;
    private ShopUI shopUI;

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

        // Spawn rương test
        this.chests = new ArrayList<>();

        // Rương 1: không khóa, tile (10,5), chứa Potion
        Chest chest1 = new Chest(10, 5, false);
        Potion potion = new Potion("hp1", "Health Potion", 30);
        chest1.setLootTable(new LootTable(List.of(potion), 1.0));
        chests.add(chest1);

        // Rương 2: khóa, tile (15,8), chứa Equipment, cần "dungeon_key"
        Chest chest2 = new Chest(15, 8, true);
        chest2.setRequiredKeyId("dungeon_key");
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        chest2.setLootTable(new LootTable(List.of(sword), 1.0));
        chests.add(chest2);

        // Cho player chìa khóa để test rương khóa
        player.getInventory().addItem(new Key("dungeon_key", "Dungeon Key", "dungeon_key"));

        // Spawn Merchant NPC ở tile (20, 10)
        this.merchant = new Merchant(20, 10);
        this.shopUI = new ShopUI();

        // Cho player 100 gold để test mua đồ
        player.addGold(100);
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
            case SHOP:
                updateShop(deltaTime);
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
            case SHOP:
                renderPlaying(g2d);
                shopUI.render(g2d, activeShop, player);
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

        // E key — mở rương hoặc tương tác merchant
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_E)) {
            // Kiểm tra merchant trước
            if (isNearMerchant()) {
                merchant.interact(player);
                activeShop = merchant.getShop();
                shopUI.open();
                changeState(GameState.SHOP);
            } else {
                // Kiểm tra rương
                for (Chest chest : chests) {
                    if (isNearChest(chest)) {
                        boolean opened = chest.open(player);
                        if (opened) {
                            Item loot = chest.getLastLoot();
                            if (loot != null) {
                                pickupMessage = "Nhặt được: " + loot.getName();
                            } else {
                                pickupMessage = "Rương trống!";
                            }
                        } else if (chest.isLocked()) {
                            pickupMessage = "Rương bị khóa! Cần chìa khóa.";
                        } else {
                            pickupMessage = "Rương đã mở rồi.";
                        }
                        messageTimer = 3.0;
                        break;
                    }
                }
            }
        }

        // Đếm ngược timer message
        if (messageTimer > 0) {
            messageTimer -= deltaTime;
            if (messageTimer <= 0) {
                pickupMessage = null;
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

    /**
     * Update logic khi đang mở shop.
     */
    private void updateShop(double deltaTime) {
        // ESC → đóng shop
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            shopUI.close();
            changeState(GameState.PLAYING);
            return;
        }

        // S → chuyển mode buy/sell
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_S)) {
            shopUI.toggleMode();
        }

        // Di chuyển cursor
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_UP)) {
            shopUI.moveCursor(Direction.UP, activeShop, player);
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            shopUI.moveCursor(Direction.DOWN, activeShop, player);
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_LEFT)) {
            shopUI.moveCursor(Direction.LEFT, activeShop, player);
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_RIGHT)) {
            shopUI.moveCursor(Direction.RIGHT, activeShop, player);
        }

        // Enter → mua hoặc bán
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            int idx = shopUI.getSelectedIndex();
            if (shopUI.isBuyMode()) {
                boolean success = activeShop.buyItem(player, idx);
                if (success) {
                    pickupMessage = "Đã mua: " + activeShop.getItems().get(idx).getName();
                } else {
                    pickupMessage = "Không thể mua! (Thiếu gold hoặc túi đầy)";
                }
            } else {
                boolean success = activeShop.sellItem(player, idx);
                if (success) {
                    pickupMessage = "Đã bán đồ thành công!";
                } else {
                    pickupMessage = "Không thể bán!";
                }
            }
            messageTimer = 2.0;
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

        // Vẽ rương
        for (Chest chest : chests) {
            drawChest(g2d, chest);
        }

        // Vẽ merchant
        if (merchant != null) {
            drawMerchant(g2d);
        }

        // HUD tạm (góc trên trái)
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(14f));
        g2d.drawString("HP: " + player.getHp() + "/" + player.getMaxHp(), 10, 20);
        g2d.drawString("Pos: (" + player.getTileX() + ", " + player.getTileY() + ")", 10, 40);
        g2d.drawString("State: " + player.getState(), 10, 60);

        // Thông báo nhặt đồ
        if (pickupMessage != null && messageTimer > 0) {
            int alpha = (int) (200 * Math.min(messageTimer / 3.0, 1.0));
            g2d.setColor(new Color(255, 255, 200, alpha));
            g2d.setFont(g2d.getFont().deriveFont(16f));
            int msgWidth = g2d.getFontMetrics().stringWidth(pickupMessage);
            g2d.drawString(pickupMessage, (Constants.SCREEN_WIDTH - msgWidth) / 2, Constants.SCREEN_HEIGHT - 40);
        }

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


    private boolean isNearChest(Chest chest) {
        int dx = Math.abs(player.getTileX() - chest.getTileX());
        int dy = Math.abs(player.getTileY() - chest.getTileY());
        return dx <= 1 && dy <= 1;
    }

    private boolean isNearMerchant() {
        if (merchant == null) return false;
        int dx = Math.abs(player.getTileX() - merchant.getTileX());
        int dy = Math.abs(player.getTileY() - merchant.getTileY());
        return dx <= 1 && dy <= 1;
    }

    private void drawChest(Graphics2D g2d, Chest chest) {
        int px = (int) chest.getWorldX();
        int py = (int) chest.getWorldY();
        int size = Constants.TILE_SIZE;
        int pad = 4;

        if (chest.isOpened()) {
            g2d.setColor(new Color(80, 70, 50));      // xám tối
        } else if (chest.isLocked()) {
            g2d.setColor(new Color(120, 80, 40));     // nâu
        } else {
            g2d.setColor(new Color(180, 140, 60));    // vàng đồng
        }
        g2d.fillRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        // Viền
        if (chest.isLocked() && !chest.isOpened()) {
            g2d.setColor(new Color(200, 60, 60));      // đỏ
        } else {
            g2d.setColor(new Color(60, 50, 30));
        }
        g2d.drawRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        // Icon: dấu + (chưa mở), dấu - (đã mở)
        g2d.setColor(chest.isOpened() ? new Color(50, 45, 35) : new Color(220, 200, 120));
        int cx = px + size / 2;
        int cy = py + size / 2;
        if (!chest.isOpened()) {
            g2d.fillRect(cx - 6, cy - 2, 12, 4);
            g2d.fillRect(cx - 2, cy - 6, 4, 12);
        } else {
            g2d.fillRect(cx - 5, cy - 1, 10, 2);
        }
    }

    private void drawMerchant(Graphics2D g2d) {
        int px = (int) merchant.getWorldX();
        int py = (int) merchant.getWorldY();
        int size = Constants.TILE_SIZE;
        int pad = 4;

        // Thân merchant — màu xanh lá
        g2d.setColor(new Color(60, 180, 100));
        g2d.fillRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        // Viền
        g2d.setColor(new Color(30, 100, 50));
        g2d.drawRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        // Icon: dấu $ 
        g2d.setColor(new Color(255, 230, 80));
        g2d.setFont(g2d.getFont().deriveFont(16f));
        g2d.drawString("$", px + size / 2 - 4, py + size / 2 + 6);
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