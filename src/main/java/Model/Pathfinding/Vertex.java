package Model.Pathfinding;

import Model.OSMNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Vertex used for graph and is represented through a OSMNode
 * Contains a node and a list of all the edges connected to the vertex
 */
public class Vertex implements Serializable {
    private OSMNode node;
    private List<Edge> edges;

    /**
     * Constructor for vertex
     * @param node OSMNode
     */
    public Vertex(OSMNode node) {
        this.node = node;
        this.edges = new ArrayList<>();
    }


    /**
     * Adds an edge to the vertex
     * @param edge Edge
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /**
     * Getter for the list of all the vertex's edges
     * @return List of Edge
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Getter for the OSMNode
     * @return OSMNode
     */
    public OSMNode getNode() {
        return node;
    }
}
