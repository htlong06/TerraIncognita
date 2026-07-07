package TerraIncognita.util;

/**
 * Hằng số cấu hình game.
 * Tập trung tất cả magic number vào đây để dễ điều chỉnh.
 */
public class Constants {

    // --- Cửa sổ ---
    public static final String GAME_TITLE = "Terra Incognita — Dungeon Explorer";
    public static final int SCREEN_WIDTH = 800;       // pixel
    public static final int SCREEN_HEIGHT = 600;      // pixel
    public static final int TARGET_FPS = 60;

    // --- Tile ---
    public static final int TILE_SIZE = 32;           // pixel (khớp với kích thước ảnh tile)

    // --- Map ---
    public static final int MAP_WIDTH = 30;           // số ô
    public static final int MAP_HEIGHT = 30;          // số ô

    // --- Player mặc định ---
    public static final int PLAYER_START_HP = 100;
    public static final int PLAYER_START_ATK = 10;
    public static final int PLAYER_START_DEF = 5;
    public static final double PLAYER_SPEED = 150.0;  // pixel/giây

    // --- Inventory ---
    public static final int INVENTORY_MAX_SLOTS = 20;

    // --- Combat ---
    public static final double CRIT_CHANCE = 0.1;     // 10%
    public static final double CRIT_MULTIPLIER = 1.5;
    public static final double MISS_CHANCE = 0.05;    // 5%

    // --- AI ---
    public static final int DEFAULT_DETECTION_RANGE = 5;  // ô

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
    private Constants() {}
}
