package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing way from .osm file
 * An OSMWay consists of a list of OSMNodes determining the way
 * OSMWay contains a min&max x and y used to determine the bounds of the way
 * OSMWay also contains a type to determine what Type of OSMWay it is
 */
public class OSMWay extends ArrayList<OSMNode> implements MapData, Serializable {
    private Type type;

    private float minX = Float.POSITIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;

    /**
     * Empty constructor for OSMWay
     */
    public OSMWay() {
    }

    /**
     * Adds an OSMNode to the OSMWay
     * @param osmNode OSMNode
     * @return boolean
     */
    @Override
    public boolean add(OSMNode osmNode) {
        float x = osmNode.getLon();
        float y = osmNode.getLat();
        if (x > maxX) {
            maxX = x;
        }
        if (x < minX) {
            minX = x;
        }

        if (y > maxY) {
            maxY = y;
        }
        if (y < minY) {
            minY = y;
        }
        return super.add(osmNode);
    }


    /**
     * Gets the first OSMNode of the OSMWay used for drawing coastlines
     * @return OSMNode
     */
    public OSMNode first() {
        return get(0);
    }

    /**
     * Gets the last OSMNode of the OSMWay used for drawing coastlines
     * @return OSMNode
     */
    public OSMNode last() {
        return get(size() - 1);
    }

    /**
     * Connects the two OSMWays depending on which Way comes after to create coastline
     * @param before OSMWay
     * @param after OSMWay
     * @return OSMWay consisting of the OSMWay before connected with the OSMWay after assuming they are not null
     */
    public static OSMWay merge(OSMWay before, OSMWay after) {
        if (before == null) {
            return after;
        }
        if (after == null) {
            return before;
        }
        OSMWay result = new OSMWay();
        if(before.first() == after.first()) {
            result.addAll(before);
            Collections.reverse(result);
            result.remove(result.size() - 1);
            result.addAll(after);
        } else if (before.first() == after.last()) {
            result.addAll(after);
            result.remove(result.size() - 1);
            result.addAll(before);
        } else if (before.last() == after.first()) {
            result.addAll(before);
            result.remove(result.size() - 1);
            result.addAll(after);
        } else if (before.last() == after.last()) {
            var tmp = new ArrayList<>(after);
            Collections.reverse(tmp);
            result.addAll(before);
            result.remove(result.size() - 1);
            result.addAll(tmp);
        } else {
            throw new IllegalArgumentException("Cannot merge unconnected OSMWays");
        }
        return result;
    }


    /**
     * Returns the minimum x value of the OSMWay determined from the minimum x value of all OSMNodes the way contains
     * @return float
     */
    public float getMinX() {
        return minX;
    }

    /**
     * Returns the minimum y value of the OSMWay determined from the minimum y value of all OSMNodes the way contains
     * @return float
     */
    public float getMinY() {
        return minY;
    }

    /**
     * Returns the maximum x value of the OSMWay determined from the minimum x value of all OSMNodes the way contains
     * @return float
     */
    public float getMaxX() {
        return maxX;
    }

    /**
     * Returns the maximum y value of the OSMWay determined from the minimum y value of all OSMNodes the way contains
     * @return float
     */
    public float getMaxY() {
        return maxY;
    }

    /**
     * Returns the type of OSMWay
     * @return Type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the OSMWay
     * @param type Type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets OSMNode from the OSMWay with index
     * @param index int
     * @return OSMNode
     */
    public OSMNode getOSMNode(int index) {
        return super.get(index);
    }

    /**
     * Gets the type of class the class
     * @return OSMWay.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }


    /**
     * @return returns middle node lon & lat (x, y) of the OSMWay
     */
    @Override
    public float[] getAsPoint() {
        int middleNode = this.size()/2;
        return new float[] {this.get(middleNode).getLon(), this.get(middleNode).getLat()};
    }
}
