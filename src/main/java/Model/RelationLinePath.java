package Model;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

/**
 * Used for creating linepaths for OSMRelations
 */
public class RelationLinePath extends ArrayList<LinePath> implements Drawable {
    private Type type;

    /**
     * Constructor for RelationLinePath
     * Runs through every OSMWay in the OSMRelation and creates a linepath with each OSMWay
     * @param currentRelation OSMRelation
     */
    public RelationLinePath(OSMRelation currentRelation, Type type) {
        this.type = type;
        for (var way : currentRelation) {
            if (way != null && way.size() != 0) {
                add(new LinePath(way, type));
            }
        }
    }

    /**
     * Draw method for the RelationLinePath
     * @param gc GraphicsContext
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        for (var line : this) {
            line.trace(gc, Double.POSITIVE_INFINITY, false);
        }
        gc.stroke();
    }

    /**
     * Draw method for the RelationLinePath with zoom level taking into account to apply poly level
     * @param gc GraphicsContext
     * @param zoom double
     */
    @Override
    public void draw(GraphicsContext gc, double zoom) {
        gc.beginPath();
        for (var line : this) {
            line.trace(gc, zoom, true);
        }
        gc.stroke();
    }

    /**
     * Returns the type of the OSMRelation
     * @return Type
     */
    @Override
    public Type getType() {
        return type;
    }
}