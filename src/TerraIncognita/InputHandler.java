package TerraIncognita;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Xử lý input từ bàn phím dùng Swing Key Bindings (InputMap / ActionMap).
 *
 * Cơ chế cũ (KeyListener) bị lỗi vì:
 * - SPACE bị Swing component mặc định nuốt (JRootPane, focus traversal)
 * - Race condition giữa AWT Event Thread và Game Thread khiến event mất
 *
 * Key Bindings với WHEN_IN_FOCUSED_WINDOW đảm bảo nhận phím bất kể
 * component nào đang có focus trong cửa sổ.
 *
 * Thêm cơ chế "buffered justPressed": khi phím được bấm, flag justPressed
 * được set và chỉ bị clear SAU KHI game loop đọc xong — không bao giờ mất event.
 */
public class InputHandler {

    // Trạng thái phím hiện tại (đang giữ hay không)
    private final boolean[] keys;
    // Trạng thái phím frame trước (dùng cho isKeyJustPressed fallback)
    private final boolean[] previousKeys;
    // Buffer: phím đã được bấm kể từ lần update() cuối — không bị mất dù
    // press+release xảy ra giữa 2 frame
    private final boolean[] justPressedBuffer;

    // --- Chuột ---
    // Trạng thái nút trái chuột hiện tại / frame trước (dùng để phát hiện
    // "vừa nhấn" / "vừa thả" — ví dụ: SWORD bấm 1 cái là chém ngay, còn
    // BOW thì giữ để ngắm rồi thả ra mới bắn).
    private volatile boolean mouseLeftDown;
    private boolean previousMouseLeftDown;
    // Buffer: chuột trái đã được bấm kể từ lần update() cuối — tương tự
    // justPressedBuffer cho bàn phím, đảm bảo không bao giờ mất event click
    private volatile boolean mouseLeftJustPressedBuffer;
    private volatile boolean mouseLeftJustReleasedBuffer;
    private volatile int mouseX;
    private volatile int mouseY;

    // Danh sách các phím game cần bind
    private static final int[] GAME_KEYS = {
        KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
        KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
        KeyEvent.VK_SPACE, KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_I,
        KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER,
        KeyEvent.VK_TAB  // radial menu
    };

    public InputHandler() {
        keys = new boolean[512];        // dư dả cho mọi VK_* code
        previousKeys = new boolean[512];
        justPressedBuffer = new boolean[512];
    }

    /**
     * Đăng ký Key Bindings + Mouse Listener lên JComponent (thường là GamePanel).
     * Gọi 1 lần duy nhất sau khi tạo panel.
     */
    public void bindTo(JComponent component) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        for (int keyCode : GAME_KEYS) {
            String pressName = "press_" + keyCode;
            String releaseName = "release_" + keyCode;

            // Bind phím bấm xuống
            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressName);
            actionMap.put(pressName, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (keyCode >= 0 && keyCode < keys.length) {
                        if (!keys[keyCode]) {
                            // Chỉ buffer lần đầu bấm (tránh auto-repeat ghi đè)
                            justPressedBuffer[keyCode] = true;
                            System.out.println("[DEBUG InputHandler] KEY PRESSED: " + KeyEvent.getKeyText(keyCode)
                                    + " (code=" + keyCode + ")");
                        }
                        keys[keyCode] = true;
                    }
                }
            });

            // Bind phím thả ra
            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releaseName);
            actionMap.put(releaseName, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (keyCode >= 0 && keyCode < keys.length) {
                        keys[keyCode] = false;
                        System.out.println("[DEBUG InputHandler] KEY RELEASED: " + KeyEvent.getKeyText(keyCode)
                                + " (code=" + keyCode + ")");
                    }
                }
            });
        }

        // --- Chuột: nút trái (BUTTON1) — dùng thẳng MouseListener, không
        // gặp vấn đề "mất focus" như bàn phím vì sự kiện chuột luôn nhắm
        // đúng component đang ở dưới con trỏ, không phụ thuộc focus. ---
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (!mouseLeftDown) {
                        mouseLeftJustPressedBuffer = true;
                        System.out.println("[DEBUG InputHandler] MOUSE LEFT PRESSED (buffer set)");
                    }
                    mouseLeftDown = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (mouseLeftDown) {
                        mouseLeftJustReleasedBuffer = true;
                        System.out.println("[DEBUG InputHandler] MOUSE LEFT RELEASED (buffer set)");
                    }
                    mouseLeftDown = false;
                }
            }
        });
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
    }

    /**
     * Cập nhật trạng thái phím frame trước.
     * Gọi MỘT LẦN mỗi frame, TRƯỚC khi kiểm tra input.
     */
    public void update() {
        System.arraycopy(keys, 0, previousKeys, 0, keys.length);
        // justPressedBuffer sẽ được clear trong isKeyJustPressed() sau khi đọc
        previousMouseLeftDown = mouseLeftDown;
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
     *
     * Dùng buffer: nếu phím đã được press kể từ lần update() cuối, trả true
     * và clear buffer. Điều này đảm bảo không bao giờ mất event ngay cả khi
     * press+release xảy ra giữa 2 frame (trường hợp KeyListener cũ bị lỗi).
     */
    public boolean isKeyJustPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            // Ưu tiên dùng buffer (chắc chắn không mất event)
            if (justPressedBuffer[keyCode]) {
                justPressedBuffer[keyCode] = false; // clear sau khi đọc
                if (keyCode == KeyEvent.VK_SPACE) {
                    System.out.println("[DEBUG InputHandler.isKeyJustPressed] SPACE => TRUE (from buffer)");
                }
                return true;
            }
            // Fallback: so sánh keys vs previousKeys (cho auto-repeat detection)
            boolean result = keys[keyCode] && !previousKeys[keyCode];
            if (keyCode == KeyEvent.VK_SPACE && result) {
                System.out.println("[DEBUG InputHandler.isKeyJustPressed] SPACE => TRUE (from diff)");
            }
            return result;
        }
        return false;
    }

    // =========================================
    // CHUỘT
    // =========================================

    /** Nút trái chuột có đang được giữ không (dùng cho "giữ để ngắm" của cung). */
    public boolean isMouseLeftPressed() {
        return mouseLeftDown;
    }

    /** Nút trái chuột vừa được bấm xuống frame này (dùng cho chém kiếm — bấm là chém ngay). */
    public boolean isMouseLeftJustPressed() {
        // Ưu tiên buffer (giống justPressedBuffer cho keyboard)
        if (mouseLeftJustPressedBuffer) {
            mouseLeftJustPressedBuffer = false;
            System.out.println("[DEBUG InputHandler.isMouseLeftJustPressed] => TRUE (from buffer)");
            return true;
        }
        // Fallback: so sánh current vs previous
        boolean result = mouseLeftDown && !previousMouseLeftDown;
        if (result) {
            System.out.println("[DEBUG InputHandler.isMouseLeftJustPressed] => TRUE (from diff)");
        }
        return result;
    }

    /** Nút trái chuột vừa được thả ra frame này (dùng cho bắn cung — thả ra mới bắn). */
    public boolean isMouseLeftJustReleased() {
        // Ưu tiên buffer
        if (mouseLeftJustReleasedBuffer) {
            mouseLeftJustReleasedBuffer = false;
            System.out.println("[DEBUG InputHandler.isMouseLeftJustReleased] => TRUE (from buffer)");
            return true;
        }
        // Fallback
        boolean result = !mouseLeftDown && previousMouseLeftDown;
        if (result) {
            System.out.println("[DEBUG InputHandler.isMouseLeftJustReleased] => TRUE (from diff)");
        }
        return result;
    }

    /** Vị trí X hiện tại của chuột, tính theo toạ độ của GamePanel (== world, vì không có camera scroll). */
    public int getMouseX() {
        return mouseX;
    }

    /** Vị trí Y hiện tại của chuột, tính theo toạ độ của GamePanel. */
    public int getMouseY() {
        return mouseY;
    }
}
