package TerraIncognita.collision;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Entity;
import TerraIncognita.entity.Player;
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

        int leftCol = Math.floorDiv(box.x, tileSize);
        int rightCol = Math.floorDiv(box.x + box.width - 1, tileSize);
        int topRow = Math.floorDiv(box.y, tileSize);
        int bottomRow = Math.floorDiv(box.y + box.height - 1, tileSize);

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
