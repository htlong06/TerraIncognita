package TerraIncognita.event;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;

/**
 * Interface sự kiện game.
 * Các sự kiện: trap, room event, switch activation...
 */
public interface GameEvent {

    /**
     * Thực thi sự kiện.
     * @param player player hiện tại
     * @param map bản đồ đang chơi
     */
    void execute(Player player, GameMap map);

    /**
     * Lấy mô tả sự kiện (để hiện thông báo cho player).
     */
    String getDescription();
}
