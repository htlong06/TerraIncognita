package TerraIncognita.collision;

import TerraIncognita.entity.Entity;
import TerraIncognita.entity.Player;
import TerraIncognita.event.EventSystem;
import TerraIncognita.map.GameMap;
import TerraIncognita.map.TileType;
import TerraIncognita.util.Constants;

import java.awt.Rectangle;
import java.util.List;

/**
 * Lớp riêng chịu trách nhiệm xử lý va chạm cho toàn bộ game.
 *
 * Cơ chế lấy cảm hứng từ CollisionChecker trong dự án 2dGame
 * (https://github.com/thinhnguyendev1601/2dGame): mỗi Entity có một
 * "hitbox" (vùng va chạm) nhỏ hơn kích thước 1 tile một chút, và việc
 * kiểm tra va chạm luôn dựa trên hitbox đó — KHÔNG dựa trên toạ độ
 * worldX/worldY thô — để tránh trường hợp entity "dính" vào tường vì
 * phần rìa sprite trong suốt.
 *
 * Khác với 2dGame (di chuyển theo pixel nguyên, từng bước bằng speed),
 * project này di chuyển bằng double + deltaTime nên các phép kiểm tra
 * được viết lại để nhận toạ độ "thử" (candidate position) thay vì chỉ
 * dựa vào hướng di chuyển hiện tại.
 *
 * Class này KHÔNG giữ trạng thái game — chỉ chứa logic kiểm tra/xử lý,
 * nên có thể dùng chung cho Player, Monster, NPC.
 */
public class CollisionManager {

    // =========================================================
    // 1) VA CHẠM VỚI TILE (tường, nước, ô ngoài map...)
    // =========================================================

    /**
     * Kiểm tra nếu entity di chuyển tới vị trí (newX, newY) thì hitbox
     * của nó có chồng lên tile không đi được không.
     *
     * Tương đương checkTile() trong CollisionChecker của 2dGame, nhưng
     * kiểm tra hitbox tại vị trí "thử" thay vì tính lùi 1 bước theo speed.
     *
     * @return true nếu bị chặn (có va chạm), false nếu đi được
     */
    public boolean checkTileCollision(Entity entity, GameMap map, double newX, double newY) {
        if (map == null) {
            return false; // chưa có map (vd còn ở giai đoạn dev) -> không chặn
        }

        Rectangle box = entity.getHitboxAt(newX, newY);
        int tileSize = Constants.TILE_SIZE;

        int leftCol = box.x / tileSize;
        int rightCol = (box.x + box.width - 1) / tileSize;
        int topRow = box.y / tileSize;
        int bottomRow = (box.y + box.height - 1) / tileSize;

        // Kiểm tra cả 4 góc của hitbox, giống 4 điểm được CollisionChecker
        // gốc kiểm tra (entityLeftCol/RightCol/TopRow/BottomRow).
        return !map.isWalkable(leftCol, topRow)
                || !map.isWalkable(rightCol, topRow)
                || !map.isWalkable(leftCol, bottomRow)
                || !map.isWalkable(rightCol, bottomRow);
    }

    /**
     * Tính vị trí mới sau khi đã chặn va chạm tường, tách riêng trục X
     * và trục Y (cho phép entity "trượt" dọc theo tường thay vì bị kẹt
     * cứng khi đi chéo).
     *
     * @param dx độ dịch chuyển theo X mong muốn (có thể âm)
     * @param dy độ dịch chuyển theo Y mong muốn (có thể âm)
     * @return mảng {resolvedX, resolvedY} — vị trí world thực sự nên áp dụng
     */
    public double[] resolveMovement(Entity entity, GameMap map, double dx, double dy) {
        double resultX = entity.getWorldX();
        double resultY = entity.getWorldY();

        if (dx != 0) {
            double tryX = resultX + dx;
            if (!checkTileCollision(entity, map, tryX, resultY)) {
                resultX = tryX;
            }
        }

        if (dy != 0) {
            double tryY = resultY + dy;
            if (!checkTileCollision(entity, map, resultX, tryY)) {
                resultY = tryY;
            }
        }

        return new double[] { resultX, resultY };
    }

    // =========================================================
    // 2) VA CHẠM GIỮA CÁC ENTITY (player <-> monster, monster <-> monster...)
    // =========================================================

    /**
     * Kiểm tra 2 entity có đang chồng hitbox lên nhau không.
     * Tương đương checkPlayer()/checkEntity() trong CollisionChecker gốc,
     * nhưng dùng java.awt.Rectangle#intersects trực tiếp (project này
     * không cần double-buffer lại solidArea vì hitbox luôn tính theo
     * worldX/worldY hiện tại).
     */
    public boolean checkEntityCollision(Entity a, Entity b) {
        if (a == null || b == null || a == b) {
            return false;
        }
        return a.getHitbox().intersects(b.getHitbox());
    }

    /**
     * Tìm entity đầu tiên trong danh sách va chạm với "self".
     * Bỏ qua chính nó và các entity đã chết.
     *
     * Dùng cho: quái đụng player -> gây damage, player đụng quái -> combat, ...
     *
     * @return entity va chạm, hoặc null nếu không có
     */
    public Entity findCollidingEntity(Entity self, List<? extends Entity> others) {
        if (others == null) {
            return null;
        }
        for (Entity other : others) {
            if (other == self || !other.isAlive()) {
                continue;
            }
            if (checkEntityCollision(self, other)) {
                return other;
            }
        }
        return null;
    }

    /**
     * Kiểm tra nếu entity di chuyển tới (newX, newY) thì có va chạm với
     * bất kỳ entity nào trong danh sách không. Dùng trước khi thật sự
     * di chuyển (giống cách checkObject/checkEntity dùng "temp direction"
     * trong bản gốc), để chặn việc quái đi xuyên qua nhau.
     */
    public boolean checkEntityCollisionAt(Entity self, double newX, double newY, List<? extends Entity> others) {
        if (others == null) {
            return false;
        }
        Rectangle box = self.getHitboxAt(newX, newY);
        for (Entity other : others) {
            if (other == self || !other.isAlive()) {
                continue;
            }
            if (box.intersects(other.getHitbox())) {
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // 3) VA CHẠM VỚI BẪY (TRAP / TRAP_HIDDEN)
    // =========================================================

    /**
     * Kiểm tra hitbox của player có đang đứng trên ô bẫy (TRAP hoặc
     * TRAP_HIDDEN) không; nếu có thì kích hoạt sự kiện tương ứng qua
     * EventSystem (đăng ký với key "trap_{tileX}_{tileY}").
     *
     * Gọi hàm này mỗi khi player di chuyển xong (sau khi resolveMovement).
     *
     * @return true nếu player đang đứng trên ô bẫy (đã kích hoạt hoặc đã kích hoạt trước đó)
     */
    public boolean checkTrapTrigger(Player player, GameMap map, EventSystem eventSystem) {
        if (player == null || map == null || eventSystem == null) {
            return false;
        }

        Rectangle box = player.getHitbox();
        int tileSize = Constants.TILE_SIZE;
        // Dùng tâm hitbox để xác định ô player thực sự đang đứng lên,
        // tránh trigger nhầm khi chỉ mới chạm rìa ô bẫy.
        int centerTileX = (box.x + box.width / 2) / tileSize;
        int centerTileY = (box.y + box.height / 2) / tileSize;

        TileType type = map.getTile(centerTileX, centerTileY).getType();
        if (type == TileType.TRAP || type == TileType.TRAP_HIDDEN) {
            eventSystem.checkTileEvent(map, player, centerTileX, centerTileY);
            return true;
        }
        return false;
    }
}
