package TerraIncognita.entity.monster;

/**
 * Quái Skeleton — loại quái mạnh hơn Slime.
 *
 * Đặc điểm:
 * - HP trung bình, ATK cao, DEF trung bình
 * - Di chuyển nhanh hơn Slime
 */
public class SkeletonMonster extends Monster {

    public SkeletonMonster(int tileX, int tileY) {
        super("Skeleton", 40, 10, 5, tileX, tileY);
        this.speed = 70;
        this.detectionRange = 5;
        this.expReward = 25;
        this.goldReward = 8;
    }
}
