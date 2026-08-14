package TerraIncognita.audio;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

import TerraIncognita.util.Constants;

/**
 * Phát hiệu ứng âm thanh (SFX) ngắn, dùng 1 lần — khác với
 * {@link BackgroundMusic} (1 Clip duy nhất, loop liên tục), ở đây MỖI lần
 * gọi {@link #play(String)} sẽ mở 1 {@link Clip} MỚI hoàn toàn. Lý do:
 * - Các đòn chém liên tiếp (combo) hoặc nhiều vụ nổ gần nhau cần chồng
 * tiếng lên nhau (attack1 nhát 2 phát ra trước khi attack1 nhát 1 dứt
 * hẳn) — nếu dùng chung 1 Clip tĩnh như BackgroundMusic thì tiếng sau
 * sẽ cắt ngang tiếng trước, nghe giật.
 * - Clip tự đóng (close()) ngay khi phát xong qua LineListener, tránh rò
 * rỉ tài nguyên khi có nhiều hiệu ứng bắn ra liên tục trong 1 trận đánh.
 *
 * Mọi lỗi (file không tồn tại, định dạng không hỗ trợ, hết line khả dụng...)
 * chỉ log ra console — KHÔNG được phép làm crash game vì thiếu/lỗi file âm
 * thanh.
 */
public class SoundManager {

    private SoundManager() {
    }

    /**
     * Phát 1 hiệu ứng âm thanh ngắn tại đường dẫn {@code path}, âm lượng
     * mặc định {@link Constants#SFX_VOLUME_DB}.
     */
    public static void play(String path) {
        play(path, Constants.SFX_VOLUME_DB);
    }

    /**
     * Phát 1 hiệu ứng âm thanh ngắn với âm lượng tuỳ chỉnh (dB, thường là
     * số âm — 0 là gốc, càng nhỏ càng nhỏ tiếng).
     */
    public static void play(String path, float volumeDb) {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(path))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            setVolume(clip, volumeDb);

            // Tự đóng Clip (giải phóng tài nguyên) ngay khi phát xong, để
            // không tích tụ dần nếu người chơi tung rất nhiều đòn/bomb liên tục.
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();
        } catch (Exception e) {
            System.err.println("[SoundManager] Could not play " + path + ": " + e.getMessage());
        }
    }

    private static void setVolume(Clip clip, float volumeDb) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        // Clamp trong khoảng control hỗ trợ để tránh IllegalArgumentException
        // trên vài driver/OS có range hẹp hơn [min, max] mặc định.
        float clamped = Math.max(volume.getMinimum(), Math.min(volume.getMaximum(), volumeDb));
        volume.setValue(clamped);
    }
}