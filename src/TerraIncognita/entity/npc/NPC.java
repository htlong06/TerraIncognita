package TerraIncognita.entity.npc;

import TerraIncognita.entity.Entity;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.util.Constants;
import java.awt.Rectangle;

/**
 * Abstract class NPC — nhân vật không phải người chơi, đứng yên tại vị trí cố định.
 */
public abstract class NPC extends Entity {

    protected String dialogText;
    protected boolean interactable;

    public NPC(String name, int tileX, int tileY) {
        super();
        this.name = name;
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldX = tileX * Constants.TILE_SIZE;
        this.worldY = tileY * Constants.TILE_SIZE;
        this.speed = 0;
        this.interactable = true;
        this.dialogText = "";
        this.state = EntityState.IDLE;
    }

    @Override
    public void update(double deltaTime) {
        updateAnimation(deltaTime);
    }

    /**
     * Vùng tương tác mặc định của NPC — hình chữ nhật mở rộng 1 ô
     * TILE_SIZE ra mỗi phía so với vị trí world. Lớp con (như Merchant)
     * có thể override nếu cần vùng khác.
     * @return Rectangle bao phủ vùng tương tác
     */
    @Override
    public Rectangle getInteractionBounds() {
        int ts = Constants.TILE_SIZE;
        int x = (int) Math.round(worldX) - ts;
        int y = (int) Math.round(worldY) - ts;
        int w = ts * 3;
        int h = ts * 3;
        return new Rectangle(x, y, w, h);
    }

    /**
     * Xử lý khi player tương tác với NPC.
     */
    public abstract void interact(Player player);

    // --- Getter ---
    public String getDialogText() { return dialogText; }
    public boolean isInteractable() { return interactable; }
}
