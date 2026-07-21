package TerraIncognita.ui;

import TerraIncognita.entity.Direction;
import TerraIncognita.inventory.Inventory;
import TerraIncognita.item.Item;
import TerraIncognita.item.ItemType;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Giao diện túi đồ (Inventory).
 * Hiện khi player bấm I: lưới 5×4 slot, có thể di chuyển cursor và sử dụng item.
 */
public class InventoryUI {

    private int selectedIndex = 0;
    private boolean open = false;

    // Layout
    private static final int SLOT_SIZE = 64;
    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int GRID_PADDING = 4;
    private static final int TITLE_HEIGHT = 36;
    private static final int INFO_HEIGHT = 60;

    // Màu theo ItemType
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

    public void toggle() {
        open = !open;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Di chuyển cursor trong lưới. Không vượt qua số slot đã dùng.
     */
    public void moveCursor(Direction dir, Inventory inv) {
        int maxSlots = inv.getMaxSlots();
        if (maxSlots == 0) return;

        int col = selectedIndex % COLS;
        int row = selectedIndex / COLS;

        switch (dir) {
            case UP:
                if (row > 0) row--;
                break;
            case DOWN:
                int newRow = row + 1;
                if (newRow * COLS + col < maxSlots) row = newRow;
                break;
            case LEFT:
                if (col > 0) col--;
                break;
            case RIGHT:
                if (col < COLS - 1 && row * COLS + col + 1 < maxSlots) col++;
                break;
            default:
                break;
        }

        int newIndex = row * COLS + col;
        if (newIndex >= 0 && newIndex < maxSlots) {
            selectedIndex = newIndex;
        }
    }

    // -- Render --

    /**
     * Vẽ inventory overlay.
     * @param g2d  graphics context
     * @param inv  inventory để vẽ
     * @param px   toạ độ x gốc (thường = 0, toàn màn hình)
     * @param py   toạ độ y gốc
     */
    public void render(Graphics2D g2d, Inventory inv, int px, int py) {
        if (!open) return;

        int sw = Constants.SCREEN_WIDTH;
        int sh = Constants.SCREEN_HEIGHT;

        // Dim overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, sw, sh);

        // Tính grid dimensions
        int gridWidth = COLS * SLOT_SIZE + (COLS - 1) * GRID_PADDING;
        int gridHeight = ROWS * SLOT_SIZE + (ROWS - 1) * GRID_PADDING;
        int panelWidth = gridWidth + 40;
        int panelHeight = TITLE_HEIGHT + gridHeight + INFO_HEIGHT + 20;
        int panelX = (sw - panelWidth) / 2;
        int panelY = (sh - panelHeight) / 2;

        // Panel background
        g2d.setColor(new Color(20, 25, 35, 240));
        g2d.fillRect(panelX, panelY, panelWidth, panelHeight);
        g2d.setColor(new Color(100, 120, 160));
        g2d.drawRect(panelX, panelY, panelWidth, panelHeight);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(18f));
        String title = "Túi đồ (" + inv.getUsedSlots() + "/" + inv.getMaxSlots() + ")";
        g2d.drawString(title, panelX + 16, panelY + 24);

        // Grid
        int gridX = panelX + 20;
        int gridY = panelY + TITLE_HEIGHT + 6;

        for (int i = 0; i < COLS * ROWS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = gridX + col * (SLOT_SIZE + GRID_PADDING);
            int y = gridY + row * (SLOT_SIZE + GRID_PADDING);

            // Slot background
            if (i < inv.getUsedSlots()) {
                g2d.setColor(new Color(45, 50, 65));
            } else {
                g2d.setColor(new Color(25, 28, 38));
            }
            g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);

            // Slot border
            g2d.setColor(new Color(80, 90, 110));
            g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);

            // Selected highlight
            if (i == selectedIndex && i < inv.getUsedSlots()) {
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(x - 1, y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2);
                g2d.drawRect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
            }

            // Draw item icon
            if (i < inv.getUsedSlots()) {
                Item item = inv.getItems().get(i);
                Color iconColor = colorForType(item.getType());

                // Icon = colored square centered in slot
                int iconSize = SLOT_SIZE - 16;
                int iconX = x + 8;
                int iconY = y + 8;
                g2d.setColor(iconColor);
                g2d.fillRect(iconX, iconY, iconSize, iconSize);
                g2d.setColor(iconColor.darker());
                g2d.drawRect(iconX, iconY, iconSize, iconSize);

                // Stack count (bottom-right)
                if (item.isStackable() && item.getStackCount() > 1) {
                    String count = String.valueOf(item.getStackCount());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(g2d.getFont().deriveFont(12f));
                    int countW = g2d.getFontMetrics().stringWidth(count);
                    g2d.drawString(count, x + SLOT_SIZE - countW - 4, y + SLOT_SIZE - 4);
                }
            }
        }

        // Info area (below grid)
        int infoY = gridY + gridHeight + 10;
        if (selectedIndex >= 0 && selectedIndex < inv.getUsedSlots()) {
            Item sel = inv.getItems().get(selectedIndex);
            g2d.setColor(new Color(200, 220, 240));
            g2d.setFont(g2d.getFont().deriveFont(13f));
            g2d.drawString("→ " + sel.getName(), panelX + 20, infoY + 16);
            g2d.setColor(new Color(150, 160, 180));
            g2d.setFont(g2d.getFont().deriveFont(11f));
            g2d.drawString("[" + sel.getType() + "]", panelX + 20, infoY + 34);
        }

        // Hint
        g2d.setColor(new Color(120, 130, 150));
        g2d.setFont(g2d.getFont().deriveFont(11f));
        g2d.drawString("Enter: Dùng  |  Mũi tên: Di chuyển  |  I: Đóng", panelX + 20, infoY + 52);
    }
}
