import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Enum representing the different types of falling items in the game.
 */
public enum ItemType {
    CROISSANT(10, 30, "/assets/pastries/croissant.png"),
    MUFFIN(15, 32, "/assets/pastries/muffin.png"),
    APPLE_PIE(20, 35, "/assets/pastries/apple_pie.png"),
    DONUT(8, 28, "/assets/pastries/donut.png"),
    ECLAIR(18, 30, "/assets/pastries/eclair.png"),
    ROTTEN_OBJECT(-15, 45, "/assets/pastries/rotten_apple.png"); // Kötü ve büyük yiyecek

    final int energyGain;
    final double radius;
    final BufferedImage image;

    ItemType(int energyGain, double radius, String imagePath) {
        this.energyGain = energyGain;
        this.radius = radius;
        this.image = Assets.load(imagePath);
    }

    /**
     * Returns a random ItemType.
     * @param r The Random instance to use.
     * @return A random ItemType.
     */
    public static ItemType random(Random r) {
        return values()[r.nextInt(values().length)];
    }
}
