package TerraIncognita;

import TerraIncognita.combat.CombatSystem;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.monster.Monster;
import TerraIncognita.entity.monster.SlimeMonster;
import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.map.GameMap;
import TerraIncognita.map.FileMapLoader;
import TerraIncognita.map.Tile;
import TerraIncognita.map.TileType;
import TerraIncognita.ui.InventoryUI;
import TerraIncognita.util.Constants;
 
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private GameState currentState;
    private Player player;
    private InputHandler inputHandler;
    private InventoryUI inventoryUI;
    private AssetLoader assetLoader;
    private List<Monster> activeMonsters;
    
    // TÍCH HỢP HỆ THỐNG BẢN ĐỒ THỰC TẾ
    private GameMap currentMap;

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
        this.inventoryUI = new InventoryUI();
        this.activeMonsters = new ArrayList<>();

        // 3. Tự động chuyển toàn bộ quái vật được nạp tự động từ file text vào Engine
        if (this.currentMap != null) {
            this.currentMap.getEntities().forEach(entity -> {
                if (entity instanceof Monster) {
                    Monster m = (Monster) entity;
                    m.initAnimations(assetLoader);
                    this.activeMonsters.add(m);
                }
            });
        }
    }

    public void update(double deltaTime) {
        switch (currentState) {
            case MENU:
                updateMenu(deltaTime);
                break;
            case PLAYING:
                updatePlaying(deltaTime);
                for (Monster m : activeMonsters) {
                    if (m.isAlive()) {
                        m.update(deltaTime);
                    }
                }
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

    public void changeState(GameState newState) {
        this.currentState = newState;
    }

    private void updateMenu(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            changeState(GameState.PLAYING);
        }
    }

    private void updatePlaying(double deltaTime) {
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
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            changeState(GameState.PAUSED);
        }
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_I)) {
            inventoryUI.toggle();
            if (inventoryUI.isOpen()) {
                changeState(GameState.INVENTORY);
            }
        }
        player.update(deltaTime);
    }

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

    private void updateGameOver(double deltaTime) {
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            changeState(GameState.MENU);
        }
    }

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

    // PHƯƠNG THỨC ĐÃ THAY ĐỔI ĐỂ DỰNG HẦM NGỤC THỰC TẾ
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

        for (Monster m : activeMonsters) {
            if (m.isAlive()) {
                drawMonster(g2d, m);
            }
        }

        // HUD thông tin đồng bộ thời gian thực
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(14f));
        g2d.drawString("HP: " + player.getHp() + "/" + player.getMaxHp(), 10, 20);
        g2d.drawString("Pos: (" + player.getTileX() + ", " + player.getTileY() + ")", 10, 40);
        g2d.drawString("State: " + player.getState(), 10, 60);
    }

    private void drawPlayer(Graphics2D g2d) {
        int tileX = (int) player.getWorldX();
        int tileY = (int) player.getWorldY();
        Animation anim = player.getCurrentAnimation();
        BufferedImage frame = (anim != null) ? anim.getCurrentFrame() : null;
        int drawSize = Constants.PLAYER_SPRITE_SIZE;
        int drawX = tileX + Constants.TILE_SIZE / 2 - drawSize / 2;
        int drawY = tileY + Constants.TILE_SIZE - drawSize;
 
        if (frame != null) {
            g2d.drawImage(frame, drawX, drawY, drawSize, drawSize, null);
        } else {
            g2d.setColor(player.isAttacking() ? new Color(230, 150, 60) : new Color(70, 130, 230));
            g2d.fillRect(tileX + 2, tileY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
            g2d.setColor(new Color(120, 180, 255));
            g2d.drawRect(tileX + 2, tileY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
        }
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
        g2d.setColor(new Color(50, 10, 10));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g2d.setColor(new Color(220, 50, 50));
        g2d.setFont(g2d.getFont().deriveFont(48f));
        String text = "GAME OVER";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, Constants.SCREEN_HEIGHT / 2);
    }

    private void drawMonster(Graphics2D g2d, Monster monster) {
        int worldX = (int) monster.getWorldX();
        int worldY = (int) monster.getWorldY();
        Animation anim = monster.getCurrentAnimation();
        BufferedImage frame = (anim != null) ? anim.getCurrentFrame() : null;
        int drawSize = Constants.PLAYER_SPRITE_SIZE;
        int drawX = worldX + Constants.TILE_SIZE / 2 - drawSize / 2;
        int drawY = worldY + Constants.TILE_SIZE - drawSize;

        if (frame != null) {
            g2d.drawImage(frame, drawX, drawY, drawSize, drawSize, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(worldX + 2, worldY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4);
            g2d.setColor(Color.WHITE);
            g2d.drawString("S", worldX + 12, worldY + 20);
        }
    }

    public GameState getCurrentState() { return currentState; }
    public Player getPlayer() { return player; }
}