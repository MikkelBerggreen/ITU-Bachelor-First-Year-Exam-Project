package Model;

import Model.MapComponents.*;
import Model.Pathfinding.Graph;
import Model.Pathfinding.Path;
import Model.Tree.KDTree;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Main model class of the MVC design pattern
 * Model is implemented with the singleton design pattern
 */
public class Model implements Serializable {
    OSMHandler OSMHandler = new OSMHandler();
    private static Model model;
    private String initFile = "denmark2.bin";
    private boolean hasBeenLoaded = false;

    private boolean drawDijkstraIllustration;
    private boolean drawKDTreeIllustration;
    private int colorScheme = 0; // default: 0 - Google Maps: 1 - Dark theme: 2 (original name Aubergine)

    private List<Runnable> observers = new ArrayList<>();
    private List<PointOfInterest> pointsOfInterest = new ArrayList<>();
    // Saves direction & path
    private Path path;
    private OSMWay route = new OSMWay();
    private PointOfInterest[] routePOI = new PointOfInterest[2];

    private Model() {}


    /**
     * Singleton design pattern.
     * Creates new instance of Model if Model has not been initialized, otherwise returns the already instantiated Model
     * @return Model
     */
    public static Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }



    /**
     * Loads the defined initial file as defined in the String field initFile
     * @throws XMLStreamException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public void loadInitFile() throws XMLStreamException, IOException, ClassNotFoundException, URISyntaxException {
        if (initFile.endsWith(".bin")) {
            // Ensures .bin files can be loaded inside jar
            InputStream res = getClass().getClassLoader().getResourceAsStream(initFile);
            BinHandler.load(res);
        } else {
            // Any other files than .bin as only .bin are supposed to be embedded in the jar
            URL res = getClass().getClassLoader().getResource(initFile);
            File file = null;
            if (res != null) {
                file = Paths.get(res.toURI()).toFile();
            }
            if (file != null) {
                load(file);
            }
        }
    }


    /**
     * Adds observer to list of runnables
     * @param observer
     */
    public void addObserver(Runnable observer) {
        observers.add(observer);
    }


    /**
     * calls the run function on all Runnables in list of observers
     */
    public void notifyObservers() {
        for (var observer : observers) {
            observer.run();
        }
    }

    // Resets everything in model
    private void reset() {
        pointsOfInterest.clear();
        path = null;
        route = new OSMWay();
        routePOI = new PointOfInterest[2];
    }


    /**
     * Responsible for handling the loading of the given file depending on the type of file
     * @param file File of type .bin .osm or .zip
     * @throws IOException
     * @throws XMLStreamException
     * @throws ClassNotFoundException
     */
    public void load(File file) throws IOException, XMLStreamException, ClassNotFoundException {
        long time = -System.nanoTime();


        String filename = file.getName();
        String fileExt = filename.substring(filename.lastIndexOf("."));

        if (fileExt.equals(".bin")) {
            reset();
            loadBin(file);
            hasBeenLoaded = true;
        } else if (fileExt.equals(".osm")) {
            reset();
            loadOSM(file);
            hasBeenLoaded = true;
        } else if (fileExt.equals(".zip")) {
            reset();
            loadZIP(file);
            hasBeenLoaded = true;
        } else {
            throw new IllegalArgumentException("Invalid type of file");
        }


        time += System.nanoTime();
        System.out.printf("Load time: %.3fms\n", time / 1e6);
        notifyObservers();

        System.gc();
    }


    private void loadBin(File file) throws IOException, ClassNotFoundException {
        URL fileURL = file.toURI().toURL();
        InputStream inputStream = fileURL.openStream();
        BinHandler.load(inputStream);
    }

    private void loadOSM(File file) throws IOException, XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file, StandardCharsets.UTF_8));
        OSMHandler.loadOSM(reader);
    }


    private void loadZIP(File file) throws IOException, XMLStreamException {
        var zipFile = new ZipFile(file);
        var iterator = zipFile.entries().asIterator();

        while (iterator.hasNext()) {
            var zipEntry = iterator.next();
            if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".osm")) {
                var stream = zipFile.getInputStream(zipEntry);
                var reader = XMLInputFactory.newFactory().createXMLStreamReader(stream);
                OSMHandler.loadOSM(reader);
            }
        }
    }


    /**
     * Responsible for handling the saving to .bin file
     * @param file File
     * @throws IllegalArgumentException
     */
    public void save(File file) throws IllegalArgumentException {
        long time = -System.nanoTime();

        if (file.getName().endsWith(".bin")) {
            BinHandler.save(file + "");
        } else {
            throw new IllegalArgumentException("Invalid type of file");
        }

        time += System.nanoTime();
        System.out.printf("Save time: %.3fms\n", time / 1e6);
    }


    /**
     * Computes the shortest path or the quickest path between the given source and destination depending on the mode of transportation
     * @param source OSMNode
     * @param destination OSMNode
     * @param transportationMode int representing the mode of transportation: 0 = car, 1 = bike, 2 = walk
     */
    public void computePath(OSMNode source, OSMNode destination, int transportationMode) {
        path = new Path(OSMHandler.getGraph(), OSMHandler.getGraph().getIndexFromNode(source), OSMHandler.getGraph().getIndexFromNode(destination), transportationMode);
        List<OSMNode> p = path.getPath();
        route = new OSMWay();
        for (OSMNode node : p) {
            route.add(node);
        }
    }

    /**
     * Returns the current applied color scheme
     * @return int representing the color scheme: 0 = default theme, 1 = google maps theme, 2 = dark theme
     */
    public int getColorScheme() {
        return colorScheme;
    }

    /**
     * Sets the color scheme to apply
     * @param i int representing the color scheme: 0 = default theme, 1 = google maps theme, 2 = dark theme
     */
    public void setColorScheme(int i){
        colorScheme = i;
    }

    /**
     * Gets the minimum latitude point of the loaded map
     * @return float representing the minimum latitude point of the loaded map
     */
    public float getMinLat() {
        return OSMHandler.getMinLat();
    }

    /**
     * Gets the minimum longitude point of the loaded map
     * @return float representing the minimum longitude point of the loaded map
     */
    public float getMinLon() {
        return OSMHandler.getMinLon();
    }

    /**
     * Gets the maximum latitude point of the loaded map
     * @return float representing the maximum latitude point of the loaded map
     */
    public float getMaxLat() {
        return OSMHandler.getMaxLat();
    }

    /**
     * Gets the maximum longitude point of the loaded map
     * @return float representing the maximum longitude point of the loaded map
     */
    public float getMaxLon() {
        return OSMHandler.getMaxLon();
    }

    /**
     * Returns a SortedAddressArrayList of all Addresses determined from the loading of an .osm file
     * @return SortedAddressArrayList with addresses
     */
    public SortedAddressArrayList getOSMAddresses() {
        return OSMHandler.getOSMAddresses();
    }

    /**
     * Returns the graph constructed during the load of .osm file
     * @return Graph
     */
    public Graph getGraph() {
        return OSMHandler.getGraph();
    }

    /**
     * Returns a SortedAddressArrayList of all Cities determined from the loading of an .osm file
     * @return SortedAddressArrayList with cities
     */
    public SortedAddressArrayList getOSMCities() {
        return OSMHandler.getOSMCities();
    }

    /**
     * Getter for the KDTree containing City names
     * @return KDTree
     */
    public KDTree getCityNamesTree() {
        return OSMHandler.getCityNamesTree();
    }

    /**
     * Getter for the KDTree containing Village names
     * @return KDTree
     */
    public KDTree getVillageNamesTree() {
        return OSMHandler.getVillageNamesTree();
    }

    /**
     * Getter for the KDTree containing Highways
     * @return KDTree
     */
    public KDTree getHighwayTree() {
        return OSMHandler.getHighwayTree();
    }

    /**
     * Getter for the KDTree containing Tertiaryways
     * @return KDTree
     */
    public KDTree getTertiarywayTree() {
        return OSMHandler.getTertiarywayTree();
    }

    /**
     * Getter for the KDTree containing Primaryways
     * @return KDTree
     */
    public KDTree getPrimarywayTree() {
        return OSMHandler.getPrimarywayTree();
    }

    /**
     * Getter for the KDTree containing Areas
     * @return KDTree
     */
    public KDTree getAreaTree() {
        return OSMHandler.getAreaTree();
    }

    /**
     * Getter for the KDTree containing Water
     * @return KDTree
     */
    public KDTree getWaterTree() {
        return OSMHandler.getWaterTree();
    }

    /**
     * Getter for the KDTree containing Buildings
     * @return KDTree
     */
    public KDTree getBuildingTree() {
        return OSMHandler.getBuildingTree();
    }

    /**
     * Getter for the KDTree containing MapIcons
     * @return KDTree
     */
    public KDTree getMapIconTree() {
        return OSMHandler.getMapIconTree();
    }

    /**
     * Getter for the KDTree containing Heaths
     * @return KDTree
     */
    public KDTree getHeathTree() {
        return OSMHandler.getHeathTree();
    }

    /**
     * Getter for the KDTree containing Meadows
     * @return KDTree
     */
    public KDTree getMeadowTree() {
        return OSMHandler.getMeadowTree();
    }

    /**
     * Getter for the KDTree containing Forests
     * @return KDTree
     */
    public KDTree getForestTree() {
        return OSMHandler.getForestTree();
    }

    /**
     * Getter for the KDTree containing Farms
     * @return KDTree
     */
    public KDTree getFarmTree() {
        return OSMHandler.getFarmTree();
    }

    /**
     * Getter for the KDTree containing Waterway
     * @return KDTree
     */
    public KDTree getWaterwayTree() {
        return OSMHandler.getWaterwayTree();
    }

    /**
     * Getter for the KDTree containing Railways
     * @return KDTree
     */
    public KDTree getRailwayTree() {
        return OSMHandler.getRailwayTree();
    }

    /**
     * Getter for the KDTree containing parks
     * @return KDTree
     */
    public KDTree getParkTree() {
        return OSMHandler.getParkTree();
    }

    /**
     * Returns boolean for whether dijkstra illustrations are supposed to be shown
     * @return boolean true if should illustrate dijkstra, false if should not
     */
    public boolean shouldDrawDijkstraIllustration() {
        return drawDijkstraIllustration;
    }

    /**
     * Sets the current state of whether the dijkstra should be illustrated
     * @param drawDijkstra boolean
     */
    public void setDrawDijkstraIllustration(boolean drawDijkstra) {
        drawDijkstraIllustration = drawDijkstra;
    }

    /**
     * Returns boolean for whether KDTree illustration are supposed to be shown
     * @return boolean true if should illustrate KDTree, false if not
     */
    public boolean shouldDrawKDTreeIllustration() {
        return drawKDTreeIllustration;
    }

    /**
     * Sets the current state of whether the KDTree should be illustrated
     * @param drawKDTree boolean
     */
    public void setDrawKDTreeIllustration(boolean drawKDTree) {
        this.drawKDTreeIllustration = drawKDTree;
    }


    /**
     * Returns result from nearest neighbor search
     * @param x float value representing the lon/x coordinate to compute nearest neighbor search at
     * @param y float value representing the lat/y coordinate to compute nearest neighbor search at
     * @return Highway
     */
    public Highway findNearestNeighbor(float x, float y) {
        return OSMHandler.getHighwayTree().nearestNeighbor(x, y);
    }

    /**
     * Returns the computed Path
     * @return Path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns a list of Drawable containing islands
     * @return List of drawable of Type island
     */
    public List<Drawable> getIslands() {
        return OSMHandler.getIslands();
    }


    /**
     * Returns the computed path given as an OSMWay
     * @return
     */
    public OSMWay getRoute() {
        return route;
    }

    /**
     * Clears the computed route / path
     */
    public void clearRoute() {
        path = null;
        route.clear();
        System.gc();
    }

    /**
     * Getter for the OSMHandler
     * @return OSMHandler
     */
    public OSMHandler getOSMHandler() {
        return OSMHandler;
    }

    /**
     * Adds a POI to the list of POI's
     * @param point PointOfInterest
     */
    public void addPointOfInterest(PointOfInterest point){
        pointsOfInterest.add(point);
    }

    /**
     * Getter for the list of Point of interests
     * @return List containing Point of Interests
     */
    public List<PointOfInterest> getPointsOfInterest(){
        return pointsOfInterest;
    }

    /**
     * Sets the POI List
     * @param pointOfInterests List containing PointOfInterests
     */
    public void setPointsOfInterest(List<PointOfInterest> pointOfInterests) {
        this.pointsOfInterest = pointOfInterests;
    }


    /**
     * Deletes specified point of interest
     * @param point PointOfInterest
     */
    public void deletePointOfInterest(PointOfInterest point){
        pointsOfInterest.remove(point);
    }

    /**
     * Gets the POI used for Source and destination of the computed path
     * @return PointOfInterest[]
     */
    public PointOfInterest[] getRoutePOI() {
        return routePOI;
    }

    /**
     * Sets the POI used for source and destination of the computed path
     * @param source PointOfInterest
     * @param destination PointOfInterest
     */
    public void setRoutePOI(PointOfInterest source, PointOfInterest destination) {
        routePOI[0] = source;
        routePOI[1] = destination;
    }


    /**
     * Getter for boolean representing if the initial load has occurred
     * @return boolean representing if the initial load has occurred
     */
    public boolean getHasBeenLoaded() {
        return hasBeenLoaded;
    }

    /**
     * Sets the initial file to be loaded
     * @param initFile String name of file
     */
    public void setInitFile(String initFile) {
        this.initFile = initFile;
    }
}