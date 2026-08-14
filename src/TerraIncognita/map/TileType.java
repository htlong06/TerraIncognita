package TerraIncognita.map;

/**
 * Logical tile types used by collision and events.
 */
public enum TileType {
    FLOOR(true),
    WALL(false),
    VOID(false);

    private final boolean walkable;

    TileType(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
