package TerraIncognita.entity.monster;

import java.awt.image.BufferedImage;

import TerraIncognita.entity.EntityState;
import TerraIncognita.graphics.AssetLoader;

/** Medium monster with close-range melee attacks. */
public class OrcMonster extends Monster {

    private static final int HP = 60;
    private static final int ATK = 12;
    private static final int DEF = 6;
    private static final int SPEED = 55;
    private static final int DETECTION_RANGE = 5;
    private static final int EXP_REWARD = 30;
    private static final int GOLD_REWARD = 12;

    private static final int IDLE_FRAME_MS = 150;
    private static final int WALK_FRAME_MS = 130;
    private static final int ATTACK_FRAME_MS = 120;
    private static final int HURT_FRAME_MS = 80;

    public OrcMonster(int tileX, int tileY) {
        super("Orc", HP, ATK, DEF, tileX, tileY);
        this.speed = SPEED;
        this.detectionRange = DETECTION_RANGE;
        this.expReward = EXP_REWARD;
        this.goldReward = GOLD_REWARD;
    }

    public void initAnimations(AssetLoader assets) {
        addOrcAnimation(assets, EntityState.IDLE, "orc_idle", IDLE_FRAME_MS, true);
        addOrcAnimation(assets, EntityState.WALK, "orc_walk", WALK_FRAME_MS, true);
        addOrcAnimation(assets, EntityState.ATTACK, "orc_attack", ATTACK_FRAME_MS, false);
        addOrcAnimation(assets, EntityState.HURT, "orc_hurt", HURT_FRAME_MS, false);

        useAnimation(EntityState.IDLE, getDirection());
    }

    private void addOrcAnimation(
            AssetLoader assets,
            EntityState state,
            String frameKey,
            int frameDurationMs,
            boolean looping
    ) {
        BufferedImage[] rightFrames = assets.getFrames(frameKey);
        BufferedImage[] leftFrames = assets.getFramesFlipped(frameKey);
        addDirectionalAnimations(state, rightFrames, leftFrames, frameDurationMs, looping);
    }
}
