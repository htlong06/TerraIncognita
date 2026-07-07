package TerraIncognita.item;

/**
 * Trang bị (vũ khí/giáp) — item mang theo tăng chỉ số.
 */
public class Equipment extends Item {

    private EquipmentSlot slot;     // vị trí trang bị
    private int atkBonus;           // bonus ATK khi mang
    private int defBonus;           // bonus DEF khi mang
    private int upgradeLevel;       // cấp nâng cấp hiện tại (0, +1, +2...)

    public Equipment(String id, String name, EquipmentSlot slot, int atkBonus, int defBonus) {
        super(id, name, slot == EquipmentSlot.WEAPON ? ItemType.WEAPON : ItemType.ARMOR);
        this.slot = slot;
        this.atkBonus = atkBonus;
        this.defBonus = defBonus;
        this.upgradeLevel = 0;
        this.stackable = false;
    }

    /**
     * Nâng cấp trang bị (+1 level).
     */
    public void upgrade() {
        upgradeLevel++;
        atkBonus += 1;
        defBonus += 1;
    }

    // --- Getter ---
    public EquipmentSlot getSlot() { return slot; }
    public int getAtkBonus() { return atkBonus; }
    public int getDefBonus() { return defBonus; }
    public int getUpgradeLevel() { return upgradeLevel; }
}
