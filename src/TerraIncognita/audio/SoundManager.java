package TerraIncognita.audio;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

import TerraIncognita.util.Constants;

/**
 * Phát hiệu ứng âm thanh (SFX) ngắn, dùng 1 lần
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
     * Phát 1 hiệu ứng âm thanh ngắn với âm lượng tuỳ chỉnh
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