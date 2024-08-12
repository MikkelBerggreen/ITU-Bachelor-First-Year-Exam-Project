package Model.Pathfinding;

import Model.OSMNode;
import Model.MapComponents.Highway;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The graph represents the network of highways created during the loading of the given file as an undirected graph of edges and vertices
 * Supports insertion of class type Highway
 */
public class Graph implements Serializable {

    private Map<OSMNode, Integer> vertexMap;
    private ArrayList<ArrayList<Edge>> adj; // Make array instead
    private ArrayList<Vertex> vertexArray;
    private List<Edge> edges;
    private int index = 0;


    /**
     * Constructor for graph
     * Initializes an empty graph with 0 vertices and 0 edges
     */
    public Graph() {
        vertexMap = new HashMap<>();
        adj = new ArrayList<>();
        vertexArray = new ArrayList<>();
        edges = new ArrayList<>();
    }


    /**
     * Inserts highway into the graph by adding and creating a new vertex with the highway's OSMNode
     * The vertex is assigned an index depending on the order it is inserted into the vertexArray
     * @param highway Highway
     */
    public void insert(Highway highway) {
        for(OSMNode node : highway.getOSMWay()) {
            // Returns if vertex is already mapped
            if (vertexMap.containsKey(node)) {
                continue;
            }

            vertexMap.put(node, index);

            Vertex vertex = new Vertex(node);
            vertexArray.add(vertex);

            adj.add(new ArrayList<>());
            index++;
        }
    }


    /**
     * Returns the Vertex based on it's index given during insertion
     * @param index int
     * @return Vertex
     */
    public Vertex getVertexFromIndex(int index) {
        return vertexArray.get(index);
    }

    /**
     * Returns the index of the given node based on the node's index given during insertion
     * @param node OSMNode
     * @return int index
     */
    public int getIndexFromNode(OSMNode node) {
        return vertexMap.get(node);
    }

    /**
     * Adds edges between to and from for each edge in the given highway in both directions
     * @param highway Highway
     * @param oneWay boolean
     * @param isRoundabout boolean
     * @param speedLimit int
     * @param drivable boolean
     * @param bikable boolean
     * @param walkable boolean
     */
    public void addEdges(Highway highway, boolean oneWay, boolean isRoundabout, int speedLimit, boolean drivable, boolean bikable, boolean walkable) {
        edges = highway.calculateEdges(oneWay, isRoundabout, speedLimit, drivable, bikable, walkable);
        for(Edge edge : edges) {
            addEdge(edge);
        }
    }

    private void addEdge(Edge v) {
        OSMNode one = v.getEither();
        OSMNode two = v.getOther();

        int index1 = vertexMap.get(one);
        int index2 = vertexMap.get(two);

        vertexArray.get(index1).addEdge(v);
        vertexArray.get(index2).addEdge(v);

        adj.get(index1).add(v);
        adj.get(index2).add(v);
    }


    /**
     * Getter for number of vertices in the graph
     * @return the number of vertices in the graph
     */
    public int numberOfVertices() {
        return index;
    }


    /**
     * Returns list of edges in the graph
     * @return List of edges
     */
    public List<Edge> getEdges() {
        return edges;
    }

}
