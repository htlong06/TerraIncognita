package TerraIncognita.entity.monster;

import TerraIncognita.entity.Entity;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;

/**
 * Abstract class quái vật cơ sở.
 * Mỗi loại quái kế thừa và override chỉ số + hành vi riêng.
 */
public abstract class Monster extends Entity {

    protected MonsterAI ai;
    protected int detectionRange;
    protected int expReward;
    protected int goldReward;
    protected boolean aggro;

    public Monster(String name, int hp, int atk, int def, int tileX, int tileY) {
        super();
        this.name = name;
        this.maxHp = hp;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * Constants.TILE_SIZE;
        this.worldY = tileY * Constants.TILE_SIZE;
        this.detectionRange = Constants.DEFAULT_DETECTION_RANGE;
        this.expReward = 10;
        this.goldReward = 5;
        this.aggro = false;
        this.speed = 60;   // Quái di chuyển chậm hơn player
        this.ai = new MonsterAI();
    }

    public abstract void initAnimations(AssetLoader assets);

    @Override
    public void update(double deltaTime) {
        updateAnimation(deltaTime);
        updateStatusEffects(deltaTime);
    }

    /**
     * Cập nhật AI quái (cần biết vị trí player và map).
     */
    public void updateAI(Player player, GameMap map, double deltaTime) {
        ai.update(this, player, map, deltaTime);
    }

    // --- Getter ---
    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isAggro() { return aggro; }
    public void setAggro(boolean aggro) { this.aggro = aggro; }
    public int getDetectionRange() { return detectionRange; }
}
