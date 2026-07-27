package TerraIncognita.collision;

import TerraIncognita.entity.Direction;
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
 */
public class CollisionManager {

    /**
     * Kiểm tra nếu entity di chuyển tới vị trí (newX, newY) thì hitbox
     * của nó có chồng lên tile không đi được không.
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

    /**
     * Kiểm tra 2 entity có đang chồng hitbox lên nhau không.
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

    /**
     * Tính hitbox tấn công: 1 hình chữ nhật nhô ra phía trước hitbox của
     * attacker theo hướng đang quay mặt, độ dài = rangeLength (px), bề
     * ngang/dọc bằng đúng bề ngang/dọc hitbox của attacker.
     */
    public Rectangle getAttackHitbox(Entity attacker, int rangeLength) {
        Rectangle base = attacker.getHitbox();
        Direction dir = attacker.getDirection();

        System.out.println("[DEBUG CollisionMgr.getAttackHitbox] dir=" + dir
                + " rangeLength=" + rangeLength
                + " baseHitbox=[x=" + base.x + " y=" + base.y
                + " w=" + base.width + " h=" + base.height + "]");

        Rectangle result;
        switch (dir) {
            case UP:
                result = new Rectangle(base.x, base.y - rangeLength, base.width, rangeLength);
                break;
            case DOWN:
                result = new Rectangle(base.x, base.y + base.height, base.width, rangeLength);
                break;
            case LEFT:
                result = new Rectangle(base.x - rangeLength, base.y, rangeLength, base.height);
                break;
            case RIGHT:
                result = new Rectangle(base.x + base.width, base.y, rangeLength, base.height);
                break;
            default:
                result = base;
                break;
        }
        System.out.println("[DEBUG CollisionMgr.getAttackHitbox] => result=[x=" + result.x
                + " y=" + result.y + " w=" + result.width + " h=" + result.height + "]");
        return result;
    }

    /**
     * Tìm mục tiêu đầu tiên trong danh sách bị hitbox tấn công chạm
     * trúng. Bỏ qua attacker và các entity đã chết.
     *
     * @return entity đầu tiên bị trúng đòn, hoặc null nếu không trúng ai
     */
    public Entity findAttackTarget(Entity attacker, Rectangle attackHitbox, List<? extends Entity> targets) {
        System.out.println("[DEBUG CollisionMgr.findAttackTarget] attackHitbox=["
                + "x=" + attackHitbox.x + " y=" + attackHitbox.y
                + " w=" + attackHitbox.width + " h=" + attackHitbox.height + "]"
                + " targetsCount=" + (targets == null ? "null" : targets.size()));
        if (targets == null) {
            return null;
        }
        for (int i = 0; i < targets.size(); i++) {
            Entity target = targets.get(i);
            if (target == attacker || !target.isAlive()) {
                System.out.println("[DEBUG CollisionMgr.findAttackTarget]   [" + i + "] '" + target.getName()
                        + "' SKIPPED (self=" + (target == attacker) + " alive=" + target.isAlive() + ")");
                continue;
            }
            Rectangle tBox = target.getHitbox();
            boolean hit = attackHitbox.intersects(tBox);
            System.out.println("[DEBUG CollisionMgr.findAttackTarget]   [" + i + "] '" + target.getName()
                    + "' hitbox=[x=" + tBox.x + " y=" + tBox.y
                    + " w=" + tBox.width + " h=" + tBox.height + "]"
                    + " INTERSECTS=" + hit);
            if (hit) {
                System.out.println("[DEBUG CollisionMgr.findAttackTarget] => HIT target='" + target.getName() + "'");
                return target;
            }
        }
        System.out.println("[DEBUG CollisionMgr.findAttackTarget] => NO TARGET found");
        return null;
    }
}