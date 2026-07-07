package TerraIncognita;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Xử lý input từ bàn phím.
 * Implements KeyListener, lưu trạng thái các phím đang được nhấn.
 *
 * Cách dùng: GamePanel addKeyListener(inputHandler),
 * sau đó các hệ thống khác kiểm tra inputHandler.isKeyPressed(KeyEvent.VK_UP) v.v.
 */
public class InputHandler implements KeyListener {

    // Mảng lưu trạng thái phím hiện tại và frame trước
    private boolean[] keys;
    private boolean[] previousKeys;

    // Các phím game thường dùng (gợi ý)
    // VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT (hoặc VK_W, VK_A, VK_S, VK_D)
    // VK_SPACE  — tấn công
    // VK_E      — tương tác
    // VK_I      — mở inventory
    // VK_ESCAPE — pause/menu

    public InputHandler() {
        keys = new boolean[256];
        previousKeys = new boolean[256];
    }

    /**
     * Cập nhật trạng thái phím frame trước.
     * Gọi MỘT LẦN mỗi frame, TRƯỚC khi kiểm tra input.
     */
    public void update() {
        System.arraycopy(keys, 0, previousKeys, 0, keys.length);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Thường không dùng
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
    }

    /**
     * Kiểm tra phím có đang được nhấn không.
     * @param keyCode mã phím (VK_UP, VK_SPACE...)
     * @return true nếu đang nhấn
     */
    public boolean isKeyPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    /**
     * Kiểm tra phím vừa được nhấn (chỉ true 1 lần, sau đó phải thả ra nhấn lại).
     * Hữu ích cho hành động 1 lần như tấn công, mở inventory.
     * @param keyCode mã phím
     * @return true nếu vừa nhấn (frame này nhấn, frame trước không nhấn)
     */
    public boolean isKeyJustPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode] && !previousKeys[keyCode];
        }
        return false;
    }
}
