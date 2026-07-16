package TerraIncognita.ui;

import TerraIncognita.util.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Hộp thoại hiện text — dùng cho NPC dialog, thông báo, lore object.
 * Parse text thành nhiều dòng (ngăn cách bằng \n), hiện từng dòng,
 * nhấn Enter để chuyển dòng tiếp, hết dòng → đóng.
 */
public class DialogBox {

    private String[] lines;
    private int currentLine;
    private boolean active;

    public DialogBox() {
        this.lines = null;
        this.currentLine = 0;
        this.active = false;
    }

    /**
     * Hiện hộp thoại với nội dung.
     * @param text nội dung (có thể nhiều dòng, ngăn cách bằng \n)
     */
    public void show(String text) {
        if (text == null || text.isEmpty()) {
            lines = new String[]{"..."};
        } else {
            lines = text.split("\n");
        }
        currentLine = 0;
        active = true;
    }

    /**
     * Nhấn Enter → chuyển dòng tiếp, hết dòng → đóng.
     * @return true nếu dialog vẫn active sau khi advance (còn dòng), false nếu đã đóng
     */
    public boolean advance() {
        if (!active) return false;
        currentLine++;
        if (currentLine >= lines.length) {
            active = false;
            lines = null;
            currentLine = 0;
            return false;
        }
        return true;
    }

    /**
     * Vẽ hộp thoại.
     */
    public void render(Graphics2D g2d) {
        if (!active || lines == null) return;

        int boxWidth = Constants.SCREEN_WIDTH - 80;
        int boxHeight = 100;
        int boxX = 40;
        int boxY = Constants.SCREEN_HEIGHT - boxHeight - 20;

        // Nền bán trong suốt
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(boxX, boxY, boxWidth, boxHeight);

        // Viền
        g2d.setColor(new Color(150, 160, 180));
        g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

        // Text dòng hiện tại
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(15f));
        String line = lines[currentLine];
        g2d.drawString(line, boxX + 16, boxY + 30);

        // Indicator "nhấn để tiếp tục"
        g2d.setColor(new Color(160, 160, 180));
        g2d.setFont(g2d.getFont().deriveFont(11f));
        if (currentLine < lines.length - 1) {
            g2d.drawString("[Enter] tiếp tục...", boxX + 16, boxY + boxHeight - 12);
        } else {
            g2d.drawString("[Enter] đóng", boxX + 16, boxY + boxHeight - 12);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void close() {
        active = false;
        lines = null;
        currentLine = 0;
    }
}
