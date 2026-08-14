package TerraIncognita.audio;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import TerraIncognita.util.Constants;

public class BackgroundMusic {

    private static Clip clip;

    public static void playLoop() {
        if (clip != null && clip.isRunning()) {
            return;
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(Constants.BACKGROUND_MUSIC_PATH))) {
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            setVolume();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("[BackgroundMusic] Could not play " + Constants.BACKGROUND_MUSIC_PATH + ": " + e.getMessage());
        }
    }

    private static void setVolume() {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        volume.setValue(Constants.BACKGROUND_MUSIC_VOLUME_DB);
    }

    private BackgroundMusic() {
    }
}
