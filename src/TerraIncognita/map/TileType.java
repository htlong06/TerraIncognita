package TerraIncognita.map;

/**
 * Enum định nghĩa các loại tile trên bản đồ.
 * Mỗi loại tile có thuộc tính: đi qua được không (walkable), tên ảnh tương ứng.
 */
public enum TileType {
    FLOOR(true, false, "floor"),
    WALL(false, false, "wall"),
    DOOR(true, true, "door"),
    DOOR_LOCKED(false, true, "door_locked"),
    STAIR_DOWN(true, true, "stairs_down"),
    STAIR_UP(true, true, "stairs_up"),
    TRAP(true, true, "trap"),
    TRAP_HIDDEN(true, true, "floor"),           // Nhìn giống FLOOR
    SWITCH(true, true, "switch"),
    SECRET_WALL(false, false, "wall"),           // Nhìn giống WALL
    CHEST_TILE(true, true, "chest"),
    CHECKPOINT(true, true, "checkpoint"),
    WATER(false, false, "water"),
    VOID(false, false, "void");

    private final boolean walkable;
    private final boolean interactable;
    private final String spriteName;

    TileType(boolean walkable, boolean interactable, String spriteName) {
        this.walkable = walkable;
        this.interactable = interactable;
        this.spriteName = spriteName;
    }

    public boolean isWalkable() { return walkable; }
    public boolean isInteractable() { return interactable; }
    public String getSpriteName() { return spriteName; }
}
