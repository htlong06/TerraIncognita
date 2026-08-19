package TerraIncognita.combat;

import TerraIncognita.entity.Entity;
import TerraIncognita.util.Constants;
import java.util.Random;

/**
 * Hệ thống chiến đấu.
 * Xử lý tính sát thương, kết quả trận đánh, turn-based logic.
 */
public class CombatSystem {

    private Random random;
    private double critChance;
    private double critMultiplier;
    private double missChance;

    public CombatSystem() {
        this.random = new Random();
        this.critChance = Constants.CRIT_CHANCE;
        this.critMultiplier = Constants.CRIT_MULTIPLIER;
        this.missChance = Constants.MISS_CHANCE;
    }

    /**
     * Thực hiện 1 lượt tấn công.
     * 
     * @param attacker entity tấn công
     * @param defender entity bị tấn công
     * @return CombatResult chứa thông tin kết quả (damage, crit, miss...)
     */
    public CombatResult attack(Entity attacker, Entity defender) {
        return attack(attacker, defender, 1.0);
    }

    /**
     * Thực hiện 1 lượt tấn công với hệ số nhân sát thương tuỳ chỉnh.
     * Dùng cho đòn combo thứ 3 của kiếm (sát thương cao hơn bình thường).
     *
     * @param damageMultiplier hệ số nhân lên sát thương gốc (1.0 = bình thường)
     */
    public CombatResult attack(Entity attacker, Entity defender, double damageMultiplier) {
        // Kiểm tra miss
        double missRoll = random.nextDouble();
        boolean isMiss = missRoll < missChance;

        if (isMiss) {
            return new CombatResult(0, false, true, false);
        }

        // Tính damage, áp dụng hệ số nhân (combo) trước khi xét crit
        int baseDamage = Math.max(1, attacker.getAtk() - defender.getDef());
        int damage = Math.max(1, (int) Math.round(baseDamage * damageMultiplier));

        // Kiểm tra crit
        double critRoll = random.nextDouble();
        boolean isCrit = critRoll < critChance;
        if (isCrit) {
            damage = (int) (damage * critMultiplier);
        }

        // Áp dụng damage
        defender.takeDamage(damage);

        return new CombatResult(damage, isCrit, false, !defender.isAlive());
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
