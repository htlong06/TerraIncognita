package TerraIncognita.map;

/**
 * Đại diện cho một ô trên bản đồ.
 */
public class Tile {

    private TileType type;
    private boolean revealed;       // Đã được khám phá chưa (Fog of War)
    private boolean visible;        // Đang nhìn thấy (trong tầm nhìn hiện tại)
    private int linkedId;           // ID liên kết (ví dụ: switch → door)

    public Tile(TileType type) {
        this.type = type;
        this.revealed = false;
        this.visible = false;
        this.linkedId = -1;
    }

    public boolean isWalkable() {
        return type.isWalkable();
    }

    public boolean isInteractable() {
        return type.isInteractable();
    }

    // --- Getter / Setter ---
    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }
    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean revealed) { this.revealed = revealed; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public int getLinkedId() { return linkedId; }
    public void setLinkedId(int linkedId) { this.linkedId = linkedId; }
}
