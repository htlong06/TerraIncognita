package TerraIncognita.map;

/**
 * Interface cho các thuật toán sinh bản đồ.
 * Cho phép dễ dàng thay đổi/thêm thuật toán sinh map mới (Strategy pattern).
 */
public interface MapGenerator {

    /**
     * Sinh bản đồ mới.
     * @param width chiều rộng map (số ô)
     * @param height chiều cao map (số ô)
     * @param difficulty độ khó (ảnh hưởng số quái, số phòng, item...)
     * @return GameMap đã được sinh
     */
    GameMap generate(int width, int height, int difficulty);
}
