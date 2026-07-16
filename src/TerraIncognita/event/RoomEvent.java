package TerraIncognita.event;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;

/**
 * Sự kiện ngẫu nhiên khi player vào phòng mới lần đầu.
 * Ví dụ: phục kích (spawn thêm quái), tìm item, phòng trống...
 */
public class RoomEvent implements GameEvent {

    public enum RoomEventType {
        AMBUSH, TREASURE_FIND, EMPTY
    }

    private RoomEventType eventType;
    private double probability;
    private String description;

    public RoomEvent(RoomEventType eventType, double probability) {
        this.eventType = eventType;
        this.probability = probability;
        this.description = "";
    }

    @Override
    public void execute(Player player, GameMap map) {
        switch (eventType) {
            case AMBUSH:
                description = "Phục kích! Quái xuất hiện!";
                break;
            case TREASURE_FIND:
                description = "Tìm được kho báu!";
                break;
            case EMPTY:
                description = "Phòng trống.";
                break;
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    public RoomEventType getEventType() { return eventType; }
    public double getProbability() { return probability; }
}
