package View;

import Model.AddressParser.Address;

import Model.MapComponents.*;
import Model.Type;
import Model.LinePath;
import Model.OSMWay;
import Model.OSMNode;
import Model.Model;
import Model.Drawable;
import Model.Tree.KDTree;
import Model.MapData;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.Stage;

/**
 * Main View class for the MVC design pattern.
 * Responsible for MapCanvas in FXML
 */
public class MapCanvas extends Canvas {
    public Stage newStage;
    private Model model;

    // Responsible for storing graphical content on canvas
    private GraphicsContext gc;

    // Responsible for all mathematical mapping for canvas
    private Affine trans;
    private Viewport viewport;
    private Address searchedAddress;


    /**
     * Initializes the Canvas
     * @param mainAnchorPane AnchorPane
     */
    public void initialize(AnchorPane mainAnchorPane) {
        model = Model.getInstance();
        this.gc = getGraphicsContext2D();
        this.trans = new Affine();
        viewport = new Viewport();
        viewport.update(this, trans);

        // Adds repaint function to observer
        model.addObserver(this::repaint);

        resetView();


        viewport.update(this, trans);


        // Sets view's width and height to size of Scene
        widthProperty().bind(mainAnchorPane.widthProperty());
        heightProperty().bind(mainAnchorPane.heightProperty());

        // Calls repaint whenever window is being resized
        widthProperty().addListener((a, b, c) -> {
            repaint();
        });
        heightProperty().addListener((a, b, c) -> {
            repaint();
        });

    }

    /**
     * Sets the initial rectangle view accordingly to the bounds from model
     */
    public void resetView() {
        trans = new Affine();

        double canvasRatio = getWidth() / getHeight();

        double mapRatio = Math.abs(model.getMaxLon() - model.getMinLon()) / Math.abs(model.getMaxLat() - model.getMinLat());

        double zoomFactor;
        if (canvasRatio > mapRatio) {
            zoomFactor = getHeight();
        } else {
            zoomFactor = getWidth();
        }

        trans.prependTranslation(-model.getMinLon(), -model.getMinLat());
        zoom(zoomFactor / (model.getMaxLat() - model.getMinLat()), 0, 0);
        repaint();
    }


    /**
     * Changes the view by panning and zooming
     * @param avgLon float
     * @param avgLat float
     * @param zoomLevel double
     */
    public void changeView(float avgLon, float avgLat, double zoomLevel) {
        double currentZoom = trans.getMyy();

        double halfWidth = getWidth() * 0.5;
        double halfHeight = getHeight() * 0.5;

        double x = trans.getTx() - halfWidth;
        double y = trans.getTy() - halfHeight;


        double mapAddressX = (avgLon * currentZoom) + x;
        double mapAddressY = (avgLat * currentZoom) + y;

        pan(-mapAddressX, -mapAddressY);
        zoom(zoomLevel / currentZoom, halfWidth, halfHeight);
        repaint();
    }


    /**
     * Draws a circle around the given address to show the address
     * @param address Address
     * @param zoomLevel double
     */
    public void showAddressOnMap(Address address, double zoomLevel) {
        double currentZoom = getZoom();

        double halfWidth = getWidth() * 0.5;
        double halfHeight = getHeight() * 0.5;

        double x = trans.getTx() - halfWidth;
        double y = trans.getTy() - halfHeight;

        double mapAddressX = (address.getNode().getLon() * currentZoom) + x;
        double mapAddressY = (address.getNode().getLat() * currentZoom) + y;

        pan(-mapAddressX, -mapAddressY);
        zoom(zoomLevel / currentZoom, halfWidth, halfHeight);
        searchedAddress = address;
        drawDot(address.getNode().getLon(), address.getNode().getLat());
        repaint();
    }


    /**
     * responsible for painting (& repainting) everything on canvas
     */
    public void repaint() {

        // setTransform defines the current viewport
        gc.setTransform(new Affine());

        // Background color - everything that is outside of drawn element becomes blue (oceans)
        gc.setStroke(Type.getColor(Type.BACKGROUND));
        gc.setFill(Type.getColor(Type.BACKGROUND));

        // Fills in background
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Updates to wanted viewport
        gc.setTransform(trans);

        viewport.update(this, trans);

        gc.setFillRule(FillRule.EVEN_ODD);

        // Paints list of drawable containing islands
        paintIslands(Type.ISLAND);

        // responsible for painting all kdtrees
        paintKDTrees();

        // Draws the searched edges/osmways for dijkstra if enabled
        if (model.shouldDrawDijkstraIllustration()) {
            paintIllustratedDijkstra();
        }

        // Paints the computed route
        paintComputedPath(Type.ROUTE);

        paintPointOfInterests();

        if (searchedAddress != null) {
            drawDot(searchedAddress.getNode().getLon(), searchedAddress.getNode().getLat());
        }

        paintRoutePins();
    }



    private void paintPointOfInterests() {
        for (PointOfInterest p : model.getPointsOfInterest()) {
            p.draw(gc, 50/getZoom(), 50/getZoom());
        }
    }

    /**
     * paints the POI pins used for Source and destination of the computed path
     */
    public void paintRoutePins() {
        if (model.getRoutePOI()[0] == null || model.getRoutePOI()[1] == null) {
            return;
        }
        for (PointOfInterest poi : model.getRoutePOI()) {
            poi.draw(gc, 50/getZoom(), 50/getZoom());
        }
    }


    /**
     * Order of painting is important!
     * current order: Area > Heath > Farm > meadow > Forest > Park > Water > Waterway > buildings > Railway > highways
     * > tertiaryways > Tertiary street names > primaryways > mapicons > highway street names > City names > Village names
     */
    private void paintKDTrees() {
        // Draws world in order - sequence below is important to not draw over other elements

        if (calculateDrawLevel() <= 7) {
            paintKDTree(model.getAreaTree());
        }

        if (calculateDrawLevel() <= 3) {
            paintKDTree(model.getHeathTree());
            paintKDTree(model.getFarmTree());
            paintKDTree(model.getMeadowTree());
            paintKDTree(model.getForestTree());
        }

        if (calculateDrawLevel() <= 5) {
            paintKDTree(model.getParkTree());
            paintKDTree(model.getWaterTree());
            paintKDTree(model.getWaterwayTree());
        }

        if (calculateDrawLevel() <= 0) {
            paintKDTree(model.getBuildingTree());
        }

        // Used for drawing highway names as well instead of rect searching multiple times
        List<MapData> highways = null;
        if (calculateDrawLevel() <= 2) {
            paintKDTree(model.getRailwayTree());
            float[] rect = viewport.getRect();
            highways = model.getHighwayTree().rectSearch(rect[0], rect[1], rect[2], rect[3]);
            paintMapDataList(highways);
        }

        List<MapData> tertiary = null;
        if (calculateDrawLevel() <= 7) {
            float[] rect = viewport.getRect();
            tertiary = model.getTertiarywayTree().rectSearch(rect[0], rect[1], rect[2], rect[3]);
            paintMapDataList(tertiary);
        }

        if (calculateDrawLevel() <= 2) {

            drawStreetNames(tertiary);
        }

        if (calculateDrawLevel() <= 10) {
            paintKDTree(model.getPrimarywayTree());
        }

        if (calculateDrawLevel() <= -1) {
            paintKDTree(model.getMapIconTree());
        }

        if (calculateDrawLevel() <= 1) {
            drawStreetNames(highways);
        }

        if ((calculateDrawLevel() > 2) && (calculateDrawLevel() <= 9)) {
            paintCityNames(model.getCityNamesTree());
        }

        if ((calculateDrawLevel() >= 2) && (calculateDrawLevel() <= 4)) {
            paintCityNames(model.getVillageNamesTree());
        }

        if (model.shouldDrawKDTreeIllustration()) {
            paintKDIllustrationBox();
        }
    }



    private int calculateDrawLevel() {
        double zoom = trans.getMyy();
        if (zoom > 300000) {
            return -1;
        }
        if (zoom > 190000) {
            return 0;
        }
        if (zoom > 130000) {
            return 1;
        }
        if (zoom > 30000) {
            return 2;
        }
        if (zoom > 12000) {
            return 3;
        }
        if (zoom > 7700) {
            return 4;
        }
        if (zoom > 5700) {
            return 5;
        }
        if (zoom > 900) {
            return 7;
        }
        if (zoom > 600) {
            return 9;
        }
        if (zoom > 500) {
            return 10;
        }
        return 10;
    }


    // Fetches the KDTree data and calls paintMapDataList
    private void paintKDTree(KDTree tree) {
        float[] rect = viewport.getRect();
        List<MapData> ways = tree.rectSearch(rect[0], rect[1], rect[2], rect[3]);

        paintMapDataList(ways);
    }


    // Converts the list of Mapdata into a list of linepaths(drawables) and calls the paintDrawables method
    private void paintMapDataList(List<MapData> mapData) {

        List<Drawable> drawWays = new ArrayList<>();

        for (MapData way : mapData) {
            if (way.getClassType() == MapIcon.class) {
                MapIcon icon = (MapIcon) way;
                drawWays.add(icon);
                continue;
            }

            Type type = way.getType();

            LinePath path;
            if (way.getClassType() == Highway.class) {
                path = new LinePath(((Highway) way).getOSMWay(), type);

            } else if (way.getClassType() == Relations.class) {
                drawWays.add((Relations) way);
                path = null;
            } else {
                path = new LinePath((OSMWay) way, type);
            }

            if (path != null) {
                drawWays.add(path);
            }
        }
        paintDrawables(drawWays);
    }


    // Main method responsible for drawing a list of drawables
    private void paintDrawables(List<Drawable> drawables) {
        Type lastType = null;
        boolean fill = false;

        for (Drawable drawable : drawables) {
            Type type = drawable.getType();

            if (type != lastType) {
                fill = Type.getFill(type);
                double lineWidth = Type.getLineWidth(type) / getZoom();

                double scaledLineWidth = lineWidth;

                Color color = Type.getColor(type);

                if (type == Type.HIGHWAY || type == Type.TERTIARYWAY || type == Type.PRIMARYWAY || type == Type.MOTORWAY) {
                    // Scales line width according to kilometers on screen instead of zoom level
                    scaledLineWidth = lineWidth / ((getWidth() / getZoom()) * 110.574); // 110.574 is km/lat

                    if (type == Type.PRIMARYWAY || type == Type.MOTORWAY) {
                        if (scaledLineWidth < lineWidth / 4) {
                            scaledLineWidth = lineWidth / 4;
                        }
                    }
                }

                gc.setLineWidth(scaledLineWidth);
                gc.setStroke(color);
                gc.setFill(color);
                lastType = type;
            }

            // Draw
            drawable.draw(gc, getZoom());
            if (fill) {
                gc.fill();
            }
        }
    }


    // Draws street names
    private void drawStreetNames(List<MapData> ways) {

        if (ways == null || ways.size() == 0) {
            return;
        }

        gc.setStroke(Type.getColor(Type.STREETNAME));

        for (MapData way : ways) {

            Highway highway = (Highway)way;

            if (highway.getStreet().isEmpty() || highway.getOSMWay().size() < 2) {
                continue;
            }



            double fontSize = Type.getFontSize(highway.getType()) / getZoom();


            double calculatedFontSize = fontSize / ((getWidth() / getZoom()) * 110.574 * 1.6);

            if (calculatedFontSize < fontSize) {
                calculatedFontSize = fontSize;
            }

            // Stored as meters on map
            double nameLength = highway.getStreet().length() * calculatedFontSize * 0.5;

            double highwayLength = Math.sqrt(Math.pow(highway.getMaxX() - highway.getMinX(), 2) + Math.pow(highway.getMaxY() - highway.getMinY(), 2));
            if (highwayLength < nameLength) {
                continue;
            }


            gc.setFont(Font.font("Verdana", FontWeight.EXTRA_LIGHT, calculatedFontSize));
            gc.setFill(Type.getColor(Type.STREETNAME));


            int size = highway.getOSMWay().size();
            OSMNode lowerMid = highway.getOSMWay().get(size / 2 - 1);
            OSMNode higherMid = highway.getOSMWay().get(size / 2);

            double angle = Math.toDegrees(Math.atan2(higherMid.getLat() - lowerMid.getLat(), higherMid.getLon() - lowerMid.getLon()));

            if (angle < -90) {
                angle += 180;
            }

            if (angle > 90) {
                angle -= 180;
            }


            double averageLon = (lowerMid.getLon() + higherMid.getLon())/2;
            double averageLat = (lowerMid.getLat() + higherMid.getLat())/2;

            gc.save();

            gc.translate(averageLon, averageLat);
            gc.rotate(angle);

            gc.fillText(highway.getStreet(), -0.8 * nameLength, 0);

            gc.restore();
        }
    }


    // Draws islands
    private void paintIslands(Type type) {
        gc.setLineWidth(1 / getZoom()); // Defines pixelwidth
        gc.setFill(Type.getColor(type)); // Changes color so islands are drawn in color lightgreen
        for (Drawable island : model.getIslands()) {
            island.draw(gc, getZoom());
            gc.fill();
        }
    }


    // Draws city names
    private void paintCityNames(KDTree tree) {
        float[] rect = viewport.getRect();

        List<MapData> ways = tree.rectSearch(rect[0], rect[1], rect[2], rect[3]);

        for (MapData city : ways) {
            City currentCity = (City) city;
            String cityName = currentCity.getCity();

            gc.setFill(Type.getColor(Type.CITYNAME));
            gc.setFont(new Font(12 / getZoom()));
            gc.fillText(cityName, currentCity.getNode().getLon(), currentCity.getNode().getLat());
        }
    }

    // Draws circle
    private void drawDot(double x, double y) {
        gc.setStroke(Color.RED);
        gc.setFill(Color.ORANGERED);
        gc.setLineWidth(4 / getZoom());
        // Width and height of circle is DIAMETER, while x and y are the coordinates minus half the diameter (RADIUS).
        double size = 12 / getZoom();
        gc.setGlobalAlpha(0.8);
        gc.strokeOval(x - size, y - size, size * 2, size * 2);
        gc.setGlobalAlpha(0.4);
        gc.fillOval(x - size, y - size, size * 2, size * 2);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Creates a rect from 4 points
     * @param x1 float
     * @param y1 float
     * @param x2 float
     * @param y2 float
     * @return returns an array of type float containing the points [x1, y1, x2, y1, x2, y2, x1, y2, x1, y1]
     */
    public static float[] createRect(float x1, float y1, float x2, float y2) {
        float[] coords = new float[10];
        coords[0] = x1;
        coords[1] = y1;

        coords[2] = x2;
        coords[3] = y1;

        coords[4] = x2;
        coords[5] = y2;

        coords[6] = x1;
        coords[7] = y2;

        coords[8] = x1;
        coords[9] = y1;

        return coords;
    }


    /**
     * Zooms by updating the affine and calling repaint
     * Capped at a certain zoom level both inwards and outwards
     * @param factor double
     * @param x double
     * @param y double
     */
    public void zoom(double factor, double x, double y) {
        // Caps zoom level on both ends
        if (getZoom() * factor > 400000 || getZoom() * factor < 100) {
            return;
        }
        trans.prependScale(factor, factor, x, y);
        repaint();
    }

    /**
     * Changes the viewport coordinates
     * @param dx double
     * @param dy double
     */
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }


    // Draws the computed path
    private void paintComputedPath(Type type) {
        if (model.getRoute().size() > 0) {
            List<Drawable> path = new ArrayList<>();
            LinePath p = new LinePath(model.getRoute(), type);
            path.add(p);
            paintDrawables(path);
        }
    }


    // paints all highways red that the dijkstra algorithm has visited
    private void paintIllustratedDijkstra() {
        if (model.getPath() != null && model.getPath().getEdgesGoneThrough() != null) {
            List<Drawable> marked = new ArrayList<>();

            for (OSMWay way : model.getPath().getEdgesGoneThrough()) {
                LinePath p = new LinePath(way, Type.ILLUSTRATE);
                marked.add(p);
            }
            paintDrawables(marked);
        }
    }

    // Draws a small box on screen showing the rectangle KDTree is searching within
    private void paintKDIllustrationBox() {
        float[] rect = viewport.getRect();

        // Draws the rectangle range it is searching in
        gc.setStroke(Type.getColor(Type.ILLUSTRATE));
        LinePath path = new LinePath(createRect(rect[0], rect[1], rect[2], rect[3]), Type.ILLUSTRATE);
        gc.setLineWidth(2/getZoom());
        path.draw(gc);
    }


    /**
     * Returns the Affine
     * @return Affine
     */
    public Affine getTrans() {
        return trans;
    }


    /**
     * Returns new Stage
     * @return Stage
     */
    public Stage getNewStage() {
        return newStage;
    }

    /**
     * Returns zoom level
     * @return double
     */
    public double getZoom() {
        return trans.getMyy();
    }

    /**
     * Returns Viewport
     * @return Viewport
     */
    public Viewport getViewport() {
        return viewport;
    }
}