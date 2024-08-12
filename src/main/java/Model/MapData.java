package Model;

/**
 * MapData interface represents data located on the map and is used for KDTree
 * Has a point representing the center point of it's location
 * Has a max/min x and y as well as a field Type representing the type of MapData
 */
public interface MapData {
    Class getClassType();

    float[] getAsPoint();

    float getMaxX();
    float getMinX();
    float getMaxY();
    float getMinY();

    Type getType();
}
