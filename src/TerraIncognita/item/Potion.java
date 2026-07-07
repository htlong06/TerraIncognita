package TerraIncognita.item;

/**
 * Bình hồi phục — dùng 1 lần để hồi HP hoặc buff.
 */
public class Potion extends Item {

    private int healAmount;

    public Potion(String id, String name, int healAmount) {
        super(id, name, ItemType.POTION);
        this.healAmount = healAmount;
        this.stackable = true;
        this.maxStack = 10;
    }

    public int getHealAmount() { return healAmount; }
}
