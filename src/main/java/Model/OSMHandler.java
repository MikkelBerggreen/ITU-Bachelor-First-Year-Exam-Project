package Model;

import Model.AddressParser.Address;
import Model.MapComponents.*;
import Model.Pathfinding.Graph;
import Model.Tree.KDTree;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * responsible for reading the given .osm file and loading it's content
 */
public class OSMHandler {

    private float minLat, minLon, maxLat, maxLon; //These floats indicate the bounds of the map, they are used for zooming and panning.

    private List<Drawable> islands = new ArrayList<>();

    // Highway trees:
    private KDTree tertiarywayTree = new KDTree(); // Contains highways of type Tertiary
    private KDTree primarywayTree = new KDTree(); // Contains highways of types Primary and Motorway
    private KDTree highwayTree = new KDTree(); // Contains all other types of highways but Tertiary, Primary and Motorway(and motorway_junction)
    private KDTree cityNamesTree = new KDTree(); // Contains slightly larger city names
    private KDTree villageNamesTree = new KDTree(); // Contains smaller village/town/city names
    private KDTree mapIconTree = new KDTree(); // Contains MapIcons for points of interests like Cafés, restaurents etc
    private KDTree areaTree = new KDTree(); // Contains ways that shows a certain area around something (ex. Residential)
    private KDTree waterTree = new KDTree(); // Contains ways that show all types of water
    private KDTree buildingTree = new KDTree(); // Contains ways that are Buildings
    private KDTree heathTree = new KDTree(); // Contains heaths
    private KDTree meadowTree = new KDTree(); // Contain meadows
    private KDTree forestTree = new KDTree(); // Contain forests
    private KDTree parkTree = new KDTree(); // Contain parks
    private KDTree farmTree = new KDTree(); // Contain farms
    private KDTree waterwayTree = new KDTree(); // Contain waterways
    private KDTree railwayTree = new KDTree(); // Contain railways

    private SortedAddressArrayList OSMAddresses = new SortedAddressArrayList(); //The list of addresses, these are used to binary search in the addressparser.
    private SortedAddressArrayList OSMCities = new SortedAddressArrayList(); //Same as above but for cities.

    // Graph
    private Graph graph;


    /**
     * Initializes the OSMHandler
     */
    public void initOSMHandler() {
        graph = new Graph();
        islands = new ArrayList<>();

        // KD Trees
        highwayTree = new KDTree();
        tertiarywayTree = new KDTree();
        primarywayTree = new KDTree();

        mapIconTree = new KDTree();
        areaTree = new KDTree();
        waterTree = new KDTree();
        buildingTree = new KDTree();

        heathTree = new KDTree();
        meadowTree = new KDTree();
        forestTree = new KDTree();
        farmTree = new KDTree();
        waterwayTree = new KDTree();
        railwayTree = new KDTree();
        parkTree = new KDTree();
        villageNamesTree = new KDTree();
        cityNamesTree = new KDTree();

        OSMAddresses = new SortedAddressArrayList();
        OSMCities = new SortedAddressArrayList();

        minLat = 0;
        minLon = 0;
        maxLat = 0;
        maxLon = 0;
    }




    /**
     * responsible for reading and parsing the .osm file by reading keys and tags
     * @param reader XMLStreamReader
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public void loadOSM(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError {

        initOSMHandler();

        OSMNode currentNode = null;
        OSMWay currentWay = null;

        // highway values
        HighwayDecoder highwayDecoder = new HighwayDecoder();
        Map<String, String> highwayValues = new HashMap<>(); // Stores extra values if road for instance has private access but is bikable
        String highwayType = null;

        Map<Long,OSMWay> idToWay = new HashMap<>();

        Map<Long, OSMNode> nodeForHighwayID = new HashMap<>();
        Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();
        Type type = Type.UNKNOWN;
        Address refAddress = new Address();
        Address refCityAddress = new Address();
        City city = new City();
        Address osmWayAddress = new Address();
        Type currentElementType = Type.UNKNOWN;
        OSMRelation currentRelation = null;
        boolean isCurrentPointOfInterest = false;

        while (reader.hasNext()) {
            reader.next();
            switch (reader.getEventType()) {
                case START_ELEMENT: // START_ELEMENTS: member, node, nd, tag, way, relation etc.
                    String tagname = reader.getLocalName();

                    switch (tagname) {
                        case "osm":
                            break;
                        case "bounds":
                            minLat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            maxLon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxLat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            minLon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            break;
                        case "node":
                            currentElementType = Type.NODE;
                            long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            OSMNode node = new OSMNode(0.56f * lon, -lat);
                            nodeForHighwayID.put(id, node);
                            currentNode = node;
                            break;
                        case "way":
                            currentElementType = Type.WAY;
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            currentWay = new OSMWay();
                            idToWay.put(id, currentWay);
                            type = Type.UNKNOWN;
                            break;
                        case "nd":  // Adds nd ref to the current way
                            var ndref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            if (currentWay != null) {
                                OSMNode n = nodeForHighwayID.get(ndref);
                                if (n != null) {
                                    currentWay.add(n);
                                }
                            }
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (k.contains("building")) {
                                type = Type.BUILDING;
                            }
                            if (k.contains("natural")) {
                                switch (v) {
                                    case "coastline":
                                        type = Type.COASTLINE;
                                        break;
                                    case "water":
                                    case "wetland":
                                        type = Type.WATER;
                                        break;
                                    case "grassland":
                                        type = Type.FOREST;
                                        break;
                                    case "heath":
                                        type = Type.HEATH;
                                        break;
                                    case "grass":
                                        type = Type.GRASS;
                                }
                                break;
                            }
                            if (k.contains("landuse")) {
                                switch (v) {
                                    case "forest":
                                        type = Type.FOREST;
                                        break;
                                    case "meadow":
                                        type = Type.MEADOW;
                                        break;
                                    case "farmyard":
                                        type = Type.FARM;
                                        break;
                                    case "reservoir":
                                        type = Type.WATER;
                                        break;
                                    case "residential":
                                        type = Type.RESIDENTIAL;
                                        break;
                                }
                            }
                            if (k.contains("leisure")) {
                                if (v.contains("park")) {
                                    type = Type.PARK;
                                }
                            }
                            if (k.contains("waterway")) {
                                type = Type.WATERWAY;
                            }
                            if (k.contains("railway")) {
                                type = Type.RAILWAY;
                            }


                            if (k.contains("highway")) {
                                if (v.contains("tertiary")) {
                                    type = Type.TERTIARYWAY;
                                } else if (v.contains("motorway") || v.contains("motorway_junction")) {
                                    type = Type.MOTORWAY;
                                } else if (v.contains("primary")) {
                                    type = Type.PRIMARYWAY;
                                } else {
                                    type = Type.HIGHWAY;
                                }
                                highwayType = v;
                            }
                            // Points of interest
                            if (k.contains("amenity")) {
                                switch (v) {
                                    case "fuel":
                                        type = Type.FUEL;
                                        isCurrentPointOfInterest = true;
                                        break;
                                    case "fast_food":
                                    case "restaurant":
                                        type = Type.RESTAURANT;
                                        isCurrentPointOfInterest = true;
                                        break;
                                    case "cafe":
                                        type = Type.CAFE;
                                        isCurrentPointOfInterest = true;
                                        break;
                                    case "bank":
                                        type = Type.BANK;
                                        isCurrentPointOfInterest = true;
                                        break;
                                }
                            }
                            // Points of interest
                            if (k.contains("shop")) {
                                switch (v) {
                                    case "clothes":
                                        type = Type.CLOTHES;
                                        isCurrentPointOfInterest = true;
                                        break;
                                    case "houseware":
                                        type = Type.SUPERMARKET;
                                        isCurrentPointOfInterest = true;
                                        break;
                                }
                            }
                            // Checks if following conditions exists
                            if (k.contains("motor_vehicle")) {
                                highwayValues.put(k, v);
                            }

                            if (k.contains("motorcar")) {
                                highwayValues.put(k, v);
                            }

                            if (k.contains("access")) {
                                if (v.contains("private") || v.contains("forestry") || v.contains("no")) {
                                    highwayValues.put(k, v);
                                }
                            }
                            if (k.contains("bicycle")) {
                                highwayValues.put(k, v);
                            }
                            if (k.contains("foot")) {
                                highwayValues.put(k, v);
                            }
                            if (k.contains("junction")) {
                                if (v.contains("roundabout")) {
                                    highwayValues.put(k, v);
                                }
                            }
                            if (k.equals("maxspeed")) {
                                highwayValues.put(k, v);

                            }
                            if (k.equals("oneway")) {
                                highwayValues.put(k, v);
                            }

                            // Creates address objects which the map contains and adds them to OSMaddresser.
                            if (k.contains("addr:city")) {
                                refAddress.setCity(v);
                            }
                            if (k.contains("addr:housenumber")) {
                                refAddress.setHouse(v);
                            }
                            if (k.contains("addr:postcode")) {
                                refAddress.setPostcode(v);
                            }
                            if (k.contains("addr:street")) {
                                refAddress.setStreet(v);
                                refAddress.setNode(currentNode);
                                OSMAddresses.add(refAddress);
                                refAddress = new Address();
                            }
                            // Name of OSMWay
                            if (k.equals("name")) {
                                osmWayAddress = new Address();
                                if (v != null) {
                                    osmWayAddress.setStreet(v);
                                    city.setCity(v);
                                    refCityAddress.setCity(v);
                                }
                            }
                            // Makes a list of cities so it's possible to search for a city.
                            if (k.contains("place")) {
                                if (v.contains("village") || v.contains("hamlet") || v.contains("town") || v.contains("city") || v.contains("municipality")) {
                                    if (currentElementType.equals(Type.NODE)) {
                                        refCityAddress.setNode(currentNode);
                                        city.setNode(currentNode);

                                        OSMCities.add(refCityAddress);

                                        if (v.contains("town") || v.contains("city")) {
                                            cityNamesTree.insert(city);
                                        } else {
                                            villageNamesTree.insert(city);
                                        }

                                        //Finally, reset the value of the city after insertion.
                                        city = new City();
                                        refCityAddress = new Address();
                                    }
                                }
                            }
                            break;
                        case "relation":
                            currentRelation = new OSMRelation();
                            break;
                        case "member":
                            var relationType = reader.getAttributeValue(null, "type");
                            ndref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            if (relationType.equals("way")) {
                                OSMWay relationWay = idToWay.get(ndref);
                                if (relationWay != null) {
                                    currentRelation.add(relationWay);
                                }
                            }
                    }

                case END_ELEMENT:
                    tagname = reader.getLocalName();
                    switch (tagname) {
                        case "way":
                            if (type == Type.HIGHWAY || type == Type.TERTIARYWAY || type == Type.MOTORWAY || type == Type.PRIMARYWAY) {

                                currentWay.setType(type);
                                Highway highway = new Highway(currentWay);

                                if (osmWayAddress.getStreet() != null) {
                                    // Set address to current road
                                    highway.setStreet(osmWayAddress.getStreet());

                                }

                                graph.insert(highway); // Inserts in graph for Dijkstra
                                graph.addEdges(highway, highwayDecoder.isOneWay(highwayValues), highwayDecoder.isRoundabout(highwayValues), highwayDecoder.getMaxSpeed(highwayType, highwayValues), highwayDecoder.isDrivable(highwayType, highwayValues), highwayDecoder.isBikable(highwayType, highwayValues), highwayDecoder.isWalkable(highwayType, highwayValues));

                                if (type == Type.TERTIARYWAY) {
                                    tertiarywayTree.insert(highway);
                                } else if (type == Type.PRIMARYWAY || type == Type.MOTORWAY) {
                                    primarywayTree.insert(highway);
                                } else if (type == Type.HIGHWAY) {
                                    highwayTree.insert(highway); // Inserts to list for KDTree
                                }
                                break;
                            } else if (type != Type.COASTLINE) {

                                if (currentWay.size() != 0) {

                                    currentWay.setType(type);

                                    switch (type) {
                                        case PARK:
                                            parkTree.insert(currentWay);
                                            break;
                                        case RESIDENTIAL:
                                            areaTree.insert(currentWay);
                                            break;
                                        case HEATH:
                                            heathTree.insert(currentWay);
                                            break;
                                        case MEADOW:
                                            meadowTree.insert(currentWay);
                                            break;
                                        case FOREST:
                                            forestTree.insert(currentWay);
                                            break;
                                        case FARM:
                                            farmTree.insert(currentWay);
                                            break;
                                        case WATERWAY:
                                            waterwayTree.insert(currentWay);
                                            break;
                                        case WATER:
                                            waterTree.insert(currentWay);
                                            break;
                                        case BUILDING:
                                            buildingTree.insert(currentWay);
                                            break;
                                        case RAILWAY:
                                            railwayTree.insert(currentWay);
                                    }
                                }
                            } else {

                                var before = nodeToCoastline.remove(currentWay.first());
                                if (before != null) {
                                    nodeToCoastline.remove(before.first());
                                    nodeToCoastline.remove(before.last());
                                }

                                var after = nodeToCoastline.remove(currentWay.last());
                                if (after != null) {
                                    nodeToCoastline.remove(after.first());
                                    nodeToCoastline.remove(after.last());
                                }

                                // first = first nd tag in the way node
                                // last = last nd tag of the way node
                                currentWay = OSMWay.merge(OSMWay.merge(before, currentWay), after);
                                nodeToCoastline.put(currentWay.first(), currentWay);
                                nodeToCoastline.put(currentWay.last(), currentWay);
                            }
                            highwayType = null; // Resets string
                            highwayValues.clear(); // Empties previous map
                            type = Type.UNKNOWN; // resets type
                            break;
                        case "node":
                            if (isCurrentPointOfInterest) {
                                MapIcon mapIcon = new MapIcon(currentNode.getLon(), currentNode.getLat(), type);
                                mapIconTree.insert(mapIcon);
                                isCurrentPointOfInterest = false;
                            }
                            break;
                        case "relation":
                            if (currentRelation != null && currentRelation.size() != 0) {
                                if (type == Type.BUILDING) {
                                    buildingTree.insert(new Relations(currentRelation, type));
                                }
                                if (type == Type.WATER) {
                                    waterTree.insert(new Relations(currentRelation, type));
                                }
                            }
                        type = Type.UNKNOWN; // resets type
                    }
            }
        }

        for (var entry : nodeToCoastline.entrySet()) {
            if (entry.getKey() == entry.getValue().last()) {
                islands.add(new LinePath(entry.getValue(), type));
            }
        }

        // Sorts OSMaddresser at program startup to allow binarysearch of the list when using the AddressParser
        OSMAddresses.sortByAddress();
        OSMCities.sortByAddress();


        // Clear all fields
        currentNode = null;
        currentWay = null;
        highwayValues = null;
        highwayType = null;
        nodeForHighwayID = null;
        nodeToCoastline = null;
        refAddress = null;
        refCityAddress = null;
        city = null;
        type = null;
        osmWayAddress = null;
        currentElementType = null;

        System.gc();
    }


    /**
     * Setter for the SortedAddressArrayList of addresses
     * @param addresses SortedAddressArrayList
     */
    public void setOSMAddresses(SortedAddressArrayList addresses) {
        OSMAddresses = addresses;
    }

    /**
     * Getter for the SortedAddressArrayList of addresses
     * @return SortedAddressArrayList
     */
    public SortedAddressArrayList getOSMAddresses() {
        return OSMAddresses;
    }

    /**
     * Getter for List of drawables containing islands
     * @return List of Drawable
     */
    public List<Drawable> getIslands() {
        return islands;
    }

    /**
     * Getter for the SortedAddressArrayList of cities
     * @return SortedAddressArrayList
     */
    public SortedAddressArrayList getOSMCities() {
        return OSMCities;
    }

    /**
     * Getter for the KDTree containing City names
     * @return KDTree
     */
    public KDTree getCityNamesTree() {
        return cityNamesTree;
    }

    /**
     * Getter for the KDTree containing Village names
     * @return KDTree
     */
    public KDTree getVillageNamesTree() {
        return villageNamesTree;
    }

    /**
     * Getter for the KDTree containing Highways
     * @return KDTree
     */
    public KDTree getHighwayTree() {
        return highwayTree;
    }

    /**
     * Getter for the KDTree containing Tertiaryways
     * @return KDTree
     */
    public KDTree getTertiarywayTree() {
        return tertiarywayTree;
    }

    /**
     * Getter for the KDTree containing Primaryways
     * @return KDTree
     */
    public KDTree getPrimarywayTree() {
        return primarywayTree;
    }

    /**
     * Getter for the KDTree containing Areas
     * @return KDTree
     */
    public KDTree getAreaTree() {
        return areaTree;
    }

    /**
     * Getter for the KDTree containing Water
     * @return KDTree
     */
    public KDTree getWaterTree() {
        return waterTree;
    }

    /**
     * Getter for the KDTree containing Buildings
     * @return KDTree
     */
    public KDTree getBuildingTree() {
        return buildingTree;
    }

    /**
     * Getter for the KDTree containing MapIcons
     * @return KDTree
     */
    public KDTree getMapIconTree() {
        return mapIconTree;
    }

    /**
     * Getter for the KDTree containing Heaths
     * @return KDTree
     */
    public KDTree getHeathTree() {
        return heathTree;
    }

    /**
     * Getter for the KDTree containing Meadows
     * @return KDTree
     */
    public KDTree getMeadowTree() {
        return meadowTree;
    }

    /**
     * Getter for the KDTree containing Parks
     * @return KDTree
     */
    public KDTree getParkTree() {
        return parkTree;
    }

    /**
     * Getter for the KDTree containing Forests
     * @return KDTree
     */
    public KDTree getForestTree() {
        return forestTree;
    }

    /**
     * Getter for the KDTree containing Farms
     * @return KDTree
     */
    public KDTree getFarmTree() {
        return farmTree;
    }

    /**
     * Getter for the KDTree containing Waterways
     * @return KDTree
     */
    public KDTree getWaterwayTree() {
        return waterwayTree;
    }

    /**
     * Getter for the KDTree containing Railways
     * @return KDTree
     */
    public KDTree getRailwayTree() {
        return railwayTree;
    }

    /**
     * Getter for the constructed graph
     * @return Graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Returns the minimum latitude bound of the map
     * @return float represented as latitude
     */
    public float getMinLat() {
        return minLat;
    }

    /**
     * Returns the minimum longitude bound of the map
     * @return float represented as longitude
     */
    public float getMinLon() {
        return minLon;
    }

    /**
     * Returns the maximum latitude bound of the map
     * @return float represented as latitude
     */
    public float getMaxLat() {
        return maxLat;
    }

    /**
     * Returns the maximum longitude bound of the map
     * @return float represented as latitude
     */
    public float getMaxLon() {
        return maxLon;
    }

    /**
     * Sets the graph
     * @param g Graph
     */
    public void setGraph(Graph g) {
        graph = g;
    }

    /**
     * Sets the minimum latitude bound of the map
     * @param lat float
     */
    public void setMinLat(float lat) {
        minLat = lat;
    }

    /**
     * Sets the maximum latitude bound of the map
     * @param lat float
     */
    public void setMaxLat(float lat) {
        maxLat = lat;
    }

    /**
     * Sets the minimum longitude bound of the map
     * @param lon float
     */
    public void setMinLon(float lon) {
        minLon = lon;
    }

    /**
     * Sets the maximum longitude bound of the map
     * @param lon float
     */
    public void setMaxLon(float lon) {
        maxLon = lon;
    }

    /**
     * Sets the SortedAddressArrayList containing Cities
     * @param OSMCities SortedAddressArrayList
     */
    public void setOSMCities(SortedAddressArrayList OSMCities) {
        this.OSMCities = OSMCities;
    }

    /**
     * Sets all KDTrees
     * Order of sequence is important and is determined when saving binary file!!!
     * @param KDTrees List of KDTree
     */
    public void setKDTrees(List<KDTree> KDTrees) {
        highwayTree = KDTrees.get(0);
        areaTree = KDTrees.get(1);
        waterTree = KDTrees.get(2);
        buildingTree = KDTrees.get(3);
        mapIconTree = KDTrees.get(4);
        tertiarywayTree = KDTrees.get(5);
        primarywayTree = KDTrees.get(6);
        heathTree = KDTrees.get(7);
        meadowTree = KDTrees.get(8);
        forestTree = KDTrees.get(9);
        farmTree = KDTrees.get(10);
        waterwayTree = KDTrees.get(11);
        cityNamesTree = KDTrees.get(12);
        villageNamesTree = KDTrees.get(13);
        parkTree = KDTrees.get(14);
        railwayTree = KDTrees.get(15);
    }

    /**
     * Sets the list of drawable containing islands
     * @param islands List of drawable
     */
    public void setIslands(List<Drawable> islands) {
        this.islands = islands;
    }
}
