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

    // --- Lựa chọn cuối dialog (VD: "Nhận nhiệm vụ" / "Từ chối") ---
    // Sau khi đọc hết các dòng text, nếu dialog có kèm lựa chọn, thay vì
    // đóng luôn thì chuyển sang "choicePhase" để player bấm chọn.
    private boolean hasChoice;
    private String[] choiceOptions;
    private int selectedChoice;
    private boolean choicePhase;

    public DialogBox() {
        this.lines = null;
        this.currentLine = 0;
        this.active = false;
        this.hasChoice = false;
        this.choiceOptions = null;
        this.selectedChoice = 0;
        this.choicePhase = false;
    }

    /**
     * Hiện hộp thoại với nội dung (không có lựa chọn — hành vi cũ, giữ nguyên
     * tương thích với chỗ nào đang/sẽ gọi show(text)).
     * @param text nội dung (có thể nhiều dòng, ngăn cách bằng \n)
     */
    public void show(String text) {
        show(text, null);
    }

    /**
     * Hiện hộp thoại, sau khi đọc hết text thì hiện thêm lựa chọn (2 phương
     * án trở lên). Dùng {@link #moveChoice(int)} để đổi lựa chọn (mũi tên
     * trái/phải hoặc lên/xuống), {@link #confirmChoice()} khi Enter để chốt.
     *
     * @param text          nội dung lời thoại
     * @param choiceOptions các lựa chọn hiện sau cùng, VD {"Nhận", "Từ chối"}.
     *                      Truyền null hoặc mảng rỗng nếu không cần lựa chọn.
     */
    public void show(String text, String[] choiceOptions) {
        if (text == null || text.isEmpty()) {
            lines = new String[]{"..."};
        } else {
            lines = text.split("\n");
        }
        currentLine = 0;
        active = true;
        this.hasChoice = (choiceOptions != null && choiceOptions.length > 0);
        this.choiceOptions = choiceOptions;
        this.selectedChoice = 0;
        this.choicePhase = false;
    }

    /**
     * Nhấn Enter → chuyển dòng tiếp. Hết dòng: nếu có lựa chọn thì chuyển
     * sang choicePhase (không đóng); nếu không thì đóng dialog luôn.
     * Khi đang ở choicePhase, gọi hàm này không làm gì (dùng confirmChoice()).
     * @return true nếu dialog vẫn active sau lệnh gọi, false nếu đã đóng
     */
    public boolean advance() {
        if (!active) return false;
        if (choicePhase) return true; // đang ở màn chọn — chờ confirmChoice()

        currentLine++;
        if (currentLine >= lines.length) {
            if (hasChoice) {
                choicePhase = true;
                return true;
            }
            active = false;
            lines = null;
            currentLine = 0;
            return false;
        }
        return true;
    }

    /** Di chuyển lựa chọn (delta = -1 hoặc +1). Không làm gì nếu chưa tới choicePhase. */
    public void moveChoice(int delta) {
        if (!choicePhase || choiceOptions == null || choiceOptions.length == 0) return;
        selectedChoice = Math.floorMod(selectedChoice + delta, choiceOptions.length);
    }

    /**
     * Chốt lựa chọn hiện tại, đóng dialog.
     * @return index của lựa chọn đã chọn (0-based); -1 nếu gọi sai lúc (không ở choicePhase)
     */
    public int confirmChoice() {
        if (!choicePhase) return -1;
        int result = selectedChoice;
        close();
        return result;
    }

    public boolean isChoicePhase() {
        return choicePhase;
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

        if (choicePhase) {
            // Màn lựa chọn: liệt kê các option theo hàng ngang, tô sáng option đang chọn
            g2d.setColor(Color.WHITE);
            g2d.setFont(g2d.getFont().deriveFont(15f));
            int optX = boxX + 16;
            for (int i = 0; i < choiceOptions.length; i++) {
                boolean selected = (i == selectedChoice);
                g2d.setColor(selected ? new Color(255, 210, 90) : Color.WHITE);
                String label = (selected ? "> " : "  ") + choiceOptions[i];
                g2d.drawString(label, optX, boxY + 30 + i * 22);
            }
            g2d.setColor(new Color(160, 160, 180));
            g2d.setFont(g2d.getFont().deriveFont(11f));
            g2d.drawString("[◄►] chọn   [Enter] xác nhận", boxX + 16, boxY + boxHeight - 12);
            return;
        }

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
        hasChoice = false;
        choiceOptions = null;
        choicePhase = false;
        selectedChoice = 0;
    }
}
