package TerraIncognita.event;

/**
 * Sự kiện ngẫu nhiên khi player vào phòng mới lần đầu.
 * Ví dụ: phục kích (spawn thêm quái), tìm item, phòng trống...
 */
public class RoomEvent implements GameEvent {

    // TODO: Khai báo enum RoomEventType
    // AMBUSH, TREASURE_FIND, EMPTY, MERCHANT_VISIT

    // TODO: Khai báo các trường
    // - RoomEventType eventType
    // - double probability        — xác suất xảy ra

    public RoomEvent() {
        // TODO
    }

    @Override
    public String getDescription() {
        // TODO: Tuỳ theo eventType trả về mô tả phù hợp
        return "";
    }
}
