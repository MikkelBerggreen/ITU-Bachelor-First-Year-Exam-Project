package Model.MapComponents;

import Model.Drawable;
import Model.ImageHandler;
import Model.MapData;
import Model.Type;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.Serializable;

/**
 * MapIcon represents a hotspot/point of interest with lat & lon coords.
 * Gets the corresponding image from ImageHandler
 */
public class MapIcon implements Drawable, MapData, Serializable {
    protected float lon;
    protected float lat;
    protected Type type;

    /**
     * Constructor for MapIcon
     * @param lon longitude coordinate
     * @param lat latitude coordinate
     * @param type Type
     */
    public MapIcon(float lon, float lat, Type type) {
        this.lon = lon;
        this.lat = lat;
        this.type = type;
    }

    /**
     * Returns the image associated with the type of mapicon
     * @return Image
     */
    protected Image getImage() {
        return ImageHandler.getInstance().getImage(type);
    }

    /**
     * Draws the image on canvas with a fixed width and height
     * @param gc GraphicsContext
     */
    public void draw(GraphicsContext gc) {
        gc.drawImage(getImage(), lon, lat, 0.0001, 0.0001);
    }

    /**
     * Draws the image on canvas with a fixed width and height
     * Zoom is not used
     * @param gc GraphicsContext
     * @param zoom double
     */
    @Override
    public void draw(GraphicsContext gc, double zoom) {
        draw(gc);
    }


    /**
     * Returns the type of MapIcon
     * @return Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Returns the class
     * @return MapIcon.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }

    /**
     * Returns the coordinates of the MapIcon
     * @return float[] with [0] = x, [1] = y
     */
    @Override
    public float[] getAsPoint() {
        return new float[] {lon, lat};
    }

    /**
     * Getter for max x value represented as longitude
     * @return max x
     */
    @Override
    public float getMaxX() {
        return lon;
    }

    /**
     * Getter for min x value represented as longitude
     * @return min x
     */
    @Override
    public float getMinX() {
        return lon;
    }

    /**
     * Getter for max y value represented as latitude
     * @return max y
     */
    @Override
    public float getMaxY() {
        return lat;
    }

    /**
     * Getter for min y value represented as latitude
     * @return min y
     */
    @Override
    public float getMinY() {
        return lat;
    }
}
