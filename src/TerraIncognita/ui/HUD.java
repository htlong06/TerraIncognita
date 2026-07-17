package TerraIncognita.ui;

import TerraIncognita.entity.Player;
import TerraIncognita.util.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * HUD (Heads-Up Display) — thanh HP, thông tin trạng thái hiện trên màn hình.
 * Vẽ ở vị trí cố định, không bị ảnh hưởng bởi Camera.
 */
public class HUD {

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 16;
    private static final int PADDING = 10;

    /**
     * Vẽ HUD lên màn hình.
     * @param g2d đối tượng đồ họa
     * @param player player để lấy HP, level, exp, gold
     */
    public void render(Graphics2D g2d, Player player) {
        int x = PADDING;
        int y = PADDING;

        // --- Thanh HP ---
        // Nền xám
        g2d.setColor(new Color(60, 30, 30));
        g2d.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);
        // Fill đỏ theo tỉ lệ HP/maxHP
        double hpRatio = (double) player.getHp() / player.getMaxHp();
        g2d.setColor(new Color(220, 50, 50));
        g2d.fillRect(x, y, (int) (BAR_WIDTH * hpRatio), BAR_HEIGHT);
        // Viền
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);
        // Text HP
        g2d.setFont(g2d.getFont().deriveFont(11f));
        g2d.drawString("HP: " + player.getHp() + "/" + player.getMaxHp(), x + 4, y + 12);

        y += BAR_HEIGHT + 6;

        // --- Thanh EXP ---
        g2d.setColor(new Color(25, 35, 50));
        g2d.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT - 4);
        double expRatio = (double) player.getExp() / player.getExpToNextLevel();
        g2d.setColor(new Color(80, 180, 240));
        g2d.fillRect(x, y, (int) (BAR_WIDTH * expRatio), BAR_HEIGHT - 4);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT - 4);

        y += BAR_HEIGHT;

        // --- Text: Level, Gold ---
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(13f));
        g2d.drawString("Lv: " + player.getLevel(), x, y + 16);
        g2d.setColor(new Color(240, 200, 60));
        String goldText = player.getGold() + "g";
        int goldW = g2d.getFontMetrics().stringWidth(goldText);
        g2d.drawString(goldText, x + BAR_WIDTH - goldW, y + 16);
    }
}
