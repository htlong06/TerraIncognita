package TerraIncognita.entity.monster;

import java.awt.image.BufferedImage;

import TerraIncognita.entity.EntityState;
import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;

/**
 * Quái Orc — quái tầm trung, mạnh hơn Slime.
 *
 * Đặc điểm:
 * - HP cao hơn, atk mạnh hơn, tốc độ vừa phải
 * - Phát hiện player trong tầm gần và đuổi theo
 * - Gây sát thương khi vào cận chiến
 */
public class OrcMonster extends Monster {

    public OrcMonster(int tileX, int tileY) {
        super("Orc", 60, 12, 6, tileX, tileY);
        this.speed = 55;
        this.detectionRange = 5;
        this.expReward = 30;
        this.goldReward = 12;
    }

    public void initAnimations(AssetLoader assets) {
        BufferedImage[] idleRight = assets.getFrames("orc_idle");
        BufferedImage[] idleLeft = assets.getFramesFlipped("orc_idle");
        BufferedImage[] walkRight = assets.getFrames("orc_walk");
        BufferedImage[] walkLeft = assets.getFramesFlipped("orc_walk");
        BufferedImage[] attackRight = assets.getFrames("orc_attack");
        BufferedImage[] attackLeft = assets.getFramesFlipped("orc_attack");
        BufferedImage[] hurtRight = assets.getFrames("orc_hurt");
        BufferedImage[] hurtLeft = assets.getFramesFlipped("orc_hurt");

        if (idleRight != null && idleRight.length > 0) {
            Animation idleAnimRight = new Animation(idleRight, 150);
            idleAnimRight.setLooping(true);
            Animation idleAnimLeft = new Animation(idleLeft, 150);
            idleAnimLeft.setLooping(true);
            animations.put(EntityState.IDLE.name().toLowerCase() + "_right", idleAnimRight);
            animations.put(EntityState.IDLE.name().toLowerCase() + "_left", idleAnimLeft);
            animations.put(EntityState.IDLE.name().toLowerCase() + "_up", idleAnimRight);
            animations.put(EntityState.IDLE.name().toLowerCase() + "_down", idleAnimRight);

            currentAnimation = idleAnimRight;
        }

        if (walkRight != null && walkRight.length > 0) {
            Animation walkAnimRight = new Animation(walkRight, 130);
            walkAnimRight.setLooping(true);
            Animation walkAnimLeft = new Animation(walkLeft, 130);
            walkAnimLeft.setLooping(true);
            animations.put(EntityState.WALK.name().toLowerCase() + "_right", walkAnimRight);
            animations.put(EntityState.WALK.name().toLowerCase() + "_left", walkAnimLeft);
            animations.put(EntityState.WALK.name().toLowerCase() + "_up", walkAnimRight);
            animations.put(EntityState.WALK.name().toLowerCase() + "_down", walkAnimRight);
        }

        if (attackRight != null && attackRight.length > 0) {
            Animation attackAnimRight = new Animation(attackRight, 120);
            attackAnimRight.setLooping(false);
            Animation attackAnimLeft = new Animation(attackLeft, 120);
            attackAnimLeft.setLooping(false);
            animations.put(EntityState.ATTACK.name().toLowerCase() + "_right", attackAnimRight);
            animations.put(EntityState.ATTACK.name().toLowerCase() + "_left", attackAnimLeft);
            animations.put(EntityState.ATTACK.name().toLowerCase() + "_up", attackAnimRight);
            animations.put(EntityState.ATTACK.name().toLowerCase() + "_down", attackAnimRight);
        }

        if (hurtRight != null && hurtRight.length > 0) {
            Animation hurtAnimRight = new Animation(hurtRight, 80);
            hurtAnimRight.setLooping(false);
            Animation hurtAnimLeft = new Animation(hurtLeft, 80);
            hurtAnimLeft.setLooping(false);
            animations.put(EntityState.HURT.name().toLowerCase() + "_right", hurtAnimRight);
            animations.put(EntityState.HURT.name().toLowerCase() + "_left", hurtAnimLeft);
            animations.put(EntityState.HURT.name().toLowerCase() + "_up", hurtAnimRight);
            animations.put(EntityState.HURT.name().toLowerCase() + "_down", hurtAnimRight);
        }
    }
}
