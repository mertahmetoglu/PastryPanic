import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * A utility class for loading assets (images) from the classpath.
 * Ensures that resources are loaded correctly from the JAR or file system.
 */
public final class Assets {
 
    /**
     * Private constructor to prevent instantiation.
     */
    private Assets() {}

    /**
     * Loads a BufferedImage from the given path in the resources folder.
     * @param path The relative path to the image file (e.g., "/assets/player.png").
     * @return The loaded BufferedImage, or null if loading fails.
     */
    public static BufferedImage load(String path) {
        if (path == null) {
            return null;
        }
        try {
            URL url = Assets.class.getResource(path);
            if (url == null) {
                System.err.println("Asset not found: " + path);
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("Failed to load asset: " + path);
            e.printStackTrace();
            return null;
        }
    }
}