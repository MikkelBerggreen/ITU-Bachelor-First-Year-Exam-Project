package Model.MapComponents;

import Model.Drawable;
import Model.Type;
import javafx.scene.canvas.GraphicsContext;

/**
 * Point of interest (POI) can either represent a user-placed POI pin with lat & lon coords as well as a given bookmark name
 * or the pins used for destination and source marking for the searched route
 * Gets the corresponding image from ImageHandler
 */
public class PointOfInterest extends MapIcon implements Drawable {
    private String name;

    /**
     * Constructor for POI primarily used for user-saved bookmarks
     * @param lon x value as longitude
     * @param lat y value as latitude
     * @param name String name of saved point of interest
     */
    public PointOfInterest(float lon, float lat, String name) {
        super(lon, lat, Type.PIN);
        this.name = name;
    }

    /**
     * Constructor for POI
     * @param lon x value as longitude
     * @param lat y value as latitude
     * @param name String name of saved point of interest
     * @param type Type of pin
     */
    public PointOfInterest(float lon, float lat, String name, Type type) {
        super(lon, lat, type);
        this.name = name;
    }

    /**
     * Draw function for POI
     * @param gc GraphicsContext
     * @param width width of the image to draw
     * @param height height of the image to draw
     */
    public void draw(GraphicsContext gc, double width, double height) {
        gc.drawImage(super.getImage(), super.lon-height/2.15, super.lat-width/1.07, width, height);
    }

    /**
     * empty and not used
     * @param gc GraphicsContext
     * @param zoom double
     */
    @Override
    public void draw(GraphicsContext gc, double zoom) {}

    /**
     * Getter for Type of POI
     * @return Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Getter for name of POI
     * @return String
     */
    public String getName() {
        return name;
    }
}
