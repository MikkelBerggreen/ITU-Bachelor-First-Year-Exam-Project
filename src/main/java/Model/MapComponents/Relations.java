package Model.MapComponents;

import Model.*;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

/**
 * OSM Relation - group of elements (OSMWays) used to model logical or geographic relationships between objects
 */
public class Relations implements MapData, Drawable, Serializable {
    Drawable shape;
    Type type;

    float minLon, maxLon, minLat, maxLat;
    float[] point;


    /**
     * Constructor for relation
     * @param currentRelation OSMRelation
     * @param type Type
     */
    public Relations(OSMRelation currentRelation, Type type) {
        shape = new RelationLinePath(currentRelation, type);
        this.type = type;

        for (var way : currentRelation) {

            if (way.getMinX() < minLon) {
                minLon = way.getMinX();
            }
            if (way.getMaxX() > maxLon) {
                maxLon = way.getMaxX();
            }
            if (way.getMinY() < minLat) {
                minLat = way.getMinY();
            }
            if (way.getMaxY() > maxLat) {
                maxLat = way.getMaxY();
            }
        }
        if (currentRelation.size() != 0) {
            OSMWay midWay = currentRelation.get(currentRelation.size()/2);
            point = midWay.getAsPoint();
        }

    }

    /**
     * Draws the relation
     * @param gc GraphicsContext
     */
    @Override
    public void draw(GraphicsContext gc) {
        shape.draw(gc);
    }

    /**
     * Draws the relation with poly
     * @param gc GraphicsContext
     * @param zoom double
     */
    @Override
    public void draw(GraphicsContext gc, double zoom) {
        shape.draw(gc, zoom);
    }

    /**
     * Returns the type of class
     * @return Relations.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }

    /**
     * Gets the middle point of the relation determined from the OSMWays in the relation
     * @return float[] with [0] = x, [1] = y
     */
    @Override
    public float[] getAsPoint() {
        return point;
    }

    /**
     * Getter for max x value represented as longitude
     * @return max x
     */
    @Override
    public float getMaxX() {
        return maxLon;
    }

    /**
     * Getter for min x value represented as longitude
     * @return min x
     */
    @Override
    public float getMinX() {
        return minLon;
    }

    /**
     * Getter for max y value represented as latitude
     * @return max y
     */
    @Override
    public float getMaxY() {
        return maxLat;
    }

    /**
     * Getter for min y value represented as latitude
     * @return min y
     */
    @Override
    public float getMinY() {
        return minLat;
    }

    /**
     * Returns the type of relation
     * @return Type
     */
    @Override
    public Type getType() {
        return type;
    }
}
