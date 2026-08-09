package TerraIncognita;

import TerraIncognita.entity.Chest;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.npc.Merchant;
import TerraIncognita.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test interaction bounds và interaction priority.
 *
 * Kiểm tra:
 * 1. Player.getInteractionBounds() trả về Rectangle đúng vị trí/kích thước
 * 2. Chest.getInteractionBounds() trả về Rectangle đúng vị trí/kích thước
 * 3. Merchant.getInteractionBounds() trả về Rectangle đúng vị trí/kích thước
 * 4. Khi player đứng gần cả merchant VÀ chest → merchant được ưu tiên
 *    (interaction priority: merchant > chest)
 */
class GameEngineTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    // --- Player interaction bounds ---

    @Test
    void playerInteractionBounds_returnsRectangleAtPlayerPosition() {
        // Đặt player tại tile (5, 5) → world (160, 160)
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(5 * Constants.TILE_SIZE);

        Rectangle bounds = player.getInteractionBounds();

        assertNotNull(bounds);
        // Vùng tương tác mở rộng 1 ô mỗi phía: x = worldX - TILE_SIZE
        assertEquals(5 * Constants.TILE_SIZE - Constants.TILE_SIZE, bounds.x);
        assertEquals(5 * Constants.TILE_SIZE - Constants.TILE_SIZE, bounds.y);
        // Kích thước = 3 ô (1 lề + player + 1 lề)
        assertEquals(Constants.TILE_SIZE * 3, bounds.width);
        assertEquals(Constants.TILE_SIZE * 3, bounds.height);
    }

    @Test
    void playerInteractionBounds_coversAdjacentTiles() {
        // Player tại tile (2, 2) → world (64, 64)
        player.setWorldX(2 * Constants.TILE_SIZE);
        player.setWorldY(2 * Constants.TILE_SIZE);

        Rectangle bounds = player.getInteractionBounds();
        int ts = Constants.TILE_SIZE;

        // Vùng từ tile (1,1) đến (4,4) — bao phủ tile (1,1), (2,2), (3,3), (4,4)...
        // bounds.x = 64 - 32 = 32, bounds.y = 32, width = 96, height = 96
        // Tức là bao phủ từ (32,32) đến (128,128)
        assertEquals(32, bounds.x);
        assertEquals(32, bounds.y);
        assertEquals(96, bounds.width);
        assertEquals(96, bounds.height);

        // Tile (1,1) tại (32,32) nằm trong bounds
        assertTrue(bounds.contains(32, 32));
        // Tile (3,3) tại (96,96) nằm trong bounds
        assertTrue(bounds.contains(96, 96));
        // Tile (3,3) centre tại (112,112) nằm trong bounds
        assertTrue(bounds.contains(112, 112));
        // Tile (0,0) tại (0,0) KHÔNG nằm trong bounds
        assertFalse(bounds.contains(0, 0));
    }

    // --- Chest interaction bounds ---

    @Test
    void chestInteractionBounds_returnsCorrectRectangle() {
        Chest chest = new Chest(3, 4, "common");
        // worldX = 3 * 32 = 96, worldY = 4 * 32 = 128

        Rectangle bounds = chest.getInteractionBounds();
        int ts = Constants.TILE_SIZE;

        assertNotNull(bounds);
        assertEquals(96 - ts, bounds.x);   // 64
        assertEquals(128 - ts, bounds.y);  // 96
        assertEquals(ts * 3, bounds.width);  // 96
        assertEquals(ts * 3, bounds.height); // 96
    }

    @Test
    void chestInteractionBounds_playerAdjacent_overlaps() {
        // Chest tại tile (5, 5)
        Chest chest = new Chest(5, 5, "common");
        // Player tại tile (4, 5) — ngay bên trái chest
        player.setWorldX(4 * Constants.TILE_SIZE);
        player.setWorldY(5 * Constants.TILE_SIZE);

        Rectangle playerBounds = player.getInteractionBounds();
        Rectangle chestBounds = chest.getInteractionBounds();

        assertTrue(playerBounds.intersects(chestBounds),
                "Player ở tile kề chest phải có vùng tương tác giao nhau");
    }

    @Test
    void chestInteractionBounds_playerFarAway_noOverlap() {
        // Chest tại tile (0, 0)
        Chest chest = new Chest(0, 0, "common");
        // Player tại tile (10, 10) — rất xa
        player.setWorldX(10 * Constants.TILE_SIZE);
        player.setWorldY(10 * Constants.TILE_SIZE);

        Rectangle playerBounds = player.getInteractionBounds();
        Rectangle chestBounds = chest.getInteractionBounds();

        assertFalse(playerBounds.intersects(chestBounds),
                "Player ở xa chest không được có vùng tương tác giao nhau");
    }

    // --- Merchant interaction bounds ---

    @Test
    void merchantInteractionBounds_returnsCorrectRectangle() {
        Merchant merchant = new Merchant(6, 7);
        // worldX = 6 * 32 = 192, worldY = 7 * 32 = 224

        Rectangle bounds = merchant.getInteractionBounds();
        int ts = Constants.TILE_SIZE;

        assertNotNull(bounds);
        assertEquals(192 - ts, bounds.x);   // 160
        assertEquals(224 - ts, bounds.y);   // 192
        assertEquals(ts * 3, bounds.width);  // 96
        assertEquals(ts * 3, bounds.height); // 96
    }

    @Test
    void merchantInteractionBounds_playerAdjacent_overlaps() {
        // Merchant tại tile (3, 3)
        Merchant merchant = new Merchant(3, 3);
        // Player tại tile (2, 3) — ngay bên trái
        player.setWorldX(2 * Constants.TILE_SIZE);
        player.setWorldY(3 * Constants.TILE_SIZE);

        Rectangle playerBounds = player.getInteractionBounds();
        Rectangle merchantBounds = merchant.getInteractionBounds();

        assertTrue(playerBounds.intersects(merchantBounds),
                "Player ở tile kề merchant phải có vùng tương tác giao nhau");
    }

    // --- Interaction priority: merchant > chest ---

    /**
     * Helper: mô phỏng logic ưu tiên tương tác.
     * Khi player đứng gần cả merchant và chest, merchant được chọn trước.
     * @param player player cần kiểm tra
     * @param merchant merchant (có thể null)
     * @param chest chest (có thể null)
     * @return "merchant" nếu ưu tiên merchant, "chest" nếu chỉ chest, null nếu không ai
     */
    private String resolveInteractionPriority(Player player, Merchant merchant, Chest chest) {
        Rectangle playerBounds = player.getInteractionBounds();

        // Ưu tiên merchant trước
        if (merchant != null && playerBounds.intersects(merchant.getInteractionBounds())) {
            return "merchant";
        }
        // Sau đó mới đến chest
        if (chest != null && playerBounds.intersects(chest.getInteractionBounds())) {
            return "chest";
        }
        return null;
    }

    @Test
    void interactionPriority_playerNearBothMerchantAndChest_prefersMerchant() {
        // Merchant tại tile (5, 5), Chest tại tile (6, 5) — cạnh nhau
        Merchant merchant = new Merchant(5, 5);
        Chest chest = new Chest(6, 5, "common");

        // Player tại tile (5, 6) — ngay dưới merchant, cũng gần chest
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(6 * Constants.TILE_SIZE);

        // Player phải giao cả merchant và chest
        Rectangle playerBounds = player.getInteractionBounds();
        assertTrue(playerBounds.intersects(merchant.getInteractionBounds()),
                "Player phải nằm trong vùng tương tác của merchant");
        assertTrue(playerBounds.intersects(chest.getInteractionBounds()),
                "Player phải nằm trong vùng tương tác của chest");

        // Nhưng khi ưu tiên → merchant được chọn
        String result = resolveInteractionPriority(player, merchant, chest);
        assertEquals("merchant", result,
                "Khi player gần cả merchant và chest, merchant phải được ưu tiên");
    }

    @Test
    void interactionPriority_playerNearOnlyChest_returnsChest() {
        Merchant merchant = new Merchant(20, 20);  // xa
        Chest chest = new Chest(5, 5, "common");

        // Player tại tile (5, 6) — gần chest, xa merchant
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(6 * Constants.TILE_SIZE);

        String result = resolveInteractionPriority(player, merchant, chest);
        assertEquals("chest", result,
                "Khi player chỉ gần chest, kết quả tương tác phải là chest");
    }

    @Test
    void interactionPriority_playerNearOnlyMerchant_returnsMerchant() {
        Merchant merchant = new Merchant(5, 5);
        Chest chest = new Chest(20, 20, "common");  // xa

        // Player tại tile (5, 6) — gần merchant, xa chest
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(6 * Constants.TILE_SIZE);

        String result = resolveInteractionPriority(player, merchant, chest);
        assertEquals("merchant", result,
                "Khi player chỉ gần merchant, kết quả tương tác phải là merchant");
    }

    @Test
    void interactionPriority_playerNearNothing_returnsNull() {
        Merchant merchant = new Merchant(20, 20);
        Chest chest = new Chest(0, 0, "common");

        // Player ở giữa, xa cả hai
        player.setWorldX(10 * Constants.TILE_SIZE);
        player.setWorldY(10 * Constants.TILE_SIZE);

        String result = resolveInteractionPriority(player, merchant, chest);
        assertNull(result,
                "Khi player không gần ai, kết quả tương tác phải là null");
    }

    @Test
    void interactionPriority_playerDiagonalToBoth_prefersMerchant() {
        // Merchant tại (4, 4), Chest tại (6, 6) — player ở giữa nhưng gần cả hai
        Merchant merchant = new Merchant(4, 4);
        Chest chest = new Chest(6, 6, "common");

        // Player tại tile (5, 5) — giữa merchant và chest
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(5 * Constants.TILE_SIZE);

        Rectangle playerBounds = player.getInteractionBounds();
        assertTrue(playerBounds.intersects(merchant.getInteractionBounds()),
                "Player tại (5,5) phải nằm trong vùng tương tác merchant tại (4,4)");
        assertTrue(playerBounds.intersects(chest.getInteractionBounds()),
                "Player tại (5,5) phải nằm trong vùng tương tác chest tại (6,6)");

        String result = resolveInteractionPriority(player, merchant, chest);
        assertEquals("merchant", result,
                "Khi player gần cả hai theo đường chéo, merchant vẫn được ưu tiên");
    }
}
