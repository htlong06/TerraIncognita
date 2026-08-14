package TerraIncognita.ui;

import TerraIncognita.InputHandler;
import TerraIncognita.util.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Menu tạm dừng (ESC trong lúc chơi). 2 cấp:
 * MAIN (Resume/Exit) → chọn Exit thì sang CONFIRM_EXIT
 * (Save & Exit / Exit without Saving / Cancel).
 */
public class PauseMenu {

    private enum Mode { MAIN, CONFIRM_EXIT }

    private static final List<String> MAIN_OPTIONS = List.of("Resume", "Exit");
    private static final List<String> CONFIRM_OPTIONS =
            List.of("Save & Exit", "Exit without Saving", "Cancel");

    private Mode mode = Mode.MAIN;
    private int selectedIndex = 0;
    private String result;

    public void update(InputHandler input) {
        List<String> options = currentOptions();

        if (input.isKeyJustPressed(KeyEvent.VK_UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            selectedIndex = Math.min(options.size() - 1, selectedIndex + 1);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            select(options.get(selectedIndex));
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            if (mode == Mode.CONFIRM_EXIT) {
                mode = Mode.MAIN;
                selectedIndex = 0;
            } else {
                result = "RESUME";
            }
        }
    }

    private void select(String option) {
        if (mode == Mode.MAIN) {
            if (option.equals("Resume")) {
                result = "RESUME";
            } else {
                mode = Mode.CONFIRM_EXIT;
                selectedIndex = 0;
            }
            return;
        }
        switch (option) {
            case "Save & Exit":
                result = "SAVE_AND_EXIT";
                break;
            case "Exit without Saving":
                result = "EXIT_NO_SAVE";
                break;
            case "Cancel":
                mode = Mode.MAIN;
                selectedIndex = 0;
                break;
        }
    }

    private List<String> currentOptions() {
        return mode == Mode.MAIN ? MAIN_OPTIONS : CONFIRM_OPTIONS;
    }

    /**
     * Lấy hành động người chơi vừa xác nhận (RESUME / SAVE_AND_EXIT / EXIT_NO_SAVE),
     * rồi clear kết quả. Trả null nếu chưa xác nhận xong (đang điều hướng menu).
     */
    public String consumeResult() {
        String r = result;
        result = null;
        return r;
    }

    public void reset() {
        mode = Mode.MAIN;
        selectedIndex = 0;
        result = null;
    }

    public void render(Graphics2D g2d) {
        List<String> options = currentOptions();

        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(java.awt.Font.BOLD, 36f));
        String title = mode == Mode.MAIN ? "PAUSED" : "Save before exiting?";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (Constants.SCREEN_WIDTH - titleWidth) / 2, Constants.SCREEN_HEIGHT / 2 - 60);

        g2d.setFont(g2d.getFont().deriveFont(java.awt.Font.PLAIN, 22f));
        int startY = Constants.SCREEN_HEIGHT / 2;
        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            boolean selected = (i == selectedIndex);
            String text = selected ? "> " + label : label;
            g2d.setColor(selected ? Color.YELLOW : Color.WHITE);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, startY + i * 36);
        }
    }
}
