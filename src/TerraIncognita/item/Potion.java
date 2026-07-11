package TerraIncognita.item;

import TerraIncognita.entity.Player;

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

    @Override
    public boolean use(Player player) {
        if (stackCount <= 0) return false;
        player.heal(healAmount);
        stackCount--;
        return true;
    }

    public int getHealAmount() { return healAmount; }
}
