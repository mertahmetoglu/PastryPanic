
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * A utility class for loading and playing sound effects.
 */
public final class SoundManager {

    /**
     * Inner class to hold audio data and format.
     * This allows us to create new clips without re-reading the file.
     */
    public static class Sound {
        private final byte[] data;
        private final AudioFormat format;

        /**
         * Constructs a Sound object.
         * @param data The audio data as a byte array.
         * @param format The format of the audio data.
         */
        Sound(byte[] data, AudioFormat format) {
            this.data = data;
            this.format = format;
        }

        public byte[] getData() {
            return this.data;
        }

        public AudioFormat getFormat() {
            return this.format;
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private SoundManager() {}

    /**
     * Loads a sound clip from the given path.
     * @param path The relative path to the sound file (e.g., "/assets/sounds/eat.wav").
     * @return The loaded Clip, or null if loading fails.
     * @deprecated Use loadSound for one-shot effects. This is now for looping music.
     */
    public static Clip loadClip(String path) {
        if (path == null) {
            return null;
        }
        try {
            AudioInputStream audioIn = getAudioInputStreamFromPath(path);
            if (audioIn == null) { 
                return null; 
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (IOException | LineUnavailableException e) {
            System.err.println("FATAL: Could not load sound asset: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a sound's data into a Sound object for efficient playback.
     * @param path The relative path to the sound file.
     * @return A Sound object, or null if loading fails.
     */
    public static Sound loadSound(String path) {
        if (path == null) {
            return null;
        }
        try {
            AudioInputStream audioIn = getAudioInputStreamFromPath(path);
            if (audioIn == null) {  
                return null; 
            }

            byte[] data = audioIn.readAllBytes();
            AudioFormat format = audioIn.getFormat();
            audioIn.close();
            return new Sound(data, format);
        } catch (IOException e) {
            System.err.println("FATAL: Could not load sound data: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private static AudioInputStream getAudioInputStreamFromPath(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                System.err.println("Sound asset not found: " + path);
                return null;
            }
            return AudioSystem.getAudioInputStream(url);
        } catch (UnsupportedAudioFileException | IOException e) {
            System.err.println("FATAL: Could not create audio stream: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Plays a clip from the beginning.
     * @param clip The clip to play.
     */     
    public static void play(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // If already playing, stop it first
            }
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        }
    }

    /**
     * Plays a one-shot sound. Creates a new clip on the fly to allow overlapping sounds.
     * @param sound The Sound object to play.
     */
    public static void play(Sound sound) {
        if (sound == null) {
            return;
        }
        try {
            DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            // Use ByteArrayInputStream to avoid re-reading from a file
            clip.open(new AudioInputStream(
                new ByteArrayInputStream(sound.getData()),
                sound.getFormat(),
                sound.getData().length / sound.getFormat().getFrameSize()
            ));
            clip.start();
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    /**
     * Loops a clip continuously.
     * @param clip The clip to loop.
     */
    public static void loop(Clip clip) {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Stops a clip.
     * @param clip The clip to stop.
     */
    public static void stop(Clip clip) {
        if (clip != null) {
            clip.stop();
        }
    }
}
