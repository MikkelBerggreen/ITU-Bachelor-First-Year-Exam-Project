package Model.Pathfinding;

import Model.OSMNode;
import Model.MapComponents.Highway;
import java.io.Serializable;

/**
 * Edge representing each section between each node in highways and has a weight depending on its distance in KM
 * Edges are by standard directional but boolean can make it function as a bidirectional edge
 * Edge has a reference to the highway it's a part of as well as other properties such as:
 * is it drivable, bikable, walkable? Speedlimit & whether it is a roundabout
 */
public class Edge implements Serializable {
    private OSMNode either;
    private OSMNode other;
    private float dist;
    private Highway highway;
    private boolean drivable, bikable, walkable;
    private boolean isOneWay;
    private boolean isRoundabout;
    private int speedLimit;


    /**
     * Constructor for edge
     * @param either the start OSMNode of the edge
     * @param other the end OSMNode of the edge
     * @param highway Corresponding highway
     * @param isOneWay boolean for whether it is a oneway edge - can be considered bidirectional if it is not
     * @param isRoundabout boolean
     * @param speedLimit int
     * @param drivable boolean
     * @param bikable boolean
     * @param walkable boolean
     */
    public Edge(OSMNode either, OSMNode other, Highway highway, boolean isOneWay, boolean isRoundabout, int speedLimit, boolean drivable, boolean bikable, boolean walkable) {
        this.either = either;
        this.other = other;
        this.highway = highway;
        this.isOneWay = isOneWay;
        this.isRoundabout = isRoundabout;
        this.speedLimit = speedLimit;
        this.drivable = drivable;
        this.bikable = bikable;
        this.walkable = walkable;


        calculateWeight();
    }

    /**
     * Calculates the weight of the edge as the distance of the edge
     */
    public void calculateWeight() {

        double denmarkLat = 56.023;

        double latDegToKm = 110.574;
        double lonDegToKm = 110.320 * Math.cos(denmarkLat); // Formula: 110.320 * cos(denmark Lat)

        double latDiff = Math.abs(either.getLat() - other.getLat());
        double lonDiff = Math.abs(either.getLon() - other.getLon());

        double xkm = latDiff * latDegToKm;
        double ykm = lonDiff * lonDegToKm;

        this.dist = (float) Math.sqrt((xkm * xkm) + (ykm * ykm));
    }

    /**
     * Getter for the either OSMNode
     * @return OSMNode
     */
    public OSMNode getEither() {
        return either;
    }

    /**
     * Getter for the other OSMNode
     * @return OSMNode
     */
    public OSMNode getOther() {
        return other;
    }

    /**
     * Getter for the dist of the edge
     * @return double
     */
    public double getDist() {
        return dist;
    }

    /**
     * Getter isDrivable property
     * @return boolean true if it is legal to drive on
     */
    public boolean isDrivable() {
        return drivable;
    }

    /**
     * Getter isBikable property
     * @return boolean true if it is legal to bike on
     */
    public boolean isBikable() {
        return bikable;
    }

    /**
     * Getter isWalkable property
     * @return boolean true if it is legal to walk on
     */
    public boolean isWalkable() {
        return walkable;
    }


    /**
     * Getter for isRoundabout property
     * @return boolean true if it is a roundabout
     */
    public boolean isRoundabout() {
        return isRoundabout;
    }

    /**
     * Getter for isOneWay property
     * @return boolean true if it is oneway
     */
    public boolean isOneWay() {
        return isOneWay;
    }

    /**
     * Getter for speed limit
     * @return int
     */
    public int getSpeedLimit() {
        return speedLimit;
    }

    /**
     * Getter for the corresponding Highway the edge is a part of
     * @return Highway
     */
    public Highway getHighway() {
        return highway;
    }

}
