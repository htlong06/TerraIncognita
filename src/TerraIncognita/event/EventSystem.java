package TerraIncognita.event;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.map.TileType;
import java.util.HashMap;
import java.util.Map;

/**
 * Hệ thống xử lý sự kiện.
 * Quản lý và kích hoạt các sự kiện (trap, switch, room event, checkpoint...).
 * Độc lập với hệ thống combat.
 */
public class EventSystem {

    private Map<String, GameEvent> registeredEvents;

    public EventSystem() {
        this.registeredEvents = new HashMap<>();
    }

    /**
     * Đăng ký sự kiện theo ID.
     */
    public void registerEvent(String id, GameEvent event) {
        registeredEvents.put(id, event);
    }

    /**
     * Kiểm tra và kích hoạt sự kiện tại vị trí (tileX, tileY).
     * Gọi mỗi khi player bước vào ô mới.
     */
    public void checkTileEvent(GameMap map, Player player, int tileX, int tileY) {
        if (map == null || player == null) return;
        TileType tileType = map.getTile(tileX, tileY).getType();

        switch (tileType) {
            case TRAP:
            case TRAP_HIDDEN:
                String trapKey = "trap_" + tileX + "_" + tileY;
                GameEvent trapEvent = registeredEvents.get(trapKey);
                if (trapEvent != null) {
                    trapEvent.execute(player, map);
                }
                break;
            case CHECKPOINT:
                // TODO: trigger SaveManager when Task 9 is done
                break;
            default:
                break;
        }
    }

    /**
     * Kiểm tra room event khi player vào phòng mới.
     */
    public void checkRoomEvent() {
        // TODO: check when player enters new room
    }
}
