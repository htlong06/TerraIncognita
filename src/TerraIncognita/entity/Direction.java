package TerraIncognita.entity;

/**
 * Enum hướng quay mặt của entity.
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** Trả về -1, 0, +1 theo trục X */
    public int getDx() { return dx; }

    /** Trả về -1, 0, +1 theo trục Y */
    public int getDy() { return dy; }

    /** Trả về hướng ngược lại */
    public Direction opposite() {
        switch (this) {
            case UP:    return DOWN;
            case DOWN:  return UP;
            case LEFT:  return RIGHT;
            case RIGHT: return LEFT;
            default:    return this;
        }
    }
}
