package TerraIncognita.map;

import TerraIncognita.entity.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trữ dữ liệu bản đồ: mảng 2D tile và danh sách entity trên map.
 *
 * GameMap không tự vẽ — nó chỉ chứa dữ liệu.
 * Renderer sẽ đọc dữ liệu từ GameMap để vẽ.
 */
public class GameMap {

    private int width;                      // kích thước map (số ô)
    private int height;
    private Tile[][] tiles;                 // mảng 2D tile
    private List<Entity> entities;          // danh sách entity trên map
    private List<Room> rooms;               // danh sách phòng
    private int playerStartX, playerStartY; // vị trí xuất phát player (tile)
    private int stairX, stairY;             // vị trí cầu thang xuống (tile)

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        this.entities = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.playerStartX = 1;
        this.playerStartY = 1;

        // Khởi tạo tất cả tile = WALL mặc định
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(TileType.WALL);
            }
        }
    }

    /**
     * Lấy tile tại vị trí (x, y).
     * @return Tile, hoặc tile VOID nếu ngoài map
     */
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return new Tile(TileType.VOID);
        }
        return tiles[y][x];
    }

    /**
     * Đặt tile tại vị trí (x, y).
     */
    public void setTile(int x, int y, Tile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[y][x] = tile;
        }
    }

    /**
     * Kiểm tra vị trí (x, y) có đi qua được không.
     */
    public boolean isWalkable(int x, int y) {
        Tile tile = getTile(x, y);
        return tile.isWalkable();
    }

    /**
     * Lấy entity tại vị trí (x, y), nếu có.
     */
    public Entity getEntityAt(int x, int y) {
        for (Entity entity : entities) {
            if (entity.getTileX() == x && entity.getTileY() == y) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Thêm entity vào map.
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Xoá entity khỏi map.
     */
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    // --- Getter / Setter ---
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Tile[][] getTiles() { return tiles; }
    public List<Entity> getEntities() { return entities; }
    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
    public int getPlayerStartX() { return playerStartX; }
    public int getPlayerStartY() { return playerStartY; }
    public void setPlayerStart(int x, int y) { this.playerStartX = x; this.playerStartY = y; }
    public int getStairX() { return stairX; }
    public int getStairY() { return stairY; }
    public void setStairs(int x, int y) { this.stairX = x; this.stairY = y; }
}
