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
    public static final float CHEST_DRAW_SCALE = 1.5f; // vẽ to hơn 1 ô để tương xứng nhà/vật thể xung quanh
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
    public static final double MISS_CHANCE = 0.01;

    // --- Bomb ---
    public static final int BOMB_SIZE = TILE_SIZE / 2;         // kích thước khối vuông đặt bom (pixel)
    public static final int BOMB_EXPLOSION_TILES = 3;          // phạm vi nổ 3x3 ô, tâm là ô đặt bom
    public static final int BOMB_EXPLOSION_RENDER_OFFSET_Y = -16; // nâng animation nổ lên trên mặt đất một chút
    public static final double BOMB_EXPLOSION_DURATION = 0.3;  // giây — dự phòng khi animation chưa load được
    public static final int BOMB_EXPLOSION_FRAME_MS = 60;      // ms mỗi frame animation nổ (10 frame => ~600ms)
    public static final int BOMB_DAMAGE = 40;                  // sát thương gây ra cho mục tiêu trong phạm vi nổ
    public static final int BOMB_IDLE_DRAW_SIZE = TILE_SIZE;   // kích thước vẽ sprite bom lúc chưa nổ (pixel)

    // --- Sự kiện bầy quái (Swarm Event) ---
    // Vùng ban đầu (góc dưới-trái map) — quái chỉ quanh quẩn ở đây cho tới
    // khi player bước vào, tính bằng TILE (không phải pixel).
    // LƯU Ý: toạ độ này giả định góc dưới-trái map là nền có thể đi được;
    // nếu map thực tế có tường/vật cản ở đây, hãy chỉnh lại 4 số bên dưới
    // cho khớp khu vực trống trên bản đồ (spawn có auto né ô không đi được,
    // nhưng vẫn cần cả vùng đủ rộng để né có chỗ mà né).
    public static final int SWARM_ZONE_TILE_X = 1;
    public static final int SWARM_ZONE_TILE_Y = MAP_HEIGHT - 9; // 8 ô tính từ đáy map, chừa 1 ô lề
    public static final int SWARM_ZONE_TILE_W = 8;
    public static final int SWARM_ZONE_TILE_H = 8;

    public static final int SWARM_COUNT = 20;              // số quái trong bầy — sửa ở đây để đổi số lượng
    public static final int SWARM_HP = 8;                    // máu mỗi con — thấp vì chỉ cần 1 vụ nổ bomb là đủ giết cả cụm
    public static final int SWARM_EXP_REWARD = 5;             // exp mỗi con khi bị bomb giết
    public static final int SWARM_GOLD_REWARD = 2;            // vàng mỗi con khi bị bomb giết
    public static final double SWARM_BASE_SPEED = 55;          // maxspeed lúc bình thường (pixel/giây) — tương đương "maxspeed" trong NOC Boid
    public static final double SWARM_PANIC_SPEED = 130;         // maxspeed khi đang hoảng loạn né player/bomb
    public static final double SWARM_MAX_FORCE = 120;            // maxforce — giới hạn độ lớn lực steering mỗi behavior (pixel/giây^2), tương đương "maxforce" trong NOC Boid
    public static final double SWARM_PANIC_RADIUS = 100;         // player đứng gần hơn khoảng cách này -> quái né ra xa (desiredSeparation cho flee)
    public static final double SWARM_NEIGHBOR_RADIUS = 90;        // bán kính "nhìn thấy" đồng loại để tính bay đàn
    public static final double SWARM_SEPARATION_RADIUS = 34;       // bán kính bắt đầu đẩy nhau ra (lực mềm, theo thuật toán boid)
    public static final double SWARM_MIN_SEPARATION_DISTANCE = 22; // khoảng cách TỐI THIỂU giữa 2 tâm quái — ép cứng mỗi frame để đảm bảo không chồng lấn hình ảnh (hitbox 12x12 -> chồng nếu < 12, đặt 22 để còn khoảng hở rõ mắt)
    public static final double SWARM_EXPLOSION_FLEE_RADIUS = 150;  // bán kính bị "hất văng" khi bomb nổ gần (rộng hơn vùng bomb gây sát thương)
    public static final double SWARM_EXPLOSION_PUSH_SPEED = 200;   // tốc độ bị hất văng ra xa tâm nổ (pixel/giây)

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
    public static final String BACKGROUND_MUSIC_PATH = "resources/audio/background.wav";
    public static final float BACKGROUND_MUSIC_VOLUME_DB = -30.0f;

    // Không cho phép khởi tạo
    private Constants() {
    }
}
