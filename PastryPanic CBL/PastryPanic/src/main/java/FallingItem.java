import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents a single item falling from the top of the screen.
 */
public class FallingItem {
    final ItemType type;
    double x;
    double y;
    double radius;

    /**
     * Constructs a new FallingItem.
     * @param type The type of the item.
     * @param x The initial x-coordinate.
     * @param y The initial y-coordinate.
     */
    public FallingItem(ItemType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.radius = type.radius;
    }

    /**
     * Draws the item on the screen.
     * @param g2 The graphics context.
     */
    public void draw(Graphics2D g2) {
        int diameter = (int) (this.radius * 2);
        int drawX = (int) (this.x - this.radius);
        int drawY = (int) (this.y - this.radius);

        if (this.type.image != null) {
            g2.drawImage(this.type.image, drawX, drawY, diameter, diameter, null);
        } else {
            g2.setColor(Color.ORANGE);
            g2.fillOval(drawX, drawY, diameter, diameter);
        }
    }

    /**
     * Gets the bounding box of the item for collision detection.
     * @return A Rectangle representing the bounds.
     */
    public Rectangle getBounds() {
        int diameter = (int) (this.radius * 2);
        int rectX = (int) (this.x - this.radius);
        int rectY = (int) (this.y - this.radius);
        return new Rectangle(rectX, rectY, diameter, diameter);
    }
}
