package TerraIncognita.util;

/**
 * Vector 2D tối giản, mô phỏng các phép toán của PVector (Processing) dùng
 * trong ví dụ Flocking (Nature of Code, chp06_agents/NOC_6_09_Flocking):
 * add/sub/mult/div/mag/normalize/limit. Dùng cho thuật toán bay đàn ở
 * {@link TerraIncognita.event.SwarmEvent} để giữ code gần đúng cấu trúc
 * gốc của tài liệu tham khảo (dễ đối chiếu/tra cứu lại về sau).
 */
public class Vec2 {

    public double x;
    public double y;

    public Vec2() {
        this(0, 0);
    }

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 copy() {
        return new Vec2(x, y);
    }

    public void add(Vec2 v) {
        x += v.x;
        y += v.y;
    }

    public void sub(Vec2 v) {
        x -= v.x;
        y -= v.y;
    }

    public void mult(double n) {
        x *= n;
        y *= n;
    }

    public void div(double n) {
        if (n == 0) return;
        x /= n;
        y /= n;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y);
    }

    /** Đưa vector về độ dài 1 (giữ nguyên hướng). Không làm gì nếu vector = (0,0). */
    public void normalize() {
        double m = mag();
        if (m > 0.0001) {
            div(m);
        }
    }

    /** Đặt độ dài vector = n, giữ nguyên hướng. */
    public void setMag(double n) {
        normalize();
        mult(n);
    }

    /** Giới hạn độ dài vector không vượt quá max — dùng để giới hạn lực/tốc độ (steering behavior). */
    public void limit(double max) {
        if (mag() > max) {
            setMag(max);
        }
    }

    public double dist(Vec2 v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** a - b, trả về vector MỚI (không đổi a hay b) — dùng cho công thức "desired = target - position". */
    public static Vec2 sub(Vec2 a, Vec2 b) {
        return new Vec2(a.x - b.x, a.y - b.y);
    }
}
