package Model;

import javafx.scene.canvas.GraphicsContext;
import java.io.Serializable;

/**
 * LinePath used for drawing on canvas and is defined though a list of coords
 */
public class LinePath implements Drawable, Serializable {
    float[] coords;
    Type type;

    /**
     * Constructor for creating a linepath with a float array of coords and the linepath's associated Type
     * @param coords float[]
     * @param type Type
     */
    public LinePath(float[] coords, Type type) {
        this.coords = coords;
        this.type = type;
    }

    /**
     * Constructor for creating a linepath from an OSMWay and the Linepath's associated Type
     * @param way
     * @param type
     */
    public LinePath(OSMWay way, Type type) {
        coords = new float[way.size() * 2];
        for (int i = 0; i < way.size(); i++) {
            coords[i * 2] = way.get(i).getLon();
            coords[i * 2 + 1] = way.get(i).getLat();
        }
        this.type = type;
    }

    /**
     * Draw method for the LinePath
     * @param gc GraphicsContext
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        trace(gc, Double.POSITIVE_INFINITY, false);
        gc.stroke();
    }

    /**
     * Draw method for the LinePath where Poly level is taken into account based on the level of zoom
     * @param gc GraphicsContext
     * @param zoom double
     */
    @Override
    public void draw(GraphicsContext gc, double zoom) {
        gc.beginPath();
        trace(gc, zoom, true);
        gc.stroke();
    }

    /**
     * Returns the type of the linepath
     * @return Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * The trace method responsible for tracing a line between coordinate/point in the linepath
     * Traces through every coordinate/point if no poly level is applied, otherwise skips points depending on the level of poly
     * @param gc GraphicsContext
     * @param zoom double
     * @param poly boolean whether to apply poly or not
     */
    protected void trace(GraphicsContext gc, double zoom, boolean poly) {
        gc.moveTo(coords[0], coords[1]);

        int polyLevel = 1;

        if (poly) {
            polyLevel = getPolyLevel(zoom, type);
        }

        // Make sure we don't end in an endless loop
        if (polyLevel < 1) {
            polyLevel = 1;
        }

        int i;
        for (i = 2; i < coords.length; i += (polyLevel * 2)) {
            gc.lineTo(coords[i], coords[i+1]);
        }

        // Make sure we draw last part of way
        if (i - (polyLevel * 2)  != coords.length - 2) {
            int length = coords.length - 2;
            gc.lineTo(coords[length], coords[length + 1]);
        }
    }


    // poly level as an int >= 1 representing the amount of points to skip
    private int getPolyLevel(double zoom, Type type) {
        // Special cases
        if (type == Type.RESIDENTIAL) { // || type == Type.FOREST || type == Type.FARM) {
            if (zoom > 3600) {
                return 1;
            } else {
                return 20;
            }
        }
        if (type == Type.FOREST || type == Type.PARK) {
            if (zoom > 5700) {
                return 3;
            } else {
                return 1;
            }
        }

        if (zoom > 104000) {
            return 1;
        }
        if (zoom > 70000) {
            return 2;
        }

        if (zoom > 25000) {
            return 4;
        }
        if (zoom > 18000) {
            return 4;
        }
        if (zoom > 10000) {
            return 8;
        }
        if (zoom > 5700) {
            return 10;
        }
        if (zoom > 900) {
            return 12;
        }
        if (zoom > 500) {
            return 24;
        }
        return 40;
    }
}