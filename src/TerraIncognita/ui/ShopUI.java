package TerraIncognita.ui;

import TerraIncognita.economy.Shop;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Item;
import TerraIncognita.item.ItemType;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Giao diện cửa hàng — mua/bán item với NPC Merchant.
 * 2 mode: BUY (xem item shop, mua vào inventory) / SELL (xem item inventory, bán lấy gold).
 * Điều khiển: mũi tên di chuyển cursor, Enter mua/bán, S chuyển mode, ESC đóng.
 */
public class ShopUI {

    private boolean open = false;
    private boolean buyMode = true;      // true = BUY, false = SELL
    private int selectedIndex = 0;

    // Layout
    private static final int SLOT_SIZE = 56;
    private static final int COLS = 5;
    private static final int GRID_PADDING = 4;
    private static final int TITLE_HEIGHT = 36;
    private static final int INFO_HEIGHT = 60;

    // Màu theo ItemType (reuse từ InventoryUI)
    private static Color colorForType(ItemType type) {
        switch (type) {
            case POTION:     return new Color(220, 60, 60);
            case WEAPON:     return new Color(160, 160, 170);
            case ARMOR:      return new Color(70, 130, 220);
            case KEY:        return new Color(240, 200, 60);
            case SCROLL:     return new Color(180, 120, 220);
            case MATERIAL:   return new Color(140, 200, 100);
            case CONSUMABLE: return new Color(240, 150, 80);
            case QUEST_ITEM: return new Color(250, 230, 100);
            default:         return Color.GRAY;
        }
    }

    // -- State --

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        buyMode = true;
        selectedIndex = 0;
    }

    public void close() {
        open = false;
    }

    public void toggle() {
        if (open) close();
        else open();
    }

    public boolean isBuyMode() {
        return buyMode;
    }

    public void setMode(boolean buyMode) {
        this.buyMode = buyMode;
        this.selectedIndex = 0;
    }

    public void toggleMode() {
        setMode(!buyMode);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Di chuyển cursor. Trong BUY mode → duyệt item shop. SELL mode → duyệt item inventory.
     */
    public void moveCursor(Direction dir, Shop shop, Player player) {
        int count = buyMode ? shop.getItems().size() : player.getInventory().getUsedSlots();
        if (count == 0) return;

        int col = selectedIndex % COLS;
        int row = selectedIndex / COLS;

        switch (dir) {
            case UP:
                if (row > 0) row--;
                break;
            case DOWN:
                int newRow = row + 1;
                if (newRow * COLS + col < count) row = newRow;
                break;
            case LEFT:
                if (col > 0) col--;
                break;
            case RIGHT:
                if (col < COLS - 1 && row * COLS + col + 1 < count) col++;
                break;
            default:
                break;
        }

        int newIndex = row * COLS + col;
        if (newIndex >= 0 && newIndex < count) {
            selectedIndex = newIndex;
        }
    }

    // -- Render --

    /**
     * Vẽ shop overlay.
     */
    public void render(Graphics2D g2d, Shop shop, Player player) {
        if (!open || shop == null) return;

        int sw = Constants.SCREEN_WIDTH;
        int sh = Constants.SCREEN_HEIGHT;

        // Dim overlay
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, sw, sh);

        // Tính dimensions
        int gridWidth = COLS * SLOT_SIZE + (COLS - 1) * GRID_PADDING;
        int gridHeight = gridWidth; // ~square grid area
        int panelWidth = gridWidth + 40;
        int panelHeight = TITLE_HEIGHT + gridHeight + INFO_HEIGHT + 20;
        int panelX = (sw - panelWidth) / 2;
        int panelY = (sh - panelHeight) / 2;

        // Panel background
        g2d.setColor(new Color(20, 25, 35, 245));
        g2d.fillRect(panelX, panelY, panelWidth, panelHeight);
        g2d.setColor(new Color(100, 120, 160));
        g2d.drawRect(panelX, panelY, panelWidth, panelHeight);

        // Title — hiện mode + gold
        g2d.setColor(buyMode ? new Color(120, 200, 120) : new Color(200, 150, 120));
        g2d.setFont(g2d.getFont().deriveFont(18f));
        String modeText = buyMode ? "🏪 MUA HÀNG" : "💰 BÁN ĐỒ";
        g2d.drawString(modeText, panelX + 16, panelY + 24);

        // Gold player (góc phải title)
        String goldText = "Vàng: " + player.getGold() + "g";
        int goldW = g2d.getFontMetrics().stringWidth(goldText);
        g2d.setColor(new Color(240, 200, 60));
        g2d.drawString(goldText, panelX + panelWidth - goldW - 16, panelY + 24);

        // Grid
        int gridX = panelX + 20;
        int gridY = panelY + TITLE_HEIGHT + 6;

        int itemCount = buyMode ? shop.getItems().size() : player.getInventory().getUsedSlots();
        int totalSlots = buyMode ? shop.getItems().size() : player.getInventory().getMaxSlots();
        int displaySlots = Math.max(totalSlots, COLS * 4);

        for (int i = 0; i < displaySlots; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = gridX + col * (SLOT_SIZE + GRID_PADDING);
            int y = gridY + row * (SLOT_SIZE + GRID_PADDING);

            // Slot background
            if (i < itemCount) {
                g2d.setColor(new Color(45, 50, 65));
            } else {
                g2d.setColor(new Color(25, 28, 38));
            }
            g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);

            // Slot border
            g2d.setColor(new Color(80, 90, 110));
            g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);

            // Selected highlight
            if (i == selectedIndex && i < itemCount) {
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(x - 1, y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2);
                g2d.drawRect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
            }

            // Draw item icon
            if (i < itemCount) {
                Item item = buyMode ? shop.getItems().get(i) : player.getInventory().getItems().get(i);
                Color iconColor = colorForType(item.getType());

                int iconSize = SLOT_SIZE - 16;
                int iconX = x + 8;
                int iconY = y + 8;
                g2d.setColor(iconColor);
                g2d.fillRect(iconX, iconY, iconSize, iconSize);
                g2d.setColor(iconColor.darker());
                g2d.drawRect(iconX, iconY, iconSize, iconSize);

                // Stack count
                if (item.isStackable() && item.getStackCount() > 1) {
                    String count = String.valueOf(item.getStackCount());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(g2d.getFont().deriveFont(11f));
                    int countW = g2d.getFontMetrics().stringWidth(count);
                    g2d.drawString(count, x + SLOT_SIZE - countW - 4, y + SLOT_SIZE - 4);
                }
            }
        }

        // Info area
        int infoY = gridY + gridHeight + 10;
        if (selectedIndex >= 0 && selectedIndex < itemCount) {
            Item sel = buyMode ? shop.getItems().get(selectedIndex) : player.getInventory().getItems().get(selectedIndex);
            int price = buyMode ? shop.getPrice(sel) : sel.getSellPrice();

            g2d.setColor(new Color(200, 220, 240));
            g2d.setFont(g2d.getFont().deriveFont(13f));
            g2d.drawString("→ " + sel.getName(), panelX + 20, infoY + 16);

            g2d.setColor(new Color(150, 160, 180));
            g2d.setFont(g2d.getFont().deriveFont(11f));
            g2d.drawString("[" + sel.getType() + "]", panelX + 20, infoY + 34);

            // Giá
            String priceText = buyMode ? ("Giá mua: " + price + "g") : ("Giá bán: " + price + "g");
            g2d.setColor(new Color(240, 200, 60));
            int priceW = g2d.getFontMetrics().stringWidth(priceText);
            g2d.drawString(priceText, panelX + panelWidth - priceW - 20, infoY + 34);
        }

        // Hint
        g2d.setColor(new Color(120, 130, 150));
        g2d.setFont(g2d.getFont().deriveFont(11f));
        String hint = "Enter: Mua/Bán  |  S: Chuyển mode  |  Mũi tên: Di chuyển  |  ESC: Đóng";
        g2d.drawString(hint, panelX + 20, infoY + 52);
    }
}
