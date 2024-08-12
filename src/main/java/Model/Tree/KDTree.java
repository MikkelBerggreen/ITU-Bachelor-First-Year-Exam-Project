package Model.Tree;

import Model.MapData;
import Model.MapComponents.Highway;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * KD Tree implemented as a 2d-tree representation of points on a 2D plane.
 */
public class KDTree implements Serializable {
    private MapData data;
    private float[] point;

    private KDTree leftChild;
    private KDTree rightChild;
    private boolean isVertical = true; //If false then horizontal

    /**
     * Constructor for new KDTree
     * initializes and empty float[] array for the root point
     */
    public KDTree() {
        point = new float[2]; // 2 size as it only contains x and y
    }


    // Sub tree
    private KDTree(MapData data, boolean vertical) {
        this.data = data;
        point = data.getAsPoint();
        this.isVertical = vertical;
    }


    //Determines whether an OSMWay should be placed as a left or right child in the KD Tree
    private boolean shouldGoLeft(float[] p) {
        // Determines whether to look for x or y
        int firstCoord = isVertical ? 0 : 1;
        int secondCoord = firstCoord == 0 ? 1 : 0;

        // In case p.x == root.x search for bigger y value
        if (p[firstCoord] == point[firstCoord]) {
            if (p[secondCoord] >= point[secondCoord]) {
                return false;
            } else {
                return true;
            }
        }

        if (p[firstCoord] > point[firstCoord]) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * Inserts the given MapData into the KDTree as a point
     * @param data MapData
     */
    public void insert(MapData data) {
        // This only happens if root node
        if (this.data == null) {
            this.data = data;
            point = data.getAsPoint();
            return;
        }

        KDTree subKDTree = new KDTree(data, !isVertical);

        float[] p = data.getAsPoint();

        // In case p.x == root.x search for bigger y value
        if (shouldGoLeft(p)) {
            if (leftChild != null) {
                leftChild.insert(data);
            } else {
                leftChild = subKDTree;
            }
        } else {
            if (rightChild != null) {
                rightChild.insert(data);
            } else {
                rightChild = subKDTree;
            }
        }
    }

    private boolean isInRect(float x1, float y1, float x2, float y2) {
        if (data == null) {
            return false;
        }

        // Checks if overlap with OSMWay bounds
        float rectWidth = Math.abs(x2 - x1);
        float rectHeight = Math.abs(y2 - y1);

        float osmWidth = Math.abs(data.getMaxX() - data.getMinX());
        float osmHeight = Math.abs(data.getMaxY() - data.getMinY());

        // Check collision
        if (x1 < data.getMinX() + osmWidth &&
                x1 + rectWidth > data.getMinX() &&
                y1 < data.getMinY() + osmHeight &&
                y1 + rectHeight > data.getMinY()) {
            return true;

        }

        return false;
    }




    // returns 2 if point is within the rect. Returns 1 if point is below/left of the rect. Returns 0 if point is above/right of the rect
    private int isOver(float x1, float y1, float x2, float y2) {

        if (isVertical) {
            if (point[0] >= x1 && point[0] <= x2) {
                return 2;
            }
            if (point[0] < x1) {
                return 1;
            }
            if (point[0] > x2) {
                return 0;
            }
        } else {
            if (point[1] >= y1 && point[1] <= y2) {
                return 2;
            }
            if (point[1] < y1) {
                return 1;
            }
            if (point[1] > y2) {
                return 0;
            }
        }
        return 2;
    }


    private float distToNode(float x, float y) {
        float x1 = x - point[0];
        float y1 = y - point[1];
        return (float) Math.sqrt((x1 * x1) + (y1 * y1)); // Can remove sqrt to optimize further mby?
    }

    private float distToLine(float[] p) {
        if (isVertical) {
            return p[0] - point[0];
        } else {
            return p[1] - point[1];
        }
    }

    private float distBetweenNodes(MapData data, float[] p) {
        float[] d = data.getAsPoint();
        float x1 = d[0] - p[0];
        float y1 = d[1] - p[1];
        return (float) Math.sqrt((x1 * x1) + (y1 * y1)); // Can remove sqrt to optimize further mby?
    }


    // Computes nearest neighbor and checks if it matches the string (if string is empty every string is matched)
    private Highway computeNearestNeighbor(float x, float y, Highway closestMatch, String address) {
        // Add point to compute with
        float[] p = new float[2];
        p[0] = x;
        p[1] = y;

        // Calculate distance to node
        float dist = distToNode(x, y);

        float nearestDist = distBetweenNodes(closestMatch, p);

        // Check if we've found a closer match
        if (dist < nearestDist && !(((Highway) data).getStreet().isEmpty()) && ((Highway) data).getStreet().contains(address)) {
            // Update values
            closestMatch = (Highway) data;
        }

        Highway result = null;

        float wentLeft = distToLine(p);

        if (wentLeft < 0) {
            if (leftChild != null) {
                result = leftChild.computeNearestNeighbor(x, y, closestMatch, address);
            }
        } else if (rightChild != null) {
            result = rightChild.computeNearestNeighbor(x, y, closestMatch, address);
        }

        if ((result != null && closestMatch != result && result.getStreet().contains(address))) {
            closestMatch = result;
            nearestDist = distBetweenNodes(result, p);

        }

        if (Math.abs(wentLeft) < nearestDist) { // TODO: 25/04/2020 Possible mistake where we don't check for closer match
            if (wentLeft < 0) {
                if (rightChild != null) {
                    result = rightChild.computeNearestNeighbor(x, y, closestMatch, address);
                }
            } else {
                if (leftChild != null) {
                    result = leftChild.computeNearestNeighbor(x, y, closestMatch, address);
                }
            }

            if ((result != null && closestMatch != result && result.getStreet().contains(address))) {
                closestMatch = result;
            }
        }
        return closestMatch;
    }

    /**
     * Returns the Highway found through nearest Neighbor search if it matches the input
     * @param x float value of the x-coordinate of the point to search the nearest highway at
     * @param y float value of the y-coordinate of the point to search the nearest highway at
     * @param addressMatch String used to ensure a match
     * @return Highway
     */
    public Highway nearestNeighbor(float x, float y, String addressMatch) {

        // Ensures we only find nearest neighbor on KDTrees with Roads
        if (data == null || data.getClassType() != Highway.class) {
            return null;
        }

        // If we haven't found a highway with given name, just return the closest match
        Highway way = computeNearestNeighbor(x, y, (Highway) data, addressMatch);
        if (way.getStreet().contains(addressMatch) || addressMatch.contains(way.getStreet())) {
            return way;
        }
        return nearestNeighbor(x, y);
    }


    /**
     * Returns the Highway found through nearest Neighbor search
     * @param x float value of the x-coordinate of the point to search the nearest highway at
     * @param y float value of the x-coordinate of the point to search the nearest highway at
     * @return highway
     */
    public Highway nearestNeighbor(float x, float y) {

        // Ensures we only find nearest neighbor on KDTrees with Roads
        if (data == null || data.getClassType() != Highway.class) {
            return null;
        }
        return computeNearestNeighbor(x, y, (Highway) data, "");
    }


    /**
     * Returns a list of all MapData nodes that are within the given 4 points corresponding to a rect to search within.
     * Calculates bounds for each MapData and uses collision check to detect whether it is within the given rect or not
     * @param x1 float value for point of the rect to search within
     * @param y1 float value for point of the rect to search within
     * @param x2 float value for point of the rect to search within
     * @param y2 float value for point of the rect to search within
     * @return list of MapData
     */
    public List<MapData> rectSearch(float x1, float y1, float x2, float y2) {

        List<MapData> ways = new ArrayList<>();

        if (isInRect(x1, y1, x2, y2)) {

            ways.add(data);

            if (leftChild != null) {
                List<MapData> left = leftChild.rectSearch(x1, y1, x2, y2);
                ways.addAll(left);
            }
            if (rightChild != null) {
                List<MapData> right = rightChild.rectSearch(x1, y1, x2, y2);
                ways.addAll(right);
            }

        } else {

            List<MapData> left = new ArrayList<>();
            List<MapData> right = new ArrayList<>();

            // If point is outside rect, check left or right sub-tree
            int toGo = isOver(x1, y1, x2, y2);

            if (toGo == 2) {
                if (leftChild != null) {
                    left = leftChild.rectSearch(x1, y1, x2, y2);
                }
                if (rightChild != null) {
                    right = rightChild.rectSearch(x1, y1, x2, y2);
                }
            }
            if (toGo == 1) {
                if (rightChild != null) {
                    right = rightChild.rectSearch(x1, y1, x2, y2);
                }
            }
            if (toGo == 0) {
                if (leftChild != null) {
                    left = leftChild.rectSearch(x1, y1, x2, y2);
                }
            }

            ways.addAll(left);
            ways.addAll(right);
        }

        return ways;
    }
}