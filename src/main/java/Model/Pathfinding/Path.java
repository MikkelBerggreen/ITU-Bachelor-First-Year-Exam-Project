package Model.Pathfinding;

import Model.OSMNode;
import Model.OSMWay;
import java.util.*;

/**
 * used to calculate shortest or fastest path/route between given source and destination
 * Path represents the dijkstra algorithm
 */
public class Path {
    private Graph graph;
    private IndexMinPQ pq;
    private double[] distTo;
    private Edge[] edgeTo;
    private boolean[] marked;
    private float totalDistance;
    private float totalTravelTime;
    private int source;
    private int destination;
    private Set<OSMWay> edgesGoneThrough;
    private int transportationMode = 0;


    /**
     * Constructor for path
     * @param graph graph
     * @param source int representing the index of the source node in graph
     * @param destination int representing the index of the destination node in graph
     * @param transportationMode int between 0 and 2 determining the mode of transporation - 0 = car, 1 = bike, 2 = walk
     */
    public Path(Graph graph, int source, int destination, int transportationMode) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
        this.transportationMode = transportationMode;
        int size = graph.numberOfVertices();

        edgesGoneThrough = new HashSet<>();
        distTo = new double[size];
        edgeTo = new Edge[size];
        marked = new boolean[size];

        for (int i = 0; i < graph.numberOfVertices(); i++) {
            distTo[i] = Double.POSITIVE_INFINITY;
        }

        distTo[source] = 0.0;

        pq = new IndexMinPQ(size);

        pq.insert(source, distTo[source]);

        relaxEdges(transportationMode);
    }


    private OSMNode getNextNode(Edge edge, Vertex vertex) {
        OSMNode node;

        if (edge == null || edge.getEither() == null) {
            return edge.getOther();
        }
        // Which node leads to this
        if(edge.getEither() == vertex.getNode()) {
            node = edge.getOther();
        } else {
            node = edge.getEither();
        }

        return node;
    }


    private List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();

        int nextNode = destination;

        while(true) {
            Edge edge = edgeTo[nextNode];

            edges.add(edge);

            if(nextNode == source) {
                break;
            }

            Vertex vertex = graph.getVertexFromIndex(nextNode);

            OSMNode node = getNextNode(edge, vertex);

            nextNode = graph.getIndexFromNode(node);
        }

        return edges;
    }

    /**
     * Returns the list of OSMNode that the path traverses through
     * @return List of OSMNode
     * @throws NullPointerException
     */
    public List<OSMNode> getPath() throws NullPointerException {
        List<OSMNode> nodes = new ArrayList<>();

        nodes.add(graph.getVertexFromIndex(destination).getNode());

        int nextNode = destination;

        while(true) {
            Edge edge = edgeTo[nextNode];
            Vertex vertex = graph.getVertexFromIndex(nextNode);

            OSMNode node = getNextNode(edge, vertex);

            nodes.add(node);

            if(nextNode == source) {
                break;
            }

            accumulateDistance(edge);
            accumulateTravelTime(edge);

            nextNode = graph.getIndexFromNode(node);
        }

        nodes.add(graph.getVertexFromIndex(source).getNode());

        return nodes;
    }

    // Checks whether it is allowed to follow the road according to the transporation mode and type of road
    private boolean isAllowed(Edge edge, int transportationMode) {
        // Car
        if (transportationMode == 0) {
            return edge.isDrivable();
        }

        // Bike
        if (transportationMode == 1) {
            return edge.isBikable();
        }

        // Walk
        if (transportationMode == 2) {
            return edge.isWalkable();
        }

        // Should never occur
        return true;
    }


    // calculates the weight of the edge based on speed
    private double calculateEdgeTime(Edge edge, int transportationMode) {
        double edgeDist = edge.getDist();
        double speed = 5; // Human walking pace (km/h)

        // Update car speed
        if (transportationMode == 0) {
            speed = edge.getSpeedLimit();
        }

        // Update bike speed
        if (transportationMode == 1) {
            speed = 15;
        }

        return edgeDist / speed;
    }

    // Checks if transport mode is allowed to follow road
    private boolean legalOneway(int transportationMode, Edge edge) {
        if (transportationMode == 0 || transportationMode == 1) {
            if (edge.isOneWay()) {
                return false;
            }

            if (edge.isRoundabout()) {
                return false;
            }
        }
        return true;
    }

    // transportationMode: 0 = car, 1 = bike, 2 = walk
    private void relaxEdges(int transportationMode) {
        while (!pq.isEmpty()) {
            int vertexIndex = pq.deleteMin();

            if (vertexIndex == destination) {
                pq.clear();
                return;
            }

            double dist = distTo[vertexIndex];

            Vertex vertex = graph.getVertexFromIndex(vertexIndex);

            List<Edge> edges = vertex.getEdges();

            for(Edge edge : edges) {

                if (!isAllowed(edge, transportationMode)) {
                    continue;
                }

                // Find other node than self
                OSMNode node;
                if(edge.getEither() == vertex.getNode()) {
                    node = edge.getOther();
                } else {
                    // Make sure we don't drive against a oneway street
                    if (!legalOneway(transportationMode, edge)) {
                        continue;
                    }

                    node = edge.getEither();
                }

                int adjIndex = graph.getIndexFromNode(node);

                double newDist = dist + calculateEdgeTime(edge, transportationMode);

                if (!marked[adjIndex]) {
                    // Add edges to draw
                    edgesGoneThrough.add(edge.getHighway().getOSMWay());

                    marked[adjIndex] = true;

                    distTo[adjIndex] = newDist;

                    pq.insert(adjIndex, newDist);

                    edgeTo[adjIndex] = edge;
                } else {

                    if (distTo[adjIndex] > newDist) {
                        distTo[adjIndex] = newDist;
                        edgeTo[adjIndex] = edge;

                        pq.insert(adjIndex, newDist);
                    }
                }
            }
        }
    }

    private String calculateDirection(Edge either, Edge other, int numberOfExits) {
        // Check for roundabout
        if (numberOfExits > 0) {
            return "exit nr. " + numberOfExits;
        }

        // stores x and y coords for vectors to its adjecency nodes in path
        double eitherX;
        double eitherY;

        double otherX;
        double otherY;

        // Determines the vector of the edges
        if(either.getEither() == other.getEither()) {

            eitherX = either.getOther().getLon() - either.getEither().getLon();
            eitherY = either.getOther().getLat() - either.getEither().getLat();

            otherX = other.getOther().getLon() - other.getEither().getLon();
            otherY = other.getOther().getLat() - other.getEither().getLat();

        } else if(either.getEither() == other.getOther()) {

            eitherX = either.getOther().getLon() - either.getEither().getLon();
            eitherY = either.getOther().getLat() - either.getEither().getLat();

            otherX = other.getEither().getLon() - other.getOther().getLon();
            otherY = other.getEither().getLat() - other.getOther().getLat();

        } else if(either.getOther() == other.getEither()) {

            eitherX = either.getEither().getLon() - either.getOther().getLon();
            eitherY = either.getEither().getLat() - either.getOther().getLat();

            otherX = other.getOther().getLon() - other.getEither().getLon();
            otherY = other.getOther().getLat() - other.getEither().getLat();

        } else {

            eitherX = either.getEither().getLon() - either.getOther().getLon();
            eitherY = either.getEither().getLat() - either.getOther().getLat();

            otherX = other.getEither().getLon() - other.getOther().getLon();
            otherY = other.getEither().getLat() - other.getOther().getLat();

        }

        // Calculates angle between the two vectors
        double dot = (eitherX * otherX) + (eitherY * otherY);

        double eitherLength = Math.sqrt((eitherX * eitherX) + (eitherY * eitherY));
        double otherLength = Math.sqrt((otherX * otherX) + (otherY * otherY));

        double rads = Math.acos(dot / (eitherLength * otherLength));

        double degrees = Math.toDegrees(rads);

        // Determines whether the road is considered straight
        if (degrees > 150) {
           return "straight";
        }


        // Calculate cross product of vectors to calculate which direction to turn
        double cross =  (otherX * eitherY) - (otherY * eitherX);
        if (cross > 0.0) {
            return "right";
        } else {
            return "left";
        }
    }


    /**
     * Computes the route description associated with the found path
     * @return List of string where each string represents the next direction
     */
    public List<String> computeRouteDescription() {
        List<String> directions = new ArrayList<>();
        List<Edge> edges = getEdges();

        // Reverse list since we calculate way reversed
        Collections.reverse(edges);

        Edge prev = edges.get(0);
        int numberOfExits = 0; // For roundabouts
        double dist = 0;
        for (Edge edge : edges) {

            // If roundabout check number of exits
            if (edge.isRoundabout()) {
                OSMNode otherNode = edge.getOther();
                int vertIndex = graph.getIndexFromNode(otherNode);
                Vertex vertex = graph.getVertexFromIndex(vertIndex);

                // if the vertex has more than 3 edges, then it means it's an exit in the roundabout
                List<Edge> vertexEdges = vertex.getEdges();

                int counter = 0;
                for (Edge vertexEdge : vertexEdges) {
                    if (transportationMode == 0 && !vertexEdge.isDrivable()) {
                        continue;
                    }
                    if (vertexEdge.getOther() != vertex.getNode() || (!vertexEdge.isRoundabout() && !vertexEdge.isOneWay())) {
                        counter++;
                    }
                }
                if (counter > 1) {
                    numberOfExits++;
                }
            }

            dist += edge.getDist();

            // If previous way is not a part of the roundabout then ignore directions
            if ((prev.isRoundabout() && edge.isRoundabout()) || (!prev.isRoundabout() && edge.isRoundabout())) {
                prev = edge;
                continue;
            }

            // Check for new street if last street name is not equals the current street
            if (!edge.getHighway().getStreet().equals(prev.getHighway().getStreet()) || (prev.isRoundabout() && !edge.isRoundabout())) {

                String dir = calculateDirection(prev, edge, numberOfExits);

                String distance; // Distance for the edge we're currently looking at

                // Check if we should display in km or meters
                if (dist > 1) {
                    distance = Math.round(dist * 10)/10.0 + " km";
                } else {
                    distance = (int)Math.round(dist*1000) + " m";
                }

                // If street name is undefined replace it with "next intersection"
                String streetName = edge.getHighway().getStreet().equals("") ? "next intersection" : edge.getHighway().getStreet();

                // Create string depending on direction
                String directionString = "";
                if (dir.contains("straight")) {
                    directionString = "In " + distance + " keep straight at " + streetName;
                } else if (dir.contains("exit")) {
                    directionString = "In " + distance + " take " + dir + " in the roundabout and stay on " + streetName;
                } else if (dir.contains("left") || dir.contains("right")) {
                    directionString = "In " + distance + " turn " + dir + " at " + streetName;
                }

                // Resets var
                numberOfExits = 0;
                dist = 0;

                directions.add(directionString);
            }
            if (edge == edges.get(edges.size() - 1)) {

                String distance; // Distance for the edge we're currently looking at

                // Check if we should display in km or meters
                if (dist > 1) {
                    distance = Math.round(dist * 10)/10.0 + " km";
                } else {
                    distance = (int)Math.round(dist*1000) + " m";
                }

                directions.add("In " + distance + " you will arrive at your destination");
            }
            prev = edge;
        }

        return directions;
    }


    private void accumulateTravelTime(Edge edge) {
        double travelTime = calculateEdgeTime(edge, transportationMode) * 60;
        totalTravelTime += travelTime;
    }

    private void accumulateDistance(Edge edge) {
        totalDistance += edge.getDist();
    }

    /**
     * Returns all the edges the dijkstra algorithm has visited/marked
     * @return Set of OSMWays
     */
    public Set<OSMWay> getEdgesGoneThrough() {
        return edgesGoneThrough;
    }

    /**
     * Getter for the index of the source OSMNode
     * @return int representing the index of the source OSMNode
     */
    public int getSource() {
        return source;
    }

    /**
     * Getter for the index of the destination OSMNode
     * @return int representing the index of the destination OSMNode
     */
    public int getDestination() {
        return destination;
    }


    /**
     * Getter for the total travel time of the found path
     * @return float
     */
    public float getTotalTravelTime() {
        return totalTravelTime;
    }

    /**
     * Getter for the total distance of the found oath
     * @return float
     */
    public float getTotalDistance() {
        return totalDistance;
    }

    /**
     * Returns the total travel time of the found path formatted
     * if >60 minutes it is returned as hours and minutes
     * if <60 it is returned as minutes
     * @return
     */
    public String getFormattedTotalTravelTime() {
        String time = "";

        if (totalTravelTime > 60) {
            int h = (int) Math.floor(totalTravelTime / 60);
            int m = (int) Math.floor(totalTravelTime % 60);

            time += h + " hours " + m + " minutes";
        } else {
            time += (int) Math.floor(totalTravelTime) + " minutes";
        }

        return time;
    }


    /**
     * Returns the total travel distance of the found path formatted
     * if the distance is more than 1000 meter, it is returned in km
     * otherwise it is returned as meters
     * @return String
     */
    public String getFormattedTotalDistance() {
        // totalDistance is given in kilometers
        if (totalDistance < 1) {
            return Math.round(totalDistance * 10.0) / 10.0 + " meters"; // rounds to 1 decimal
        } else {
            return Math.round(totalDistance * 10.0) / 10.0 + " km"; // rounds to 1 decimal
        }
    }
}
