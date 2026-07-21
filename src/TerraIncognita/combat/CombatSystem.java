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
        return attack(attacker, defender, 1.0);
    }

    /**
     * Thực hiện 1 lượt tấn công với hệ số nhân sát thương tuỳ chỉnh.
     * Dùng cho đòn combo thứ 3 của kiếm (sát thương cao hơn bình thường).
     *
     * @param damageMultiplier hệ số nhân lên sát thương gốc (1.0 = bình thường)
     */
    public CombatResult attack(Entity attacker, Entity defender, double damageMultiplier) {
        System.out.println("[DEBUG CombatSystem.attack] attacker='" + attacker.getName()
                + "' ATK=" + attacker.getAtk()
                + " | defender='" + defender.getName()
                + "' DEF=" + defender.getDef() + " HP=" + defender.getHp() + "/" + defender.getMaxHp()
                + " | multiplier=" + damageMultiplier);

        // Kiểm tra miss
        double missRoll = random.nextDouble();
        boolean isMiss = missRoll < missChance;
        System.out.println("[DEBUG CombatSystem.attack] missRoll=" + String.format("%.3f", missRoll)
                + " missChance=" + missChance + " => isMiss=" + isMiss);
        if (isMiss) {
            System.out.println("[DEBUG CombatSystem.attack] => MISS! Trả về damage=0");
            return new CombatResult(0, false, true, false);
        }

        // Tính damage, áp dụng hệ số nhân (combo) trước khi xét crit
        int baseDamage = Math.max(1, attacker.getAtk() - defender.getDef());
        int damage = Math.max(1, (int) Math.round(baseDamage * damageMultiplier));
        System.out.println("[DEBUG CombatSystem.attack] baseDamage=max(1, " + attacker.getAtk() + "-" + defender.getDef()
                + ")=" + baseDamage + " | afterMultiplier=" + damage);

        // Kiểm tra crit
        double critRoll = random.nextDouble();
        boolean isCrit = critRoll < critChance;
        System.out.println("[DEBUG CombatSystem.attack] critRoll=" + String.format("%.3f", critRoll)
                + " critChance=" + critChance + " => isCrit=" + isCrit);
        if (isCrit) {
            int beforeCrit = damage;
            damage = (int) (damage * critMultiplier);
            System.out.println("[DEBUG CombatSystem.attack] CRIT! damage " + beforeCrit + " * " + critMultiplier + " = " + damage);
        }

        // Áp dụng damage
        int hpBefore = defender.getHp();
        defender.takeDamage(damage);
        System.out.println("[DEBUG CombatSystem.attack] takeDamage(" + damage + ") HP: " + hpBefore + " => " + defender.getHp()
                + " alive=" + defender.isAlive());

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
