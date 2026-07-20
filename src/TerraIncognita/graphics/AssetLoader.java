package TerraIncognita.graphics;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import TerraIncognita.util.Constants;  

/**
 * Load và cache toàn bộ tài nguyên ảnh (sprites, tiles, UI).
 * Chỉ load 1 lần khi khởi động game, lưu vào Map để tái sử dụng.
 *
 * Cách dùng:
 * AssetLoader assets = new AssetLoader();
 * assets.loadAll();
 * BufferedImage[] playerWalk = assets.getFrames("player_walk");
 * BufferedImage wallTile = assets.getTile("wall");
 */
public class AssetLoader {

    private static final int PLAYER_FRAME_SIZE = 100; // mỗi frame trong sprite sheet Soldier là 100x100

    private Map<String, BufferedImage[]> spriteFrames; // animation frames theo tên (vd: "player_walk")
    private Map<String, BufferedImage[]> flippedCache; // cache bản lật ngang (dùng khi quay trái)
    private Map<String, BufferedImage> tileImages; // ảnh tile đơn lẻ
    private Map<String, BufferedImage> uiImages; // ảnh giao diện

    public AssetLoader() {
        this.spriteFrames = new HashMap<>();
        this.flippedCache = new HashMap<>();
        this.tileImages = new HashMap<>();
        this.uiImages = new HashMap<>();
    }

    /**
     * Load toàn bộ tài nguyên ảnh từ thư mục resources/sprites/.
     * Gọi 1 lần duy nhất khi khởi động game.
     */
    public void loadAll() {
        loadPlayer();
        loadChest();
        loadMonsters();
        loadArrow();
    }

    private void loadChest() {
        String path = Constants.SPRITES_PATH + "items/chest/Chests.png";
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            return;
        }
        // Chests.png: 6 cols × 8 rows, mỗi frame 40×32
        // Layout: mỗi loại rương chiếm 2 hàng (hàng 0-1 brown, 2-3 gold, 4-5 red, 6-7 blue)
        // Hàng chẵn (0,2,4,6): closed → mở dần
        // Hàng lẻ (1,3,5,7): closed → mở dần (bản khác góc nhìn?)
        // Dùng frame 0 = closed, frame 5 (cuối hàng) = open
        SpriteSheet cutter = new SpriteSheet(sheet, Constants.CHEST_FRAME_WIDTH, Constants.CHEST_FRAME_HEIGHT);

        // Brown: hàng 0, frame 0 = closed, frame 5 = open
        tileImages.put("chest_brown_closed", cutter.getFrame(0, 0));
        tileImages.put("chest_brown_open", cutter.getFrame(5, 0));

        // Gold: hàng 2, frame 0 = closed, frame 5 = open
        tileImages.put("chest_gold_closed", cutter.getFrame(0, 2));
        tileImages.put("chest_gold_open", cutter.getFrame(5, 2));

        // Blue: hàng 6, frame 0 = closed, frame 5 = open
        tileImages.put("chest_blue_closed", cutter.getFrame(0, 6));
        tileImages.put("chest_blue_open", cutter.getFrame(5, 6));
    }

    private void loadPlayer() {
        String base = Constants.SPRITES_PATH + "player/";

        loadAnimationSheet("player_idle", base + "Soldier_Idle.png");
        loadAnimationSheet("player_walk", base + "Soldier_Walk.png");
        loadAnimationSheet("player_attack", base + "Soldier_Attack01.png");
        loadAnimationSheet("player_attack2", base + "Soldier_Attack02.png"); // đòn combo thứ 3
        loadAnimationSheet("player_attack3", base + "Soldier_Attack03.png"); // tấn công cung
        loadAnimationSheet("player_hurt", base + "Soldier_Hurt.png");
        loadAnimationSheet("player_dead", base + "Soldier_Death.png");
    }

    /**
     * Load sprite mũi tên (frame đầu tiên của Arrow01(100x100).png).
     * Sprite gốc hướng sang phải; sẽ được xoay lúc vẽ bởi Arrow.render().
     */
    private void loadArrow() {
        String path = Constants.SPRITES_PATH + "player/Arrow01(100x100).png";
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            System.err.println("[AssetLoader] Could not load arrow sprite: " + path);
            return;
        }
        // Lấy frame đầu tiên (100x100) từ sprite sheet
        SpriteSheet cutter = new SpriteSheet(sheet, PLAYER_FRAME_SIZE, PLAYER_FRAME_SIZE);
        BufferedImage frame0 = cutter.getFrame(0, 0);
        tileImages.put("arrow0", frame0);
        System.out.println("[DEBUG AssetLoader] Loaded arrow0 frame (" + frame0.getWidth() + "x" + frame0.getHeight() + ")");
    }

    /**
     * Lấy frame mũi tên (đã load sẵn).
     */
    public BufferedImage getArrowFrame() {
        return tileImages.get("arrow0");
    }

    private void loadAnimationSheet(String name, String path) {
        System.out.println("[DEBUG AssetLoader] Loading sheet: name='" + name + "' path='" + path + "'");
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            System.out.println("[DEBUG AssetLoader] FAILED to load sheet: '" + name + "' — image is null!");
            spriteFrames.put(name, new BufferedImage[0]);
            return;
        }
        SpriteSheet sheetCutter = new SpriteSheet(sheet, PLAYER_FRAME_SIZE, PLAYER_FRAME_SIZE);
        BufferedImage[] frames = sheetCutter.getFullRow(0);
        spriteFrames.put(name, frames);
        System.out.println("[DEBUG AssetLoader] Loaded '" + name + "' => " + frames.length + " frames"
                + " (sheet " + sheet.getWidth() + "x" + sheet.getHeight() + ")");
    }

    private void loadMonsters() {
    // Đường dẫn gốc: "resources/sprites/monster/"
    String base = Constants.SPRITES_PATH + "monsters/"; 

    // Nạp hoạt ảnh đứng yên (idle) cho quái vật Slime (hoặc Orc_Idle nếu bạn muốn dùng Orc)
    loadMonsterSheet("slime_idle", base + "Slime_Idle.png");
}

private void loadMonsterSheet(String name, String path) {
    BufferedImage sheet = loadImage(path);
    if (sheet == null) {
        spriteFrames.put(name, new BufferedImage[0]);
        return;
    }
    
    // Cắt sprite sheet của quái vật với kích thước frame 100x100
    int monsterFrameSize = 100; 
    SpriteSheet sheetCutter = new SpriteSheet(sheet, monsterFrameSize, monsterFrameSize);
    BufferedImage[] frames = sheetCutter.getFullRow(0);
    spriteFrames.put(name, frames);
}

    private BufferedImage loadImage(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Asset not found: " + path);
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("[AssetLoader] Loi doc anh " + path + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy mảng frame animation theo tên.
     * 
     * @param name tên sprite (ví dụ: "player_walk_down", "slime_idle")
     * @return mảng BufferedImage[] các frame
     */
    public BufferedImage[] getFrames(String name) {
        BufferedImage[] frames = spriteFrames.get(name);
        return frames != null ? frames : new BufferedImage[0];
    }

    public BufferedImage[] getFramesFlipped(String name) {
        if (flippedCache.containsKey(name)) {
            return flippedCache.get(name);
        }

        BufferedImage[] original = getFrames(name);
        BufferedImage[] flipped = new BufferedImage[original.length];

        for (int i = 0; i < original.length; i ++) {
            flipped[i] = flipHorizontal(original[i]);
        }
        flippedCache.put(name, flipped);

        return flipped;
    }

    private BufferedImage flipHorizontal(BufferedImage src) {
        if (src == null) {
            return null;
        }

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = res.createGraphics();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-w,0);
        g2d.drawImage(src, tx, null);
        g2d.dispose();
        return res;
    }

    /**
     * Lấy ảnh tile theo tên.
     * 
     * @param name tên tile (ví dụ: "wall", "floor", "door")
     * @return BufferedImage ảnh tile
     */
    public BufferedImage getTile(String name) {
        return tileImages.get(name);
    }

    /**
     * Lấy ảnh UI theo tên.
     */
    public BufferedImage getUI(String name) {
        return uiImages.get(name);
    }

    // TODO: Phương thức helper
    // private BufferedImage loadImage(String path) { ... } — đọc 1 file ảnh bằng
    // ImageIO.read()
    // private BufferedImage[] loadFrames(String basePath, int count) { ... } — load
    // nhiều frame
}
