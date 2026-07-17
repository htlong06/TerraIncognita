package TerraIncognita.entity.monster;

import TerraIncognita.graphics.Animation;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.entity.EntityState;
import java.awt.image.BufferedImage;

/**
 * Quái Slime — loại quái yếu, xuất hiện ở tầng đầu.
 *
 * Đặc điểm:
 * - HP thấp, ATK thấp, DEF thấp
 * - Di chuyển chậm
 * - AI đơn giản: phát hiện player gần → đuổi theo
 */
public class SlimeMonster extends Monster {

    public SlimeMonster(int tileX, int tileY) {
        super("Slime", 20, 5, 2, tileX, tileY); // hp: 20, atk: 5, def: 2
        this.speed = 40; //
        this.detectionRange = 3; //
        this.expReward = 10; //
        this.goldReward = 3; //
    }

    /**
     * Khởi tạo hoạt ảnh đứng yên (idle) cho Slime từ Sprite Sheet
     */
    public void initAnimations(AssetLoader assets) {
        BufferedImage[] facingRight = assets.getFrames("slime_idle");
        BufferedImage[] facingLeft = assets.getFramesFlipped("slime_idle");

        if (facingRight == null || facingRight.length == 0) {
            return;
        }

        // Tạo đối tượng hoạt ảnh với thời gian chuyển frame là 130ms
        Animation animRight = new Animation(facingRight, 130);
        animRight.setLooping(true);
        Animation animLeft = new Animation(facingLeft, 130);
        animLeft.setLooping(true);

        // Đăng ký hoạt ảnh cho trạng thái IDLE tương ứng với các hướng quay mặt
        String prefix = EntityState.IDLE.name().toLowerCase() + "_";
        animations.put(prefix + "right", animRight);
        animations.put(prefix + "up", animRight);
        animations.put(prefix + "down", animRight);
        animations.put(prefix + "left", animLeft);
        
        // Gán animation mặc định ban đầu
        this.currentAnimation = animRight;
    }
}