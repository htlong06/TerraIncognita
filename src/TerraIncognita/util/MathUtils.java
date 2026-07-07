package TerraIncognita.util;

import java.util.Random;

/**
 * Tiện ích tính toán dùng chung.
 */
public class MathUtils {

    private static final Random random = new Random();

    /**
     * Tính khoảng cách Manhattan giữa 2 điểm trên lưới.
     */
    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Tính khoảng cách Euclidean giữa 2 điểm.
     */
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Clamp giá trị trong khoảng [min, max].
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Sinh số ngẫu nhiên trong khoảng [min, max] (inclusive).
     */
    public static int randomRange(int min, int max) {
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Kiểm tra xác suất (ví dụ: chance = 0.1 → 10% trả true).
     */
    public static boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    // Không cho phép khởi tạo
    private MathUtils() {}
}
