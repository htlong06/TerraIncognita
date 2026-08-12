package TerraIncognita.map;

/**
 * One logical map cell.
 */
public class Tile {

    private static final Tile VOID = new Tile(TileType.VOID);

    private final TileType type;

    public Tile(TileType type) {
        this.type = type;
    }

    public static Tile voidTile() {
        return VOID;
    }

    public boolean isWalkable() {
        return type.isWalkable();
    }

    public TileType getType() {
        return type;
    }
}
