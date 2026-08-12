package TerraIncognita.util;

/**
 * Hằng số cấu hình game.
 * Tập trung tất cả magic number vào đây để dễ điều chỉnh.
 */
public class Constants {

    // --- Cửa sổ ---
    public static final String GAME_TITLE = "Terra Incognita — Dungeon Explorer";
    public static final int SCREEN_WIDTH = 800; // pixel
    public static final int SCREEN_HEIGHT = 600; // pixel
    public static final int TARGET_FPS = 60;

    // --- Tile ---
    public static final int TILE_SIZE = 32; // pixel (khớp với kích thước ảnh tile)

    // --- Map ---
    public static final int MAP_WIDTH = 30; // số ô
    public static final int MAP_HEIGHT = 30; // số ô

    // --- Player mặc định ---
    public static final int PLAYER_START_HP = 100;
    public static final int PLAYER_START_ATK = 10;
    public static final int PLAYER_START_DEF = 5;
    public static final double PLAYER_SPEED = 150.0;  // pixel/giây

    // --- Chest sprite ---
    public static final int CHEST_FRAME_WIDTH = 40;   // pixel/frame trong Chests.png
    public static final int CHEST_FRAME_HEIGHT = 32;
    public static final int CHEST_COLS = 6;           // số frame mỗi hàng
    public static final double PLAYER_ATTACK_COOLDOWN = 0.3;
    public static final double PLAYER_ATTACK_DURATION = 0.3;
    public static final int PLAYER_ATTACK_RANGE = 50; // độ vươn xa của hitbox kiếm (pixel), tính từ mép hitbox

    // --- Cung (bắn tầm xa) ---
    public static final int PLAYER_BOW_RANGE = 160; // độ vươn xa của hitbox mũi tên (pixel) — xa hơn kiếm nhiều

    // --- Mũi tên (Arrow projectile) ---
    public static final double ARROW_SPEED = 400.0;    // tốc độ bay (pixel/giây)
    public static final double ARROW_MAX_RANGE = 500.0; // quãng đường bay tối đa trước khi tự hủy (pixel)
    public static final int ARROW_SPRITE_SIZE = 32;     // kích thước vẽ mũi tên (pixel)

    // --- Combo kiếm ---
    // Đánh 3 nhát liên tiếp (trong khoảng COMBO_RESET_WINDOW giây kể từ nhát
    // trước) -> nhát thứ 3 dùng frame Soldier_Attack02 + sát thương cao hơn.
    public static final double COMBO_RESET_WINDOW = 1.0; // giây — quá thời gian này không đánh tiếp thì mất chuỗi combo
    public static final double COMBO_FINISHER_DAMAGE_MULTIPLIER = 1.3; // +30% sát thương ở đòn thứ 3
    public static final double BOW_ATTACK_SPEED_MULTIPLIER = 0.4; // tốc độ di chuyển khi bắn cung = 40% bình thường

    public static final int PLAYER_SPRITE_SIZE = 200;
    // Vị trí "chân" thật (đáy bóng đổ nhân vật) trong khung sprite gốc 100x100 —
    // đo trực tiếp trên asset (Soldier_Idle/Walk/Attack/Hurt đều cho kết quả
    // y=60 ổn định), KHÔNG phải y=100 (đáy canvas) như công thức cũ từng giả định.
    // Dùng để neo sprite đúng vào đáy hitbox thay vì neo theo mép ảnh.
    public static final int PLAYER_FRAME_SIZE = 100;
    public static final int PLAYER_FEET_Y_IN_FRAME = 60;

    // --- Inventory ---
    public static final int INVENTORY_MAX_SLOTS = 20;

    // --- Combat ---
    public static final double CRIT_CHANCE = 0.1; // 10%
    public static final double CRIT_MULTIPLIER = 1.5;
    public static final double MISS_CHANCE = 0.05; // 5%

    // --- Bomb ---
    public static final int BOMB_SIZE = TILE_SIZE / 2;         // kích thước khối vuông đặt bom (pixel)
    public static final int BOMB_EXPLOSION_TILES = 3;          // phạm vi nổ 3x3 ô, tâm là ô đặt bom
    public static final double BOMB_EXPLOSION_DURATION = 0.3;  // giây — thời gian hiển thị vụ nổ trước khi biến mất
    public static final int BOMB_DAMAGE = 40;                  // sát thương gây ra cho mục tiêu trong phạm vi nổ

    // --- AI ---
    public static final int DEFAULT_DETECTION_RANGE = 5; // ô

    // --- Map Generation ---
    public static final int MIN_ROOM_SIZE = 4;
    public static final int MAX_ROOM_SIZE = 8;
    public static final int MAX_ROOMS = 8;

    // --- File paths ---
    public static final String SPRITES_PATH = "resources/sprites/";
    public static final String MAPS_PATH = "resources/maps/";
    public static final String DATA_PATH = "resources/data/";
    public static final String SAVES_PATH = "resources/saves/";

    // Không cho phép khởi tạo
    private Constants() {
    }
}
