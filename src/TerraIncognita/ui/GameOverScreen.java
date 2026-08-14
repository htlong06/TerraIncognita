package TerraIncognita.ui;

import TerraIncognita.util.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/** Screen displayed when the player dies. */
public class GameOverScreen {

    private int selectedOption;
    private static final String[] OPTIONS = {"Quay lai Menu", "Thoat game"};

    public GameOverScreen() {
        this.selectedOption = 0;
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

    public void render(Graphics2D g2d) {
        int sw = Constants.SCREEN_WIDTH;
        int sh = Constants.SCREEN_HEIGHT;

        g2d.setColor(new Color(30, 10, 15));
        g2d.fillRect(0, 0, sw, sh);

        g2d.setColor(new Color(220, 50, 50));
        g2d.setFont(g2d.getFont().deriveFont(56f));
        String title = "GAME OVER";
        int titleW = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (sw - titleW) / 2, sh / 4);

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

        g2d.setColor(new Color(120, 120, 130));
        g2d.setFont(g2d.getFont().deriveFont(12f));
        String hint = "Mui ten: Chon | Enter: Xac nhan";
        int hintW = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (sw - hintW) / 2, sh - 30);
    }
}
