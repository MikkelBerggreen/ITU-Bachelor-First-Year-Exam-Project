package Model.MapComponents;

import Model.MapData;
import Model.OSMWay;
import Model.Pathfinding.Edge;
import Model.LinePath;

import java.util.ArrayList;
import java.util.List;

/**
 * Highway class representing highway from OSM
 */
public class Highway extends LinePath implements MapData {
    private OSMWay way;
    private String street = "";

    /**
     * Constructor for highway
     * @param way OSMWay
     */
    public Highway(OSMWay way) {
        super(way, way.getType());
        this.way = way;
    }

    /**
     * Calculates all edges in the highway
     * There is an edge between each node in the highway
     * @param isOneWay boolean for whether the edge is directional
     * @param isRoundabout boolean for whether it is a roundabout
     * @param speedLimit int
     * @param drivable boolean for whether it is legal to drive on
     * @param bikable boolean for whether it is legal to bike on
     * @param walkable boolean for whether it is legal to walk on
     * @return ArrayList of the highway's edges
     */
    public List<Edge> calculateEdges(boolean isOneWay, boolean isRoundabout, int speedLimit, boolean drivable, boolean bikable, boolean walkable) {
        List<Edge> edges = new ArrayList<>();

        // Loops through all points in OSMWay
        for(int i = 0; i < way.size() - 1; i++) {
            // Generate edge between nodes
            Edge edge = new Edge(way.get(i), way.get(i + 1), this, isOneWay, isRoundabout, speedLimit, drivable, bikable, walkable);
            edges.add(edge);
        }

        return edges;
    }

    /**
     * Returns the type of class
     * @return Highway.class
     */
    @Override
    public Class getClassType() {
        return this.getClass();
    }

    /**
     * Gets the middle point of the highway
     * @return float[] with [0] = x, [1] = y
     */
    @Override
    public float[] getAsPoint() {
        return way.getAsPoint();
    }

    /**
     * getter for max x value
     * @return float max x
     */
    @Override
    public float getMaxX() {
        return way.getMaxX();
    }

    /**
     * getter for min x value
     * @return float min x
     */
    @Override
    public float getMinX() {
        return way.getMinX();
    }

    /**
     * getter for max y value
     * @return float max y
     */
    @Override
    public float getMaxY() {
        return way.getMaxY();
    }

    /**
     * getter for min y value
     * @return float min y
     */
    @Override
    public float getMinY() {
        return way.getMinY();
    }


    /**
     * Getter for street
     * @return String
     */
    public String getStreet() { return street; }

    /**
     * Getter for way
     * @return OSMWay
     */
    public OSMWay getOSMWay() {
        return way;
    }


    /**
     * Setter for street field
     * @param street String
     */
    public void setStreet(String street) { this.street = street; }

}
