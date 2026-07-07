package TerraIncognita.event;

/**
 * Interface sự kiện game.
 * Các sự kiện: trap, room event, switch activation...
 */
public interface GameEvent {

    /**
     * Thực thi sự kiện.
     * @param context dữ liệu ngữ cảnh (player, map, vị trí...)
     */
    // void execute(EventContext context);

    /**
     * Lấy mô tả sự kiện (để hiện thông báo cho player).
     */
    String getDescription();
}
