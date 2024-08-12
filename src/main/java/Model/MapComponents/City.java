package Model.MapComponents;

import Model.MapData;
import Model.OSMNode;
import Model.Type;

import java.io.Serializable;

/**
 * Class representing a city from OSM, defined as Node in osmfile
 */
public class City implements MapData, Serializable {
    String city = "";
    OSMNode node;

    /**
     * Constructor for city
     * @param city String
     */
    public City(String city) {
        this.city = city;
    }

    /**
     * Empty constructor for city
     */
    public City(){

    }

    /**
     * Setter for city
     * @param city String
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Setter for the OSMNode
     * @param node OSMNode
     */
    public void setNode(OSMNode node) {
        this.node = node;
    }

    /**
     * Returns this class
     * @return class of the object: City.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }

    /**
     * Gets the point of the City from OSMNode
     * @return float[] with [0] = x, [1] = y
     */
    @Override
    public float[] getAsPoint() {
        return node.getAsPoint();
    }

    /**
     * Returns the max x value
     * @return float max x
     */
    @Override
    public float getMaxX() {
        return node.getMaxX();
    }

    /**
     * Returns the min x value
     * @return float min x
     */
    @Override
    public float getMinX() {
        return node.getMinX();
    }

    /**
     * Returns the max y value
     * @return float max y
     */
    @Override
    public float getMaxY() {
        return node.getMaxY();
    }

    /**
     * Returns the min y value
     * @return float min y
     */
    @Override
    public float getMinY() {
        return node.getMinY();
    }

    /**
     * @return null
     */
    @Override
    public Type getType() {
        return null;
    }

    /**
     * Getter for city
     * @return String city
     */
    public String getCity() {
        return city;
    }

    /**
     * Getter for OSMNode
     * @return OSMNode
     */
    public OSMNode getNode() {
        return node;
    }
}
