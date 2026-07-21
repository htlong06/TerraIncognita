package TerraIncognita.ui;

import TerraIncognita.util.Constants;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Radial menu — menu vòng tròn giống Honkai Star Rail.
 * Giữ TAB → hiện menu, di chuột chọn, thả TAB → xác nhận.
 * 3 tính năng: Talent, Map, Inventory.
 */
public class RadialMenu {

    public enum Option {
        TALENT("Talent", "✦"),
        MAP("Map", "◈"),
        INVENTORY("Inventory", "▤");

        final String label;
        final String icon;

        Option(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    private static final Option[] OPTIONS = Option.values();
    private static final int RADIUS = 140;
    private static final int ICON_RADIUS = 28;
    private static final int CENTER_RADIUS = 56;

    private boolean open;
    private int hoveredIndex;

    public RadialMenu() {
        this.open = false;
        this.hoveredIndex = -1;
    }

    public void open() {
        open = true;
        hoveredIndex = -1;
    }

    public void close() {
        open = false;
        hoveredIndex = -1;
    }

    public boolean isOpen() {
        return open;
    }

    /**
     * Cập nhật hover theo vị trí chuột.
     * @param mouseX tọa độ chuột X (panel coords)
     * @param mouseY tọa độ chuột Y
     */
    public void updateHover(int mouseX, int mouseY) {
        if (!open) {
            hoveredIndex = -1;
            return;
        }

        int centerX = Constants.SCREEN_WIDTH / 2;
        int centerY = Constants.SCREEN_HEIGHT / 2;

        // Tính góc từ tâm đến chuột
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;

        // Mỗi option chiếm 120° (360/3), bắt đầu từ -90° (12 giờ)
        // Option 0: 270°-30°, Option 1: 30°-150°, Option 2: 150°-270°
        double sector = 360.0 / OPTIONS.length;
        double startAngle = -90; // 12 giờ

        hoveredIndex = -1;
        for (int i = 0; i < OPTIONS.length; i++) {
            double sectorStart = startAngle + i * sector;
            double sectorEnd = sectorStart + sector;
            // Normalize về 0-360
            double normStart = ((sectorStart % 360) + 360) % 360;
            double normEnd = ((sectorEnd % 360) + 360) % 360;

            if (normStart < normEnd) {
                if (angle >= normStart && angle < normEnd) {
                    hoveredIndex = i;
                    break;
                }
            } else { // wrap qua 360
                if (angle >= normStart || angle < normEnd) {
                    hoveredIndex = i;
                    break;
                }
            }
        }
    }

    /**
     * Lấy option đang hover, hoặc null nếu chưa chọn.
     */
    public Option getHoveredOption() {
        if (hoveredIndex < 0 || hoveredIndex >= OPTIONS.length) return null;
        return OPTIONS[hoveredIndex];
    }

    /**
     * Vẽ radial menu.
     */
    public void render(Graphics2D g2d) {
        if (!open) return;

        int centerX = Constants.SCREEN_WIDTH / 2;
        int centerY = Constants.SCREEN_HEIGHT / 2;

        // Nền mờ toàn màn hình
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Vẽ 3 sector
        for (int i = 0; i < OPTIONS.length; i++) {
            double angle = Math.toRadians(-90 + i * 120 + 60); // tâm sector
            int iconX = centerX + (int) (RADIUS * Math.cos(angle));
            int iconY = centerY + (int) (RADIUS * Math.sin(angle));

            boolean hovered = (i == hoveredIndex);

            // Sector background (lát cắt mờ)
            g2d.setColor(hovered ? new Color(80, 60, 120, 200) : new Color(30, 30, 45, 160));
            g2d.fillArc(centerX - RADIUS - 20, centerY - RADIUS - 20,
                    (RADIUS + 20) * 2, (RADIUS + 20) * 2,
                    -90 + i * 120, 120);

            // Icon circle
            g2d.setColor(hovered ? new Color(200, 180, 255) : new Color(120, 110, 150));
            g2d.fillOval(iconX - ICON_RADIUS, iconY - ICON_RADIUS, ICON_RADIUS * 2, ICON_RADIUS * 2);
            g2d.setColor(hovered ? new Color(40, 30, 60) : new Color(60, 55, 80));
            g2d.drawOval(iconX - ICON_RADIUS, iconY - ICON_RADIUS, ICON_RADIUS * 2, ICON_RADIUS * 2);

            // Icon text (emoji)
            g2d.setFont(g2d.getFont().deriveFont(24f));
            FontMetrics fm = g2d.getFontMetrics();
            String icon = OPTIONS[i].icon;
            int iconW = fm.stringWidth(icon);
            g2d.setColor(hovered ? Color.WHITE : new Color(200, 200, 210));
            g2d.drawString(icon, iconX - iconW / 2, iconY + 8);

            // Label
            g2d.setFont(g2d.getFont().deriveFont(13f));
            fm = g2d.getFontMetrics();
            String label = OPTIONS[i].label;
            int labelW = fm.stringWidth(label);
            g2d.setColor(hovered ? Color.WHITE : new Color(160, 160, 175));
            g2d.drawString(label, iconX - labelW / 2, iconY + ICON_RADIUS + 16);
        }

        // Vòng tròn tâm
        g2d.setColor(new Color(60, 50, 90, 220));
        g2d.fillOval(centerX - CENTER_RADIUS, centerY - CENTER_RADIUS, CENTER_RADIUS * 2, CENTER_RADIUS * 2);
        g2d.setColor(new Color(150, 140, 190));
        g2d.drawOval(centerX - CENTER_RADIUS, centerY - CENTER_RADIUS, CENTER_RADIUS * 2, CENTER_RADIUS * 2);

        // Text tâm
        g2d.setFont(g2d.getFont().deriveFont(14f));
        FontMetrics fm = g2d.getFontMetrics();
        String centerText = hoveredIndex >= 0 ? OPTIONS[hoveredIndex].label : "Chọn";
        int textW = fm.stringWidth(centerText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(centerText, centerX - textW / 2, centerY + 5);

        // Hint
        g2d.setFont(g2d.getFont().deriveFont(11f));
        String hint = "Giữ TAB + di chuột → thả TAB để chọn";
        int hintW = g2d.getFontMetrics().stringWidth(hint);
        g2d.setColor(new Color(120, 120, 140));
        g2d.drawString(hint, centerX - hintW / 2, Constants.SCREEN_HEIGHT - 30);
    }
}
