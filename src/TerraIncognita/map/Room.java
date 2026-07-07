package TerraIncognita.map;

/**
 * Đại diện 1 phòng chữ nhật trên bản đồ.
 */
public class Room {

    private int x, y;               // toạ độ góc trên-trái (ô)
    private int width, height;      // kích thước phòng (ô)
    private boolean visited;        // player đã vào phòng này chưa
    private int id;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visited = false;
        this.id = 0;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }

    /**
     * Kiểm tra phòng này có chồng lên phòng khác không (có margin).
     */
    public boolean overlaps(Room other, int margin) {
        return x - margin < other.x + other.width &&
               x + width + margin > other.x &&
               y - margin < other.y + other.height &&
               y + height + margin > other.y;
    }

    /**
     * Kiểm tra toạ độ (px, py) có nằm trong phòng không.
     */
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    // --- Getter / Setter ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
