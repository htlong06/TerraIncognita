package TerraIncognita.ui;

import TerraIncognita.InputHandler;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Menu chính.
 * Hiện khi khởi động game: New Game, Continue (nếu có save), Exit.
 */
public class MenuScreen {

    private List<String> options;
    private int selectedIndex = 0;
    private boolean confirmed = false;

    public MenuScreen(boolean hasSaveFile) {
        options = new ArrayList<>();
        options.add("New Game");
        if (hasSaveFile) {
            options.add("Continue");
        }
        options.add("Exit");
    }

    /**
     * Cập nhật logic menu (nhận input lên/xuống/Enter).
     */
    public void update(InputHandler input) {
        if (input.isKeyJustPressed(KeyEvent.VK_UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            selectedIndex = Math.min(options.size() - 1, selectedIndex + 1);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            confirmed = true;
        }
    }

    /**
     * Vẽ menu lên màn hình.
     */
    public void render(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(java.awt.Font.BOLD, 40f));
        String title = "TERRA INCOGNITA";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (Constants.SCREEN_WIDTH - titleWidth) / 2, Constants.SCREEN_HEIGHT / 4);

        g2d.setFont(g2d.getFont().deriveFont(java.awt.Font.PLAIN, 18f));
        String subtitle = "Dungeon Explorer";
        int subtitleWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, (Constants.SCREEN_WIDTH - subtitleWidth) / 2, Constants.SCREEN_HEIGHT / 4 + 30);

        g2d.setFont(g2d.getFont().deriveFont(java.awt.Font.PLAIN, 22f));
        int startY = Constants.SCREEN_HEIGHT / 2;
        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            boolean selected = (i == selectedIndex);
            String text = selected ? "> " + label : label;
            g2d.setColor(selected ? Color.YELLOW : Color.WHITE);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, startY + i * 40);
        }
    }

    /**
     * Lấy lựa chọn đã chọn (sau khi nhấn Enter).
     * @return tên option, hoặc null nếu chưa xác nhận
     */
    public String getSelectedOption() {
        return confirmed ? options.get(selectedIndex) : null;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void reset() {
        confirmed = false;
        selectedIndex = 0;
    }
}
