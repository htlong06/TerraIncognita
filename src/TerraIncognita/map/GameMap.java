package TerraIncognita.map;

/**
 * Logical tile grid used for collision and map events.
 *
 * Rendering is handled separately by DungeonMapManager using the visual layers
 * loaded from the TMX file.
 */
public class GameMap {

    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private int playerStartX = 1;
    private int playerStartY = 1;

    public GameMap(int width, int height) {
        this(width, height, TileType.WALL);
    }

    public GameMap(int width, int height, TileType defaultType) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        fill(defaultType);
    }

    public Tile getTile(int x, int y) {
        if (!contains(x, y)) {
            return Tile.voidTile();
        }
        return tiles[y][x];
    }

    public void setTile(int x, int y, TileType type) {
        if (contains(x, y)) {
            tiles[y][x] = new Tile(type);
        }
    }

    public void setTile(int x, int y, Tile tile) {
        if (contains(x, y)) {
            tiles[y][x] = tile;
        }
    }

    public boolean isWalkable(int x, int y) {
        return getTile(x, y).isWalkable();
    }

    public boolean contains(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public void setPlayerStart(int x, int y) {
        if (contains(x, y)) {
            this.playerStartX = x;
            this.playerStartY = y;
        }
    }

    private void fill(TileType type) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(type);
            }
        }
    }
}
