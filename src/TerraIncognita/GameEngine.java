package TerraIncognita;

import TerraIncognita.combat.CombatSystem;
import TerraIncognita.entity.Chest;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Entity;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.Arrow;
import TerraIncognita.entity.WeaponMode;
import TerraIncognita.entity.monster.Monster;
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
import TerraIncognita.map.GameMap;
import TerraIncognita.map.FileMapLoader;
import TerraIncognita.map.Tile;
import TerraIncognita.map.TileType;
import TerraIncognita.ui.InventoryUI;
import TerraIncognita.ui.ShopUI;
import TerraIncognita.ui.HUD;
import TerraIncognita.ui.DialogBox;
import TerraIncognita.ui.GameOverScreen;
import TerraIncognita.ui.RadialMenu;
import TerraIncognita.util.Constants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Quản lý trạng thái game (State Machine).
 *
 * Các trạng thái: MENU → HUB → PLAYING (Dungeon) → RUN_SUMMARY → HUB → ... →
 * GAME_OVER
 *
 * Trách nhiệm:
 * - Chuyển đổi giữa các trạng thái (state transition)
 * - Gọi update/render đúng logic cho từng trạng thái
 * - Quản lý tài nguyên chung (player, map hiện tại, quái vật, rương, shop...)
 *
 * File này được GỘP (merge) từ 2 nhánh:
 *  - Nhánh bản đồ: tải dungeon từ file text (FileMapLoader), đồng bộ vị trí
 *    player/quái theo map, chặn đi xuyên tường.
 *  - Nhánh gameplay: rương, merchant/shop, HUD, dialog, radial menu,
 *    hệ thống chiến đấu (kiếm/cung, combo), mũi tên.
 */
public class GameEngine {

    private GameState currentState;
    private Player player;
    private InputHandler inputHandler;
    private InventoryUI inventoryUI;
    private AssetLoader assetLoader;

    // Bản đồ hầm ngục tải từ file text
    private GameMap currentMap;

    private List<Chest> chests;
    private Set<Chest> collidingChests;
    private String pickupMessage;
    private double messageTimer;

    private Merchant merchant;
    private Shop activeShop;
    private ShopUI shopUI;
    private HUD hud;
    private DialogBox dialogBox;
    private GameOverScreen gameOverScreen;
    private RadialMenu radialMenu;
    private List<Monster> activeMonsters;
    private CombatSystem combatSystem;

    // --- Mũi tên (Arrow projectile) ---
    private List<Arrow> activeArrows;
    private java.awt.image.BufferedImage arrowSprite;

    // TODO (GĐ5): EventSystem eventSystem
    // TODO (GĐ6): SaveManager saveManager

    public GameEngine(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.assetLoader = new AssetLoader();
        this.assetLoader.loadAll();

        // 1. Tải bản đồ hầm ngục từ file text dungeon_1.txt thông qua Constants
        String fullMapPath = Constants.MAPS_PATH + "dungeon_1.txt";
        FileMapLoader mapLoader = new FileMapLoader(fullMapPath);
        this.currentMap = mapLoader.generate(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, 1);

        this.player = new Player();

        // 2. Đồng bộ hóa vị trí nhân vật dựa trên ký tự 'P' trong file bản đồ
        if (this.currentMap != null) {
            int startX = this.currentMap.getPlayerStartX();
            int startY = this.currentMap.getPlayerStartY();
            this.player.setWorldX(startX * Constants.TILE_SIZE);
            this.player.setWorldY(startY * Constants.TILE_SIZE);
            this.player.updateTilePosition(Constants.TILE_SIZE);

            // Kích hoạt tính năng chống đi xuyên tường bằng cách truyền tham chiếu map
            this.player.setCurrentMap(this.currentMap);
        } else {
            // Mức dự phòng ban đầu nếu file lỗi
            this.player.setWorldX(Constants.SCREEN_WIDTH / 2.0 - Constants.TILE_SIZE / 2.0);
            this.player.setWorldY(Constants.SCREEN_HEIGHT / 2.0 - Constants.TILE_SIZE / 2.0);
        }

        this.player.initAnimations(assetLoader);
        this.currentState = GameState.PLAYING;

        // Inventory UI
        this.inventoryUI = new InventoryUI();

        // Spawn rương test
        this.chests = new ArrayList<>();
        this.collidingChests = new HashSet<>();

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
        this.hud = new HUD();
        this.dialogBox = new DialogBox();
        this.gameOverScreen = new GameOverScreen();
        this.radialMenu = new RadialMenu();

        // Cho player 100 gold để test mua đồ
        player.addGold(100);

        // 3. Tự động nạp toàn bộ quái vật được khai báo trong file bản đồ
        this.activeMonsters = new ArrayList<>();
        if (this.currentMap != null) {
            this.currentMap.getEntities().forEach(entity -> {
                if (entity instanceof Monster) {
                    Monster m = (Monster) entity;
                    m.initAnimations(assetLoader);
                    this.activeMonsters.add(m);
                }
            });
        }

        // Hệ thống chiến đấu — tính damage/crit/miss khi tấn công
        this.combatSystem = new CombatSystem();

        // Danh sách mũi tên đang bay
        this.activeArrows = new ArrayList<>();
        this.arrowSprite = assetLoader.getArrowFrame();
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
                // --- CẬP NHẬT HOẠT ẢNH CHO QUÁI VẬT ---
                for (Monster m : activeMonsters) {
                    if (m.isAlive()) {
                        m.update(deltaTime); // Cập nhật chuyển frame hoạt ảnh
                    }
                }
                break;
            case INVENTORY:
                updateInventory(deltaTime);
                break;
            case SHOP:
                updateShop(deltaTime);
                break;
            case RADIAL_MENU:
                updateRadialMenu(deltaTime);
                break;
            case DIALOG:
                updateDialog(deltaTime);
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
            case DIALOG:
                renderPlaying(g2d);
                dialogBox.render(g2d);
                break;
            case RADIAL_MENU:
                renderPlaying(g2d);
                radialMenu.render(g2d);
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

        updateChestCollisions();

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

        // TAB — mở radial menu
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_TAB)) {
            radialMenu.open();
            changeState(GameState.RADIAL_MENU);
        }

        // E key — gần merchant thì tương tác/mở shop; ngược lại đổi vũ khí Kiếm/Cung
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_E)) {
            if (isNearMerchant()) {
                merchant.interact(player);
                activeShop = merchant.getShop();
                shopUI.open();
                changeState(GameState.SHOP);
            } else {
                player.toggleWeaponMode();
                pickupMessage = "Đã chuyển sang "
                        + (player.getWeaponMode() == WeaponMode.SWORD ? "Kiếm" : "Cung");
                messageTimer = 1.2;
            }
        }

        // Tấn công bằng chuột trái — hành vi khác nhau theo vũ khí hiện tại:
        // - Kiếm: bấm là chém ngay (isMouseLeftJustPressed)
        // - Cung: giữ để ngắm (cập nhật hướng quay mặt theo chuột mỗi frame),
        //   thả chuột ra mới thực sự bắn (isMouseLeftJustReleased)
        if (player.getWeaponMode() == WeaponMode.SWORD) {
            if (inputHandler.isMouseLeftJustPressed()) {
                if (player.canAttack()) {
                    handlePlayerAttack();
                }
            }
        } else {
            if (inputHandler.isMouseLeftPressed()) {
                player.aimTowards(inputHandler.getMouseX(), inputHandler.getMouseY());
            }
            if (inputHandler.isMouseLeftJustReleased()) {
                if (player.canAttack()) {
                    handlePlayerAttack();
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

        // Cập nhật mũi tên đang bay
        updateArrows(deltaTime);

        // Kiểm tra game over
        if (!player.isAlive()) {
            changeState(GameState.GAME_OVER);
        }
    }

    /**
     * Update logic khi đang mở inventory.
     */
    private void updateInventory(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_I) || inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            inventoryUI.toggle();
            changeState(GameState.PLAYING);
            return;
        }
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
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            int idx = inventoryUI.getSelectedIndex();
            player.getInventory().useItem(idx, player);
        }
    }

    /**
     * Update logic khi đang mở shop.
     */
    private void updateShop(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            shopUI.close();
            changeState(GameState.PLAYING);
            return;
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_S)) {
            shopUI.toggleMode();
        }
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
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            int idx = shopUI.getSelectedIndex();
            if (shopUI.isBuyMode()) {
                boolean success = activeShop.buyItem(player, idx);
                pickupMessage = success ? "Đã mua!" : "Không đủ vàng hoặc túi đầy!";
            } else {
                boolean success = activeShop.sellItem(player, idx);
                pickupMessage = success ? "Đã bán!" : "Không bán được!";
            }
            messageTimer = 2.0;
        }
    }

    private void updateRadialMenu(double deltaTime) {
        radialMenu.updateHover(inputHandler.getMouseX(), inputHandler.getMouseY());

        // Thả TAB → chọn option
        if (!inputHandler.isKeyPressed(KeyEvent.VK_TAB)) {
            RadialMenu.Option selected = radialMenu.getHoveredOption();
            radialMenu.close();
            if (selected != null) {
                switch (selected) {
                    case INVENTORY:
                        inventoryUI.toggle();
                        if (inventoryUI.isOpen()) {
                            changeState(GameState.INVENTORY);
                        } else {
                            changeState(GameState.PLAYING);
                        }
                        break;
                    case TALENT:
                        // TODO: talent tree state khi Person 2 xong
                        pickupMessage = "Talent tree — chưa implement";
                        messageTimer = 2.0;
                        changeState(GameState.PLAYING);
                        break;
                    case MAP:
                        // TODO: map state khi Person 1 xong
                        pickupMessage = "Map — chưa implement";
                        messageTimer = 2.0;
                        changeState(GameState.PLAYING);
                        break;
                }
            } else {
                changeState(GameState.PLAYING);
            }
        }
    }

    private void updateDialog(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER) || inputHandler.isKeyJustPressed(KeyEvent.VK_E)) {
            dialogBox.advance();
            if (!dialogBox.isActive()) {
                changeState(GameState.PLAYING);
            }
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            dialogBox.close();
            changeState(GameState.PLAYING);
        }
    }

    private void updateGameOver(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_UP)) {
            gameOverScreen.moveCursorUp();
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            gameOverScreen.moveCursorDown();
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            int opt = gameOverScreen.getSelectedOption();
            if (opt == 0) {
                changeState(GameState.MENU);
            } else {
                System.exit(0);
            }
        }
    }

    // =========================================
    // RENDER riêng cho từng state
    // =========================================

    private void renderMenu(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(32f));
        String title = Constants.GAME_TITLE;
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (Constants.SCREEN_WIDTH - titleWidth) / 2, Constants.SCREEN_HEIGHT / 3);

        g2d.setFont(g2d.getFont().deriveFont(16f));
        String hint = "Nhan ENTER de bat dau";
        int hintWidth = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (Constants.SCREEN_WIDTH - hintWidth) / 2, Constants.SCREEN_HEIGHT / 2);
    }

    /**
     * Vẽ toàn bộ màn chơi: tile bản đồ (tường/cửa/cầu thang) từ currentMap,
     * rồi tới player, rương, merchant, HUD, quái vật và mũi tên đang bay.
     */
    private void renderPlaying(Graphics2D g2d) {
        // Nền tối hầm ngục
        g2d.setColor(new Color(30, 30, 40));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Duyệt ma trận dữ liệu của currentMap để kết xuất đồ họa cụ thể
        if (currentMap != null) {
            int tileSize = Constants.TILE_SIZE;
            for (int y = 0; y < currentMap.getHeight(); y++) {
                for (int x = 0; x < currentMap.getWidth(); x++) {
                    Tile tile = currentMap.getTile(x, y);
                    if (tile.getType() == TileType.WALL) {
                        g2d.setColor(new Color(60, 60, 70)); // Khối tường đá xám đậm
                        g2d.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                        g2d.setColor(new Color(40, 40, 45)); // Khung viền tường
                        g2d.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    } else if (tile.getType() == TileType.DOOR) {
                        g2d.setColor(new Color(139, 69, 19)); // Khối gỗ cửa nâu
                        g2d.fillRect(x * tileSize + 4, y * tileSize, tileSize - 8, tileSize);
                    } else if (tile.getType() == TileType.STAIR_DOWN) {
                        g2d.setColor(new Color(100, 149, 237)); // Cầu thang thoát màu lam
                        g2d.fillRect(x * tileSize + 2, y * tileSize + 2, tileSize - 4, tileSize - 4);
                    }
                }
            }
        }

        drawPlayer(g2d);

        // Đường ngắm mờ khi đang giữ chuột trái ở chế độ Cung
        if (player.getWeaponMode() == WeaponMode.BOW && inputHandler.isMouseLeftPressed()) {
            drawAimLine(g2d);
        }

        // Vẽ rương
        for (Chest chest : chests) {
            drawChest(g2d, chest);
        }

        // Vẽ merchant
        if (merchant != null) {
            drawMerchant(g2d);
        }

        // HUD
        hud.render(g2d, player);
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(g2d.getFont().deriveFont(12f));
        g2d.drawString("Pos: (" + player.getTileX() + ", " + player.getTileY() + ")", 10, 70);
        g2d.drawString("State: " + player.getState(), 10, 85);

        // Vũ khí hiện tại + tiến trình combo (chỉ hiện số combo khi đang dùng Kiếm)
        String weaponLabel = (player.getWeaponMode() == WeaponMode.SWORD) ? "Kiếm" : "Cung";
        String weaponLine = "Vũ khí: " + weaponLabel + " (E để đổi)";
        if (player.getWeaponMode() == WeaponMode.SWORD && player.getComboCount() > 0) {
            weaponLine += "  |  Combo: " + player.getComboCount() + "/3";
        }
        g2d.setColor(new Color(220, 220, 160));
        g2d.drawString(weaponLine, 10, 100);

        // Thông báo nhặt đồ
        if (pickupMessage != null && messageTimer > 0) {
            int alpha = (int) (200 * Math.min(messageTimer / 3.0, 1.0));
            g2d.setColor(new Color(255, 255, 200, alpha));
            g2d.setFont(g2d.getFont().deriveFont(16f));
            int msgWidth = g2d.getFontMetrics().stringWidth(pickupMessage);
            g2d.drawString(pickupMessage, (Constants.SCREEN_WIDTH - msgWidth) / 2, Constants.SCREEN_HEIGHT - 40);
        }

        // Vẽ quái vật đang hoạt động
        for (Monster m : activeMonsters) {
            if (m.isAlive()) {
                drawMonster(g2d, m);
            }
        }

        // Vẽ mũi tên đang bay
        for (Arrow arrow : activeArrows) {
            arrow.render(g2d);
        }
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

    /**
     * Vẽ đường kẻ mờ từ tâm player tới hướng ngắm (đã clamp ±60° từ ngang)
     * khi đang giữ chuột trái ở chế độ Cung.
     */
    private void drawAimLine(Graphics2D g2d) {
        int cx = (int) player.getWorldX() + Constants.TILE_SIZE / 2;
        int cy = (int) player.getWorldY() + Constants.TILE_SIZE - Constants.PLAYER_SPRITE_SIZE / 2;
        int mx = inputHandler.getMouseX();
        int my = inputHandler.getMouseY();

        double clampedAngle = clampBowAngle(cx, cy, mx, my);
        double dx = mx - cx;
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        int aimX = (int) (cx + Math.cos(clampedAngle) * dist);
        int aimY = (int) (cy + Math.sin(clampedAngle) * dist);

        java.awt.Stroke oldStroke = g2d.getStroke();
        float[] dash = {8f, 6f};
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.drawLine(cx, cy, aimX, aimY);
        g2d.setStroke(oldStroke);

        g2d.setColor(new Color(255, 220, 120, 180));
        g2d.fillOval(aimX - 4, aimY - 4, 8, 8);
        g2d.setColor(new Color(255, 160, 60, 140));
        g2d.drawOval(aimX - 6, aimY - 6, 12, 12);
    }

    /**
     * Clamp góc ngắm cung trong phạm vi ±60° so với hướng ngang.
     */
    private double clampBowAngle(double cx, double cy, double mx, double my) {
        double dx = mx - cx;
        double dy = my - cy;
        double rawAngle = Math.atan2(dy, dx);
        double maxDev = Math.toRadians(60);

        if (dx >= 0) {
            return Math.max(-maxDev, Math.min(maxDev, rawAngle));
        } else {
            double dev = Math.atan2(Math.sin(rawAngle - Math.PI), Math.cos(rawAngle - Math.PI));
            dev = Math.max(-maxDev, Math.min(maxDev, dev));
            return Math.atan2(Math.sin(dev + Math.PI), Math.cos(dev + Math.PI));
        }
    }

    private void updateChestCollisions() {
        Set<Chest> currentlyColliding = new HashSet<>();
        for (Chest chest : chests) {
            if (isCollidingWithChest(chest)) {
                currentlyColliding.add(chest);
                if (!collidingChests.contains(chest)) {
                    tryOpenChest(chest);
                }
            }
        }
        collidingChests = currentlyColliding;
    }

    private void tryOpenChest(Chest chest) {
        boolean opened = chest.open(player);
        if (opened) {
            Item loot = chest.getLastLoot();
            pickupMessage = (loot != null) ? "Nhặt được: " + loot.getName() : "Rương trống!";
        } else if (chest.isLocked()) {
            pickupMessage = "Rương bị khóa! Cần chìa khóa.";
        } else {
            pickupMessage = "Rương đã mở rồi.";
        }
        messageTimer = 3.0;
    }

    private boolean isCollidingWithChest(Chest chest) {
        return rectsOverlap(
                player.getWorldX(), player.getWorldY(),
                chest.getWorldX(), chest.getWorldY(),
                Constants.TILE_SIZE);
    }

    private boolean rectsOverlap(double x1, double y1, double x2, double y2, int size) {
        return x1 < x2 + size && x1 + size > x2 && y1 < y2 + size && y1 + size > y2;
    }

    /**
     * Xử lý 1 nhát tấn công của player — kiếm (cận chiến, có combo) hoặc
     * cung (tầm xa, bắn mũi tên), tuỳ theo player.getWeaponMode().
     */
    private void handlePlayerAttack() {
        player.stateAttack();

        if (player.getWeaponMode() == WeaponMode.BOW) {
            // --- CUNG: spawn mũi tên từ tâm hiển thị sprite player ---
            double cx = player.getWorldX() + Constants.TILE_SIZE / 2.0;
            double cy = player.getWorldY() + Constants.TILE_SIZE - Constants.PLAYER_SPRITE_SIZE / 2.0;
            double mx = inputHandler.getMouseX();
            double my = inputHandler.getMouseY();

            double clampedAngle = clampBowAngle(cx, cy, mx, my);
            double tmx = cx + Math.cos(clampedAngle);
            double tmy = cy + Math.sin(clampedAngle);

            Arrow arrow = new Arrow(cx, cy, tmx, tmy, arrowSprite);
            activeArrows.add(arrow);
            return;
        }

        // --- KIẾM: kiểm tra hitbox tức thời (cận chiến) ---
        Rectangle attackHitbox = player.getAttackHitbox();
        Entity target = player.getCollisionManager().findAttackTarget(player, attackHitbox, activeMonsters);

        if (target == null) {
            return;
        }

        // Đòn combo thứ 3 của kiếm gây sát thương cao hơn
        double damageMultiplier = player.isLastAttackComboFinisher()
                ? Constants.COMBO_FINISHER_DAMAGE_MULTIPLIER
                : 1.0;

        CombatSystem.CombatResult result = combatSystem.attack(player, target, damageMultiplier);

        if (result.isMiss) {
            pickupMessage = "Trượt!";
        } else {
            String comboTag = player.isLastAttackComboFinisher() ? "COMBO x3! " : "";
            String critTag = result.isCrit ? "CHÍ MẠNG! " : "";
            pickupMessage = comboTag + critTag + "Gây " + result.damage + " sát thương";

            if (result.targetDied && target instanceof Monster) {
                Monster monster = (Monster) target;
                player.addExp(monster.getExpReward());
                player.addGold(monster.getGoldReward());
                pickupMessage += " — hạ gục " + monster.getName()
                        + "! +" + monster.getExpReward() + " EXP, +" + monster.getGoldReward() + " vàng";
            }
        }
        messageTimer = 1.5;
    }

    /**
     * Cập nhật tất cả mũi tên đang bay: di chuyển, kiểm tra va chạm quái,
     * gây sát thương khi trúng, và xoá mũi tên đã chết.
     */
    private void updateArrows(double deltaTime) {
        Iterator<Arrow> it = activeArrows.iterator();
        while (it.hasNext()) {
            Arrow arrow = it.next();
            arrow.update(deltaTime);

            if (!arrow.isAlive()) {
                it.remove();
                continue;
            }

            Rectangle arrowBox = arrow.getHitbox();
            for (Monster m : activeMonsters) {
                if (!m.isAlive()) continue;
                if (arrowBox.intersects(m.getHitbox())) {
                    CombatSystem.CombatResult result = combatSystem.attack(player, m, 1.0);
                    arrow.kill();

                    if (result.isMiss) {
                        pickupMessage = "Mũi tên trượt!";
                    } else {
                        String critTag = result.isCrit ? "CHÍ MẠNG! " : "";
                        pickupMessage = "🏹 " + critTag + "Gây " + result.damage + " sát thương";
                        if (result.targetDied) {
                            player.addExp(m.getExpReward());
                            player.addGold(m.getGoldReward());
                            pickupMessage += " — hạ gục " + m.getName()
                                    + "! +" + m.getExpReward() + " EXP, +" + m.getGoldReward() + " vàng";
                        }
                    }
                    messageTimer = 1.5;
                    break; // mỗi mũi tên chỉ trúng 1 mục tiêu
                }
            }

            if (!arrow.isAlive()) {
                it.remove();
            }
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

        String key = "chest_" + chest.getChestType() + (chest.isOpened() ? "_open" : "_closed");
        BufferedImage frame = assetLoader.getTile(key);

        if (frame != null) {
            int drawW = size;
            int drawH = (int) ((double) Constants.CHEST_FRAME_HEIGHT / Constants.CHEST_FRAME_WIDTH * size);
            int drawY = py + size - drawH; // neo đáy tile
            g2d.drawImage(frame, px, drawY, drawW, drawH, null);
        } else {
            int pad = 4;
            if (chest.isOpened()) {
                g2d.setColor(new Color(80, 70, 50));
            } else if (chest.isLocked()) {
                g2d.setColor(new Color(120, 80, 40));
            } else {
                g2d.setColor(new Color(180, 140, 60));
            }
            g2d.fillRect(px + pad, py + pad, size - pad * 2, size - pad * 2);
        }
    }

    private void drawMerchant(Graphics2D g2d) {
        int px = (int) merchant.getWorldX();
        int py = (int) merchant.getWorldY();
        int size = Constants.TILE_SIZE;
        int pad = 4;

        g2d.setColor(new Color(60, 180, 100));
        g2d.fillRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        g2d.setColor(new Color(30, 100, 50));
        g2d.drawRect(px + pad, py + pad, size - pad * 2, size - pad * 2);

        g2d.setColor(new Color(255, 230, 80));
        g2d.setFont(g2d.getFont().deriveFont(16f));
        g2d.drawString("$", px + size / 2 - 4, py + size / 2 + 6);
    }

    private void renderPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

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
        gameOverScreen.render(g2d);
    }

    /**
     * Phương thức bổ trợ vẽ hoạt ảnh quái vật lên màn hình
     */
    private void drawMonster(Graphics2D g2d, Monster monster) {
        int worldX = (int) monster.getWorldX();
        int worldY = (int) monster.getWorldY();

        Animation anim = monster.getCurrentAnimation();
        BufferedImage frame = (anim != null) ? anim.getCurrentFrame() : null;

        int drawSize = Constants.PLAYER_SPRITE_SIZE;
        int drawX = worldX + Constants.TILE_SIZE / 2 - drawSize / 2;
        int drawY = worldY + Constants.TILE_SIZE - drawSize; // Ghép chân vào đáy tile

        if (frame != null) {
            g2d.drawImage(frame, drawX, drawY, drawSize, drawSize, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(worldX + 2, worldY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
            g2d.setColor(Color.WHITE);
            g2d.drawString("S", worldX + 12, worldY + 20);
        }
    }

    // --- Getter ---
    public GameState getCurrentState() {
        return currentState;
    }

    public Player getPlayer() {
        return player;
    }
}