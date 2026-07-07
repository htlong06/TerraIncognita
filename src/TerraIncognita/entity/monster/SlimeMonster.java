package TerraIncognita.entity.monster;

/**
 * Quái Slime — loại quái yếu, xuất hiện ở tầng đầu.
 *
 * Đặc điểm:
 * - HP thấp, ATK thấp, DEF thấp
 * - Di chuyển chậm
 * - AI đơn giản: phát hiện player gần → đuổi theo
 */
public class SlimeMonster extends Monster {

    public SlimeMonster(int tileX, int tileY) {
        super("Slime", 20, 5, 2, tileX, tileY);
        this.speed = 40;
        this.detectionRange = 3;
        this.expReward = 10;
        this.goldReward = 3;
    }
}
