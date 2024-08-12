package Model;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for drawable element
 * Contains two draw methods.
 * The draw method called with a double zoom parameter is used for determining poly levels
 * if draw is called without zoom it is fully drawn and no poly is applied
 */
public interface Drawable {
    void draw(GraphicsContext gc);
    void draw(GraphicsContext gc, double zoom);
    Type getType();
}
