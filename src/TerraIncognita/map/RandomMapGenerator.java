package TerraIncognita.map;

import java.util.List;
import java.util.Random;

/**
 * Sinh bản đồ ngẫu nhiên bằng thuật toán Random Room + Corridor.
 *
 * Thuật toán gợi ý:
 * 1. Khởi tạo toàn bộ map là WALL
 * 2. Sinh N phòng chữ nhật ngẫu nhiên (không chồng nhau)
 * 3. Nối các phòng bằng hành lang (corridor) L-shaped
 * 4. Đặt cầu thang, quái, item, bẫy... trong các phòng
 * 5. Đặt vị trí xuất phát player ở phòng đầu tiên
 */
public class RandomMapGenerator implements MapGenerator {

    // TODO: Khai báo các trường
    // - Random random
    // - int minRoomSize, maxRoomSize    — kích thước phòng tối thiểu/tối đa
    // - int maxRooms                    — số phòng tối đa cố gắng sinh

    public RandomMapGenerator() {
        // TODO: Khởi tạo Random, đặt giá trị mặc định cho kích thước phòng
    }

    @Override
    public GameMap generate(int width, int height, int difficulty) {
        // TODO: Implement thuật toán sinh map
        // 1. Tạo GameMap mới, fill toàn bộ WALL
        // 2. Gọi generateRooms() → danh sách Room
        // 3. Đào các phòng (set tile = FLOOR)
        // 4. Gọi connectRooms() → nối phòng bằng corridor
        // 5. Gọi placeStairs() → đặt cầu thang ở phòng cuối
        // 6. Gọi placeEntities() → đặt quái, item, bẫy theo difficulty
        // 7. Set vị trí xuất phát player ở trung tâm phòng đầu
        return null;
    }

    // TODO: Các phương thức helper
    // private List<Room> generateRooms(int width, int height) { ... }
    // private boolean doesRoomOverlap(Room room, List<Room> existing) { ... }
    // private void carveRoom(GameMap map, Room room) { ... }
    // private void connectRooms(GameMap map, List<Room> rooms) { ... }
    // private void carveCorridor(GameMap map, int x1, int y1, int x2, int y2) { ... }
    // private void placeStairs(GameMap map, List<Room> rooms) { ... }
    // private void placeEntities(GameMap map, List<Room> rooms, int difficulty) { ... }
}
