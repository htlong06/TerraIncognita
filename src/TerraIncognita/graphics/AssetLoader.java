package TerraIncognita.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
        loadItemIcons();
        loadMonsters();
        loadArrow();
        loadBomb();
        loadNPC();
    }

    /**
     * Load sprite cho NPC (Quest Giver, v.v.).
     * Hình desertnpc.png là sprite sheet 2 frame nằm cạnh nhau.
     * Tách thành từng frame riêng để animation.
     */
    private void loadNPC() {
        String path = Constants.SPRITES_PATH + "npc/desertnpc.png";
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            System.err.println("[AssetLoader] WARN: Could not load npc/desertnpc.png");
            return;
        }
        int frameCount = 2;
        int frameW = sheet.getWidth() / frameCount;
        int frameH = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameW, 0, frameW, frameH);
        }
        spriteFrames.put("npc_questgiver", frames);
        System.out.println("[DEBUG AssetLoader] Loaded npc_questgiver => "
                + frameCount + " frames (" + frameW + "x" + frameH + " each)");
    }

    private static final int CHEST_SIZE = 64;
    private static final int ITEM_ICON_SIZE = 64;

    /**
     * Icon vật phẩm trong túi đồ/shop. Đa số dùng chung 1 icon theo ItemType
     * (key "item_"+type), riêng Potion có 3 biến thể cùng ItemType.POTION nên
     * dùng key riêng theo spriteName ("item_potion_heal/regen/exp" — xem
     * Potion.spriteName). Chỉ load type/biến thể nào game thực sự đang tạo
     * instance — ARMOR/SCROLL/QUEST_ITEM chưa có item thật.
     */
    private void loadItemIcons() {
        String base = Constants.SPRITES_PATH + "items/icons/";
        tileImages.put("item_potion_heal", loadImageScaled(base + "potion_heal.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
        tileImages.put("item_potion_regen", loadImageScaled(base + "potion_regen.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
        tileImages.put("item_potion_exp", loadImageScaled(base + "potion_exp.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
        tileImages.put("item_weapon", loadImageScaled(base + "weapon.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
        tileImages.put("item_material", loadImageScaled(base + "material.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
        // Tái dùng sprite bom sẵn có (effects/bomb.png) — không cần thêm asset mới
        tileImages.put("item_consumable",
                loadImageScaled(Constants.SPRITES_PATH + "effects/bomb.png", ITEM_ICON_SIZE, ITEM_ICON_SIZE));
    }

    private void loadChest() {
        String base = Constants.SPRITES_PATH + "items/chest/";

        // Common chest (brown)
        tileImages.put("chest_common_closed", loadImageScaled(base + "common/common_no_glow.png", CHEST_SIZE, CHEST_SIZE));
        tileImages.put("chest_common_open", loadImageScaled(base + "common/common_empty_no_glow.png", CHEST_SIZE, CHEST_SIZE));

        // Rare chest (blue)
        tileImages.put("chest_rare_closed", loadImageScaled(base + "rare/rare_no_glow.png", CHEST_SIZE, CHEST_SIZE));
        tileImages.put("chest_rare_open", loadImageScaled(base + "rare/rare_empty_no_glow.png", CHEST_SIZE, CHEST_SIZE));

        // Mythic chest (gold/purple gem)
        tileImages.put("chest_mythic_closed", loadImageScaled(base + "mythic/mythical_no_glow.png", CHEST_SIZE, CHEST_SIZE));
        tileImages.put("chest_mythic_open", loadImageScaled(base + "mythic/mythical_empty_no_glow.png", CHEST_SIZE, CHEST_SIZE));
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

    private static final int FROG_FRAME_SIZE = 32; // mỗi frame frog spritesheet là 32x32

    private void loadMonsters() {
        String base = Constants.SPRITES_PATH + "monsters/";

        // Orc (đã có sẵn asset trong resources/sprites/monsters/orc/)
        loadMonsterSheet("orc_idle", base + "orc/Orc_Idle.png");
        loadMonsterSheet("orc_walk", base + "orc/Orc_Walk.png");
        loadMonsterSheet("orc_attack", base + "orc/Orc_Attack01.png");
        loadMonsterSheet("orc_hurt", base + "orc/Orc_Hurt.png");
        loadMonsterSheet("orc_dead", base + "orc/Orc_Death.png");

        // Frog (SwarmCreature) — spritesheet 512x512, frame 32x32, 16 cột x 16 hàng
        // Row 1 (index 1) = di chuyển sang phải, Row 7 (index 7) = di chuyển sang trái
        loadFrogFrames(base + "frog_GameBoy_Green_spritesheet.png");
    }

    private void loadMonsterSheet(String name, String path) {
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            spriteFrames.put(name, new BufferedImage[0]);
            return;
        }

        int monsterFrameSize = 100;
        SpriteSheet sheetCutter = new SpriteSheet(sheet, monsterFrameSize, monsterFrameSize);
        BufferedImage[] frames = sheetCutter.getFullRow(0);
        spriteFrames.put(name, frames);
    }

    /**
     * Load frog spritesheet (512x512, frame 32x32).
     * Mỗi hàng có 16 cột nhưng chỉ dùng 2 nhóm:
     *   - Cột 0-7  → "idle" (ếch đứng yên)
     *   - Cột 8-13 → "walk" (ếch nhảy/di chuyển)
     * Áp dụng cho:
     *   - Row 1 (index 1) → hướng sang phải
     *   - Row 7 (index 7) → hướng sang trái
     * Lọc bỏ frame trống (toàn pixel trong suốt).
     */
    private void loadFrogFrames(String path) {
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            System.err.println("[AssetLoader] WARN: Could not load frog spritesheet: " + path);
            spriteFrames.put("frog_idle_right", new BufferedImage[0]);
            spriteFrames.put("frog_idle_left", new BufferedImage[0]);
            spriteFrames.put("frog_walk_right", new BufferedImage[0]);
            spriteFrames.put("frog_walk_left", new BufferedImage[0]);
            return;
        }

        SpriteSheet cutter = new SpriteSheet(sheet, FROG_FRAME_SIZE, FROG_FRAME_SIZE);

        // --- Row 1 (index 1): hướng phải ---
        BufferedImage[] idleRightFrames = extractColumnRange(cutter, 1, 0, 7);
        BufferedImage[] walkRightFrames = extractColumnRange(cutter, 1, 8, 11);
        spriteFrames.put("frog_idle_right", idleRightFrames);
        spriteFrames.put("frog_walk_right", walkRightFrames);

        // --- Row 7 (index 7): hướng trái ---
        BufferedImage[] idleLeftFrames = extractColumnRange(cutter, 7, 0, 7);
        BufferedImage[] walkLeftFrames = extractColumnRange(cutter, 7, 8, 11);
        spriteFrames.put("frog_idle_left", idleLeftFrames);
        spriteFrames.put("frog_walk_left", walkLeftFrames);

        System.out.println("[DEBUG AssetLoader] Loaded frog_idle_right => " + idleRightFrames.length + " frames");
        System.out.println("[DEBUG AssetLoader] Loaded frog_walk_right => " + walkRightFrames.length + " frames");
        System.out.println("[DEBUG AssetLoader] Loaded frog_idle_left  => " + idleLeftFrames.length + " frames");
        System.out.println("[DEBUG AssetLoader] Loaded frog_walk_left  => " + walkLeftFrames.length + " frames");
    }

    /**
     * Trích xuất các frame từ cột startCol đến endCol (inclusive) trên hàng row.
     * Lọc bỏ frame trống.
     */
    private BufferedImage[] extractColumnRange(SpriteSheet cutter, int row, int startCol, int endCol) {
        java.util.List<BufferedImage> result = new java.util.ArrayList<>();
        for (int col = startCol; col <= endCol; col++) {
            BufferedImage frame = cutter.getFrame(col, row);
            if (frame != null && !isFrameEmpty(frame)) {
                result.add(frame);
            }
        }
        return result.toArray(new BufferedImage[0]);
    }

    /**
     * Lọc bỏ các frame trống (toàn pixel alpha = 0) khỏi mảng.
     * Spritesheet frog có nhiều ô trống ở cuối mỗi hàng.
     */
    private boolean isFrameEmpty(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    return false; // có ít nhất 1 pixel không trong suốt → không trống
                }
            }
        }
        return true;
    }


    /**
     * Load animation nổ bomb (Bomb_Explosion.png — sprite sheet 11 frame,
     * mỗi frame 80x48, cắt sẵn từ asset "explosion-b" gốc).
     * Frame 0 = quả bom (chấm đỏ) lúc chưa nổ, frame 1-10 = các giai đoạn nổ.
     * Kích thước frame KHÁC PLAYER_FRAME_SIZE nên không dùng loadAnimationSheet().
     */
    private static final int BOMB_FRAME_WIDTH = 80;
    private static final int BOMB_FRAME_HEIGHT = 48;

    private void loadBomb() {
        // Load bomb.png (hình thùng bom pixel art) dùng vẽ khi PLACED
        String bombImgPath = Constants.SPRITES_PATH + "effects/bomb.png";
        BufferedImage bombImg = loadImageScaled(bombImgPath, Constants.BOMB_IDLE_DRAW_SIZE, Constants.BOMB_IDLE_DRAW_SIZE);
        if (bombImg != null) {
            tileImages.put("bomb_placed", bombImg);
            System.out.println("[DEBUG AssetLoader] Loaded bomb_placed from bomb.png ("
                    + bombImg.getWidth() + "x" + bombImg.getHeight() + ")");
        } else {
            System.err.println("[AssetLoader] Could not load bomb.png: " + bombImgPath);
        }

        String path = Constants.SPRITES_PATH + "effects/bomb/Bomb_Explosion.png";
        BufferedImage sheet = loadImage(path);
        if (sheet == null) {
            System.err.println("[AssetLoader] Could not load bomb explosion sprite: " + path);
            spriteFrames.put("bomb_idle", new BufferedImage[0]);
            spriteFrames.put("bomb_explosion", new BufferedImage[0]);
            return;
        }
        SpriteSheet cutter = new SpriteSheet(sheet, BOMB_FRAME_WIDTH, BOMB_FRAME_HEIGHT);
        BufferedImage[] allFrames = cutter.getFullRow(0);

        if (allFrames.length >= 11) {
            // Frame 0 riêng: quả bom lúc đặt xuống, chưa nổ
            spriteFrames.put("bomb_idle", new BufferedImage[] { allFrames[0] });
            // Frame 1-10: animation lúc nổ
            BufferedImage[] explosionFrames = new BufferedImage[allFrames.length - 1];
            System.arraycopy(allFrames, 1, explosionFrames, 0, explosionFrames.length);
            spriteFrames.put("bomb_explosion", explosionFrames);
        } else {
            System.err.println("[AssetLoader] Bomb sheet has fewer than 11 frames: " + allFrames.length);
            spriteFrames.put("bomb_idle", new BufferedImage[0]);
            spriteFrames.put("bomb_explosion", new BufferedImage[0]);
        }
        System.out.println("[DEBUG AssetLoader] Loaded bomb sheet => " + allFrames.length + " frames total");
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
     * Load ảnh và resize về đúng kích thước (dùng cho ảnh gốc lớn, vd chest 2000x2000).
     */
    private BufferedImage loadImageScaled(String path, int width, int height) {
        BufferedImage src = loadImage(path);
        if (src == null) {
            return null;
        }
        Image scaled = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return result;
    }

    /**
     * Lấy mảng frame animation theo tên.
     * 
     * @param name tên sprite (ví dụ: "player_walk_down", "orc_idle")
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
    // TODO: Phương thức helper
    // private BufferedImage loadImage(String path) { ... } — đọc 1 file ảnh bằng
    // ImageIO.read()
    // private BufferedImage[] loadFrames(String basePath, int count) { ... } — load
    // nhiều frame
}
