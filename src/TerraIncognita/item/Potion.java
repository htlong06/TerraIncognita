package TerraIncognita.item;

import TerraIncognita.entity.Player;

import java.util.Locale;

/**
 * Bình hồi phục — dùng 1 lần, hiệu ứng tuỳ Effect (bình đỏ hồi máu ngay,
 * bình xanh biển hồi máu dần theo thời gian, bình xanh lá cộng thẳng EXP).
 */
public class Potion extends Item {

    public enum Effect { HEAL, REGEN, EXP }

    private final Effect effect;
    private final int healAmount;      // dùng khi effect=HEAL
    private final int regenAmount;     // HP hồi mỗi giây, dùng khi effect=REGEN
    private final double regenDuration; // thời gian hiệu ứng REGEN (giây)
    private final int expAmount;       // dùng khi effect=EXP

    /** Bình đỏ — hồi máu ngay lập tức. */
    public Potion(String id, String name, int healAmount) {
        this(id, name, Effect.HEAL, healAmount, 0, 0, 0);
    }

    private Potion(String id, String name, Effect effect, int healAmount,
                   int regenAmount, double regenDuration, int expAmount) {
        super(id, name, ItemType.POTION);
        this.effect = effect;
        this.healAmount = healAmount;
        this.regenAmount = regenAmount;
        this.regenDuration = regenDuration;
        this.expAmount = expAmount;
        this.stackable = true;
        this.maxStack = 10;
        this.spriteName = "item_potion_" + effect.name().toLowerCase(Locale.ROOT);
    }

    /** Bình xanh biển — hồi máu dần theo thời gian (StatusEffect.REGEN). */
    public static Potion createRegen(String id, String name, int regenAmountPerSecond, double durationSeconds) {
        return new Potion(id, name, Effect.REGEN, 0, regenAmountPerSecond, durationSeconds, 0);
    }

    /** Bình xanh lá — cộng thẳng EXP cho nhân vật. */
    public static Potion createExp(String id, String name, int expAmount) {
        return new Potion(id, name, Effect.EXP, 0, 0, 0, expAmount);
    }

    @Override
    public boolean use(Player player) {
        if (stackCount <= 0) return false;
        switch (effect) {
            case HEAL:
                player.heal(healAmount);
                break;
            case REGEN:
                player.addStatusEffect(new StatusEffect(name, StatusEffect.EffectType.REGEN,
                        regenDuration, regenAmount));
                break;
            case EXP:
                player.addExp(expAmount);
                break;
        }
        stackCount--;
        return true;
    }

    @Override
    public Item copy() {
        Potion p = new Potion(id, name, effect, healAmount, regenAmount, regenDuration, expAmount);
        copyBaseFieldsTo(p);
        return p;
    }

    // --- Getter ---
    public Effect getEffect() { return effect; }
    public int getHealAmount() { return healAmount; }
    public int getRegenAmount() { return regenAmount; }
    public double getRegenDuration() { return regenDuration; }
    public int getExpAmount() { return expAmount; }
}
