package TerraIncognita.event;

/**
 * Hệ thống xử lý sự kiện.
 * Quản lý và kích hoạt các sự kiện (trap, switch, room event, checkpoint...).
 * Độc lập với hệ thống combat.
 */
public class EventSystem {

    // TODO: Khai báo các trường
    // - Map<String, GameEvent> registeredEvents   — sự kiện đăng ký theo ID

    public EventSystem() {
        // TODO
    }

    /**
     * Kiểm tra và kích hoạt sự kiện tại vị trí (tileX, tileY).
     * Gọi mỗi khi player bước vào ô mới.
     */
    public void checkTileEvent(int tileX, int tileY /*, Player player, GameMap map */) {
        // TODO: Kiểm tra tile type tại vị trí
        // TODO: Nếu TRAP → kích hoạt TrapEvent
        // TODO: Nếu SWITCH → kích hoạt SwitchEvent
        // TODO: Nếu CHECKPOINT → lưu game
    }

    /**
     * Kiểm tra room event khi player vào phòng mới.
     */
    public void checkRoomEvent(/* Room room, Player player */) {
        // TODO: Nếu room chưa visited → xác suất sinh event
    }
}
