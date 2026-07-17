package TerraIncognita.ui;

import TerraIncognita.hub.RunSummary;
import TerraIncognita.util.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Màn hình Game Over.
 * Hiện khi player chết: điểm số, thống kê, Retry / Quit.
 */
public class GameOverScreen {

    private RunSummary summary;
    private int selectedOption;
    private static final String[] OPTIONS = {"Quay lại Menu", "Thoát game"};

    public GameOverScreen() {
        this.summary = null;
        this.selectedOption = 0;
    }

    public void setSummary(RunSummary summary) {
        this.summary = summary;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void moveCursorUp() {
        selectedOption = (selectedOption - 1 + OPTIONS.length) % OPTIONS.length;
    }

    public void moveCursorDown() {
        selectedOption = (selectedOption + 1) % OPTIONS.length;
    }

    /**
     * Vẽ màn hình Game Over.
     */
    public void render(Graphics2D g2d) {
        int sw = Constants.SCREEN_WIDTH;
        int sh = Constants.SCREEN_HEIGHT;

        // Nền tối đỏ
        g2d.setColor(new Color(30, 10, 15));
        g2d.fillRect(0, 0, sw, sh);

        // "GAME OVER"
        g2d.setColor(new Color(220, 50, 50));
        g2d.setFont(g2d.getFont().deriveFont(56f));
        String title = "GAME OVER";
        int titleW = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (sw - titleW) / 2, sh / 4);

        // Thống kê
        int statsY = sh / 4 + 60;
        g2d.setFont(g2d.getFont().deriveFont(15f));
        g2d.setColor(new Color(200, 200, 200));

        if (summary != null) {
            int score = summary.calculateScore();
            String[] stats = {
                "Quái đã giết: " + summary.getMonstersKilled(),
                "Vàng kiếm được: " + summary.getGoldEarned() + "g",
                "Tầng đã qua: " + summary.getFloorsCleared(),
                "Điểm tổng kết: " + score
            };
            for (int i = 0; i < stats.length; i++) {
                int w = g2d.getFontMetrics().stringWidth(stats[i]);
                g2d.drawString(stats[i], (sw - w) / 2, statsY + i * 24);
            }
        } else {
            String noStats = "Không có dữ liệu";
            int w = g2d.getFontMetrics().stringWidth(noStats);
            g2d.drawString(noStats, (sw - w) / 2, statsY);
        }

        // Options
        int optY = sh / 2 + 60;
        g2d.setFont(g2d.getFont().deriveFont(18f));
        for (int i = 0; i < OPTIONS.length; i++) {
            if (i == selectedOption) {
                g2d.setColor(new Color(255, 220, 100));
                g2d.drawString("> " + OPTIONS[i], sw / 2 - 120, optY + i * 32);
            } else {
                g2d.setColor(new Color(150, 150, 160));
                g2d.drawString("  " + OPTIONS[i], sw / 2 - 120, optY + i * 32);
            }
        }

        // Hint
        g2d.setColor(new Color(120, 120, 130));
        g2d.setFont(g2d.getFont().deriveFont(12f));
        String hint = "Mũi tên: Chọn  |  Enter: Xác nhận";
        int hintW = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (sw - hintW) / 2, sh - 30);
    }
}
