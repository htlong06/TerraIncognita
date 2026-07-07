package TerraIncognita.event;

/**
 * Sự kiện bẫy — kích hoạt khi player bước vào ô TRAP.
 */
public class TrapEvent implements GameEvent {

    // TODO: Khai báo các trường
    // - int damage           — sát thương gây ra
    // - boolean hidden       — bẫy ẩn hay hiện
    // - boolean triggered    — đã kích hoạt chưa (mỗi bẫy chỉ hoạt động 1 lần?)

    public TrapEvent(int damage, boolean hidden) {
        // TODO
    }

    @Override
    public String getDescription() {
        // TODO: return "Bạn bước vào bẫy! Mất " + damage + " HP."
        return "";
    }
}
