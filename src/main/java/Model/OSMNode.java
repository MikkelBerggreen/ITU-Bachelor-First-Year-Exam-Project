package Model;

import java.io.Serializable;

/**
 * Class representing node from .osm file
 * OSMNode contains a coordinate of lon and lat
 */
public class OSMNode implements MapData, Serializable {
    // Node from OSM has id, lon & lat.
    private float lon;
    private float lat;

    /**
     * Constructor for OSMNode
     * @param lon float
     * @param lat float
     */
    public OSMNode(float lon, float lat) {
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * Getter for lat
     * @return float lat
     */
    public float getLat() {
        return lat;
    }

    /**
     * Getter for lon
     * @return float lon
     */
    public float getLon() {
        return lon;
    }


    /**
     * Returns the type of class
     * @return OSMNode.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }

    /**
     * Returns the OSMNode as a float[] containing a point
     * @return float[] with [0] = x, [1] = y
     */
    @Override
    public float[] getAsPoint() {
        return new float[] {lon, lat};
    }

    /**
     * Getter for the max X(lon) value of the OSMNode (same as lon)
     * @return float lon
     */
    @Override
    public float getMaxX() {
        return lon;
    }

    /**
     * Getter for the min X(lon) value of the OSMNode (same as lon)
     * @return float lon
     */
    @Override
    public float getMinX() {
        return lon;
    }

    /**
     * Getter for the max Y(lat) value of the OSMNode (same as lat)
     * @return float lat
     */
    @Override
    public float getMaxY() {
        return lat;
    }

    /**
     * Getter for the min Y(lat) value of the OSMNode (same as lat)
     * @return float lat
     */
    @Override
    public float getMinY() {
        return lat;
    }

    /**
     * Gets the type of OSMNode (null)
     * @return null
     */
    @Override
    public Type getType() {
        return null;
    }

}
