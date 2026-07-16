package TerraIncognita.event;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;

/**
 * Sự kiện bẫy — kích hoạt khi player bước vào ô TRAP.
 */
public class TrapEvent implements GameEvent {

    private int damage;
    private boolean hidden;
    private boolean triggered;

    public TrapEvent(int damage, boolean hidden) {
        this.damage = damage;
        this.hidden = hidden;
        this.triggered = false;
    }

    @Override
    public void execute(Player player, GameMap map) {
        if (triggered) return;
        player.takeDamage(damage);
        triggered = true;
    }

    @Override
    public String getDescription() {
        return "Bạn bước vào bẫy! Mất " + damage + " HP.";
    }

    public int getDamage() { return damage; }
    public boolean isHidden() { return hidden; }
    public boolean isTriggered() { return triggered; }
}
