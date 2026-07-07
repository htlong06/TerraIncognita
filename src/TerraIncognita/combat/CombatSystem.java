package TerraIncognita.combat;

import TerraIncognita.entity.Entity;
import TerraIncognita.util.Constants;
import java.util.Random;

/**
 * Hệ thống chiến đấu.
 * Xử lý tính sát thương, kết quả trận đánh, turn-based logic.
 *
 * Công thức gợi ý: damage = max(1, ATK_tấn_công - DEF_phòng_thủ)
 * Có thể thêm: crit hit, miss, bonus từ equipment.
 */
public class CombatSystem {

    private Random random;
    private double critChance;      // xác suất crit (ví dụ 0.1 = 10%)
    private double critMultiplier;  // hệ số crit (ví dụ 1.5x)
    private double missChance;      // xác suất trượt (ví dụ 0.05 = 5%)

    public CombatSystem() {
        this.random = new Random();
        this.critChance = Constants.CRIT_CHANCE;
        this.critMultiplier = Constants.CRIT_MULTIPLIER;
        this.missChance = Constants.MISS_CHANCE;
    }

    /**
     * Thực hiện 1 lượt tấn công.
     * @param attacker entity tấn công
     * @param defender entity bị tấn công
     * @return CombatResult chứa thông tin kết quả (damage, crit, miss...)
     */
    public CombatResult attack(Entity attacker, Entity defender) {
        // Kiểm tra miss
        boolean isMiss = random.nextDouble() < missChance;
        if (isMiss) {
            return new CombatResult(0, false, true, false);
        }

        // Tính damage
        int damage = Math.max(1, attacker.getAtk() - defender.getDef());

        // Kiểm tra crit
        boolean isCrit = random.nextDouble() < critChance;
        if (isCrit) {
            damage = (int) (damage * critMultiplier);
        }

        // Áp dụng damage
        defender.takeDamage(damage);

        return new CombatResult(damage, isCrit, false, !defender.isAlive());
    }

    /**
     * Kiểm tra 2 entity có đứng cạnh nhau không (để tấn công cận chiến).
     */
    public boolean isInMeleeRange(Entity a, Entity b) {
        int dx = Math.abs(a.getTileX() - b.getTileX());
        int dy = Math.abs(a.getTileY() - b.getTileY());
        return (dx + dy) == 1;
    }

    /**
     * Kết quả của một lượt tấn công.
     */
    public static class CombatResult {
        public final int damage;
        public final boolean isCrit;
        public final boolean isMiss;
        public final boolean targetDied;

        public CombatResult(int damage, boolean isCrit, boolean isMiss, boolean targetDied) {
            this.damage = damage;
            this.isCrit = isCrit;
            this.isMiss = isMiss;
            this.targetDied = targetDied;
        }
    }
}
