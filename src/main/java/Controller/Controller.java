package Controller;

import Model.AddressParser.AddressParser;
import Model.AddressParser.Address;
import Model.MapComponents.PointOfInterest;
import Model.Model;
import Model.OSMNode;
import Model.SortedAddressArrayList;
import Model.MapComponents.Highway;

import View.MapCanvas;

import View.Viewport;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import Model.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Controller responsible for view.fxml
 */
public class Controller {
    @FXML
    private Scene scene;

    @FXML
    private BorderPane borderPane;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    public HBox loadingStatusHBox;

    @FXML
    public VBox loadVBoxContainer, VRouteDirections;

    @FXML
    public Button defaultMapButton, defaultMapButton2, customMapButton;

    @FXML
    private StackPane searchPane, burgerMenuStackPane, POIPane, scaleBarPane, loadPane;

    @FXML
    private ListView routeFromList, routeToList, searchPaneList, POIListView;

    @FXML
    private ImageView searchImageView, scalebarImage;

    @FXML
    private TextField routeTo, routeFrom, searchField;

    @FXML
    private ComboBox comboBoxTransport;

    @FXML
    private Label travelTime, travelDistance, closestHighwayLabel, invalidRouteLabel;

    @FXML
    private Label mouseCoordinatesLabel, scalebarLabel, loadingStatusLabel;


    private Stage stage;
    public MapCanvas mapCanvas;
    Model model = Model.getInstance();
    Point2D lastMouse;

    MenuItem addPointOfInterestMenu = new MenuItem("Add point of interest");
    MenuItem clearRouteMenu = new MenuItem("Clear route");
    ContextMenu contextMenu = new ContextMenu(addPointOfInterestMenu, clearRouteMenu);

    /**
     * Controller initializer sets all event handlers and initializes MapCanvas
     */
    public void initialize() {
        mapCanvas.initialize(mainAnchorPane);

        // Sets eventlistener for textField for auto suggestions
        routeFrom.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                suggestionListHandler(e, false);
            }
        });
        routeTo.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                suggestionListHandler(e, false);
            }
        });
        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                suggestionListHandler(e, true);
            }
        });

        scene.widthProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                responsiveHandler((double) newValue);
            }
        });

        mapCanvas.setOnContextMenuRequested(e -> {
            this.contextMenu.show(stage, e.getScreenX(), e.getScreenY());
        });

        // Add handlers to menu items
        addPointOfInterestMenu.setOnAction(e -> {
            double x = -new Viewport().mouseCoordToLon(lastMouse.getX(), mapCanvas);
            double y = -new Viewport().mouseCoordToLat(lastMouse.getY(), mapCanvas);
            handlePointOfInterestInputDialog(x, y);
            mapCanvas.repaint();
        });
        clearRouteMenu.setOnAction(e -> {
            clearRouteAccessories();
        });

        reloadPointOfInterests();
    }


    /**
     * @return model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Event handler for mouse movement on canvas
     * @param e MouseEvent
     */
    @FXML
    public void setOnMouseMoved(MouseEvent e) {
        double lon = -new Viewport().mouseCoordToLon(e.getX(), mapCanvas);
        double lat = -new Viewport().mouseCoordToLat(e.getY(), mapCanvas);
        updateClosestHighway((float) lat, (float) lon);
    }

    // Updates the closest highway to mouse
    private void updateClosestHighway(float lat, float lon) {
        Highway closestWay = model.findNearestNeighbor(lon, lat);

        if (closestWay == null) {
            return;
        }

        closestHighwayLabel.setText("Closest street: " + closestWay.getStreet());
        mouseCoordinatesLabel.setText("Mouse lon & lat: " + -lat + ", " + (1f/0.56f) * lon);
    }


    /**
     * Event handler for mouse press on canvas
     * @param e MouseEvent
     */
    public void setOnMousePressed(MouseEvent e) {
        lastMouse = new Point2D(e.getX(), e.getY());

        updateScalebar();
        // Records current mouse coordinates upon click
        double x = -new Viewport().mouseCoordToLon(e.getX(), mapCanvas);
        double y = -new Viewport().mouseCoordToLat(e.getY(), mapCanvas);


        if (e.isShiftDown()) {
            handlePointOfInterestInputDialog(x, y);
        }

        mapCanvas.repaint();
        //Point2D mc = view.toModelCoords(e.getX(), e.getY());  // not used - may be used in future tho
    }


    /**
     * Event handler for mouse dragging on canvas
     * @param e MouseEvent
     */
    public void setOnMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        mapCanvas.pan(dx, dy);

        lastMouse = new Point2D(e.getX(), e.getY());
    }

    /**
     * Event Handler for scrolling on canvas
     * @param e MouseEvent
     */
    public void setOnScroll(ScrollEvent e) {
        double factor = Math.pow(1.001, e.getDeltaY());
        mapCanvas.zoom(factor, e.getX(), e.getY());
        updateScalebar();
    }

    // Gets highway from input with nearest neighbor search
    private Highway getHighwayFromInput(Address address) {
        if (address == null || address.getNode() == null) {
            return null;
        }

        OSMNode node = address.getNode();

        return model.getHighwayTree().nearestNeighbor(node.getLon(), node.getLat(), address.getStreet());
    }


    /**
     * Event handler for route button
     */
    public void routeButtonAction() {
        clearRouteAccessories();

        // sets invalid route label to invisible in case it is visible
        invalidRouteLabel.setVisible(false);

        String from = routeFrom.getText();
        String to = routeTo.getText();

        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        Address source = AddressParser.parse(from, model.getOSMAddresses());
        Address destination = AddressParser.parse(to, model.getOSMAddresses());

        Highway nearestFrom = getHighwayFromInput(source);
        Highway nearestTo = getHighwayFromInput(destination);

        if (nearestFrom == null) {
            invalidRouteLabel.setText("Invalid start point");
            invalidRouteLabel.setVisible(true);
            return;
        }
        if (nearestTo == null) {
            invalidRouteLabel.setText("Invalid destination");
            invalidRouteLabel.setVisible(true);
            return;
        }

        if (nearestFrom == nearestTo) {
            invalidRouteLabel.setText("Cannot compute a valid route");
            invalidRouteLabel.setVisible(true);
            return;
        }

        boolean routeComputed = handleRouteComputations(nearestFrom, source.getNode(),  nearestTo, destination.getNode());
        if (routeComputed) {
            // Shows route descriptions for new route
            showDirections();
            showTravelTime();
            showTravelDistance();

            zoomToRoute(nearestFrom, nearestTo);
            placePins(source, destination);
        } else {
            clearRouteAccessories();
        }
        mapCanvas.repaint();
    }


    private boolean handleRouteComputations(Highway from, OSMNode fromNode, Highway to, OSMNode toNode) {
        try {
            OSMNode fromWayNode = closestNodeInWayToNode(from, fromNode);
            OSMNode toWayNode = closestNodeInWayToNode(to, toNode);

            model.computePath(fromWayNode, toWayNode, getTransportMode());
        } catch (Exception e) {
            invalidRouteLabel.setText("Cannot compute a valid route");
            invalidRouteLabel.setVisible(true);
            return false;
        }
        return true;
    }

    // Places pins at start and end of route
    private void placePins(Address source, Address destination) {
        OSMNode s = source.getNode();
        OSMNode d = destination.getNode();

        PointOfInterest poiSource = new PointOfInterest(s.getLon(), s.getLat(), "", Type.SOURCEPIN);
        PointOfInterest poiDestination = new PointOfInterest(d.getLon(), d.getLat(), "", Type.DESTINATIONPIN);
        model.setRoutePOI(poiSource, poiDestination);

        mapCanvas.paintRoutePins();
        mapCanvas.repaint();
    }


    private void zoomToRoute(Highway from, Highway to) {
        //Zooms to show the user the route that they've searched for.
        float[] fromNode = from.getAsPoint();
        float[] toNode = to.getAsPoint();

        float minLon, minLat, maxLat, maxLon, avgLon, avgLat;
        //The following 4 lines are unnecessary, but save a bit of space later, so I currently don't bother refactoring the code below.
        minLon = Math.min(fromNode[0], toNode[0]);
        minLat = Math.min(fromNode[1], toNode[1]);
        maxLon = Math.max(fromNode[0], toNode[0]);
        maxLat = Math.max(fromNode[1], toNode[1]);

        float lonDiff = Math.abs(maxLon - minLon);
        float latDiff = Math.abs(maxLat - minLat);

        avgLon = (minLon + maxLon) / 2;
        avgLat = (minLat + maxLat) / 2;

        float pointRatio = lonDiff / latDiff;

        double screenRatio = mapCanvas.getViewport().getWidth() / mapCanvas.getViewport().getHeight();

        double zoom;

        // Check if points are wider than screen
        if (pointRatio > screenRatio) {
            zoom = mapCanvas.getWidth() / lonDiff;
        } else {
            zoom = mapCanvas.getHeight() / latDiff;
        }

        zoom *= 0.5;

        mapCanvas.changeView(avgLon, avgLat, zoom);
    }

    // Finds the OSMNode in the given highway closest the the osmnode given as 2nd parameter
    private OSMNode closestNodeInWayToNode(Highway way, OSMNode point) {
        OSMNode closest = null;
        double closestDist = Float.MAX_VALUE;

        for (OSMNode node : way.getOSMWay()) {

            // Calculates distance from point to node
            double dist = Math.sqrt(Math.pow(node.getLon() - point.getLon(), 2) + Math.pow(node.getLat() - point.getLat(), 2));

            // Update if closer
            if (dist < closestDist) {
                closest = node;
                closestDist = dist;
            }
        }

        return closest;
    }

    private void clearRouteAccessories() {
        // Resets in case of previous searched route
        resetRouteDescription();

        model.clearRoute();
        model.setRoutePOI(null, null);
        mapCanvas.repaint();
    }


    private void drawSearchedAddress() {

        String searchInput = searchField.getText();

        if (!searchInput.equals("")) {

            Address parsedAddress = AddressParser.parse(searchInput, model.getOSMAddresses());
            Address foundAddress = model.getOSMAddresses().get(parsedAddress);

            // Checks if the user-input is a single string, and therefore not knowing if it's a street or city.
            if (parsedAddress.getCity() == null && parsedAddress.getHouse() == null && parsedAddress.getPostcode() == null) {
                // Changes parsedAddress so it can compare to OSMcities
                Address alteredParsedAddress = AddressParser.changeCityToStreet(parsedAddress);
                Address foundCity = model.getOSMCities().get(alteredParsedAddress);
                if (foundCity != null) {
                    mapCanvas.showAddressOnMap(foundCity, 40000);
                } else if (foundAddress != null) {
                    mapCanvas.showAddressOnMap(foundAddress, 120000);
                } else {
                    castPopupAlert("Address not found!", "Address not found");
                }
            } else if (foundAddress != null) {
                mapCanvas.showAddressOnMap(foundAddress, 120000);
            } else {
                castPopupAlert("Address not found!", "Address not found");
            }
        }
    }


    /**
     * Enables KDTree illustration
     */
    @FXML
    public void showKDTree() {
        model.setDrawKDTreeIllustration(!model.shouldDrawKDTreeIllustration());
        mapCanvas.repaint();
    }


    /**
     * Enables Dijkstra illustration
     */
    @FXML
    public void showDijkstra() {
        model.setDrawDijkstraIllustration(!model.shouldDrawDijkstraIllustration());
        mapCanvas.repaint();
    }


    private void disableLoadingStatus(boolean disable) {
        defaultMapButton.setDisable(disable);
        defaultMapButton2.setDisable(disable);
        customMapButton.setDisable(disable);
        setProcessStatus(disable, true);
    }


    private void castPopupAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText("");
        alert.setTitle(title);
        alert.setGraphic(null);
        alert.showAndWait();
    }

    /**
     * Sets the color theme to the default theme
     */
    @FXML
    public void setTheme0() {
        model.setColorScheme(0);
        themeChanger(0);
        mapCanvas.repaint();
    }

    /**
     * Sets the color theme to google maps theme
     */
    @FXML
    public void setTheme1() {
        model.setColorScheme(1);
        themeChanger(1);
        mapCanvas.repaint();
    }

    /**
     * Sets the color theme to night mode
     */
    @FXML
    public void setTheme2() {
        model.setColorScheme(2);
        themeChanger(2);
        mapCanvas.repaint();
    }

    private void changeThemeColors(int theme) {
        Color textColor = Color.BLACK;

        if (theme == 2) {
            textColor = Color.web("#d4d4d4");
        }

        // Changes point of interest list colors
        for (int i = 0; i < POIListView.getItems().size(); i++) {
            TextFlow textFlow = (TextFlow) POIListView.getItems().get(i);
            Label label = (Label) textFlow.getChildren().get(0);
            label.setTextFill(textColor);
        }

        for (int i = 0; i < VRouteDirections.getChildren().size(); i++) {
            TextFlow textFlow = (TextFlow) VRouteDirections.getChildren().get(i);
            Text text = (Text) textFlow.getChildren().get(1);
            text.setFill(textColor);

            if (theme == 2) {
                text.setStyle("-fx-font-weight: bolder");
            } else {
                text.setStyle("-fx-font-weight: normal");
            }
        }

    }

    private void themeChanger(int theme) {
        String newStylesheet = "";
        if (theme == 0 || theme == 1) { // default theme
            scene.getStylesheets().clear();
            scene.getStylesheets().add("css/stylesheet.css");
        } else if (theme == 2) {
            scene.getStylesheets().add("css/darkmode.css");
        }

        changeThemeColors(theme);
    }

    /**
     * Adds route descriptions and corresponding images to ScrollPane & VBox
     */
    private void showDirections() {
        List<String> directions = model.getPath().computeRouteDescription();

        for (String direction : directions) {
            TextFlow flow = new TextFlow();
            flow.setPadding(new Insets(5, 0, 0, 2));
            flow.setLineSpacing(5);

            Text text;
            Image directionImage = null;

            if (direction.contains("straight")) {
                directionImage = new Image("DirectionImages/StraightArrow.png");
                text = new Text(direction);
            } else if (direction.contains("left")) {
                directionImage = new Image("DirectionImages/LeftArrow.png");
                text = new Text(direction);
            } else if (direction.contains("right")) {
                directionImage = new Image("DirectionImages/RightArrow.png");
                text = new Text(direction);
            } else if (direction.contains("roundabout")) {
                directionImage = new Image("DirectionImages/Roundabout.png");
                text = new Text(direction);
            } else {
                text = new Text(direction);
            }

            text.setId("directionText");
            flow.setId("directionTextFlow");

            ImageView imageView = new ImageView(directionImage);
            imageView.setFitWidth(15);
            imageView.setFitHeight(15);

            flow.getChildren().addAll(imageView, text);
            VRouteDirections.getChildren().add(flow);
        }
    }


    // returns a list of suggested addresses
    private List<String> searchSuggestedAddresses(TextField textField, SortedAddressArrayList addressList, boolean isCitySearch) {
        String searched = textField.getText();

        if (searched.isEmpty()) {
            return new ArrayList<>();
        }

        List<Address> addresses = addressList.recommendedAddresses(searched, isCitySearch);

        List<String> suggestions = new ArrayList<>();

        for (Address address : addresses) {
            String suggestion = address.getFormattedAddress();
            if (!suggestion.isEmpty()) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }


    /**
     * Event handler for suggestion list
     * @param event
     */
    @FXML
    public void suggestionListHandler(KeyEvent event) {
        suggestionListHandler(event, event.getSource() == searchField);
    }


    // suggestions for Listview
    private void suggestionListHandler(KeyEvent event, boolean isMainSearchField) {
        // Search textfield being handled
        TextField inputField = (TextField) event.getSource();
        ListView targetList;
        List<String> suggestions;
        if (isMainSearchField) {
            // Combine address and cities list so we can suggest both cities and addresses
            SortedAddressArrayList addresses = model.getOSMAddresses();
            SortedAddressArrayList cities = model.getOSMCities();

            // The search textfield's corresponding ListView of suggestions
            targetList = searchPaneList;

            List<String> addressSuggestions = searchSuggestedAddresses(inputField, addresses, false);
            suggestions = searchSuggestedAddresses(inputField, cities, true);
            suggestions.addAll(addressSuggestions);

        } else {
            // The search textfield's corresponding ListView of suggestions
            targetList = inputField == routeFrom ? routeFromList : routeToList;
            suggestions = searchSuggestedAddresses(inputField, model.getOSMAddresses(), false);
        }

        if (targetList != null) {

            // Handles if delete key is pressed on an empty search field
            if (event.getCode().equals(KeyCode.BACK_SPACE) && targetList.getItems().isEmpty()) {
                targetList.setManaged(false);
                targetList.setVisible(false);
            }

            // Changes focus to the corresponding ListView to make it navigable
            if (event.getCode().equals(KeyCode.DOWN)) {
                if (!targetList.isFocused()) {
                    targetList.requestFocus();
                }
                handleListViewSelection(event);
            }


            if (!isMainSearchField) {
                // Ensures we only have one ListView open at a time
                if (targetList == routeFromList && routeToList.isManaged()) {
                    routeToList.setManaged(false);
                    routeToList.setVisible(false);
                } else if (targetList == routeToList && routeFromList.isManaged()) {
                    routeFromList.setManaged(false);
                    routeFromList.setVisible(false);
                }
            }

            // For clearing old results before adding new results
            targetList.getItems().clear();

            if (suggestions.isEmpty()) {
                // removes the ListView from the flow if no suggestions
                targetList.setManaged(false);
                targetList.setVisible(false);
            } else {
                targetList.getItems().addAll(suggestions);
            }

            if (event.getCode().equals(KeyCode.UNDEFINED)) {
                return;
            }

            // Puts ListView back in the flow if it contains items
            targetList.setManaged(true);
            targetList.setVisible(true);
            targetList.setMaxHeight(100);
            targetList.setMinHeight(100);

            if (event.getCode().equals(KeyCode.ENTER)) {
                if (!targetList.getItems().isEmpty()) {
                    inputField.setText((String) targetList.getItems().get(0));
                    targetList.setManaged(false); // Disables/hides the ListView after result is grabbed
                    targetList.setVisible(false);
                }

                if (isMainSearchField) {
                    drawSearchedAddress();
                }
            }
        }
    }




    // Handles which ListView to open and show selections for
    private void handleListViewSelect(Event event) {
        ListView suggestionList = (ListView) event.getSource();

        TextField targetSearchField = null;
        if (suggestionList == routeFromList) {
            targetSearchField = routeFrom;
        } else if (suggestionList == routeToList) {
            targetSearchField = routeTo;
        } else if (suggestionList == searchPaneList) {
            targetSearchField = searchField;
        }

        targetSearchField.setText((String) suggestionList.getSelectionModel().getSelectedItem());
        if (targetSearchField == searchField) {
            drawSearchedAddress();
            searchField.requestFocus();
        }
        suggestionList.setManaged(false);
        suggestionList.setVisible(false);
    }



    /**
     * Adds the total travel time for the route to the label travelTime
     */
    public void showTravelTime() {
        travelTime.setText(travelTime.getText() + model.getPath().getFormattedTotalTravelTime());
    }

    /**
     * Adds the total travel distance for the route to the label travelDistance
     */
    public void showTravelDistance() {
        travelDistance.setText(travelDistance.getText() + model.getPath().getFormattedTotalDistance());
    }

    /**
     * Resetting travelTime, travelDistance and ScrollPane/VBox content
     * to ensure next route search does not stack the descriptions
     */
    public void resetRouteDescription() {
        // Checks if first time searched for route
        if (VRouteDirections != null) {
            travelTime.setText("Travel time: ");
            travelDistance.setText("Distance: ");
            VRouteDirections.getChildren().clear();
        }
    }

    /**
     * Event handler for click selection on listview
     * @param mouseEvent MouseEvent
     */
    @FXML
    public void handleMouseClickListView(MouseEvent mouseEvent) {
        handleListViewSelect(mouseEvent);
    }

    /**
     * Event handler for listview selection with keyevents
     * @param keyEvent KeyEvent
     */
    public void handleListViewSelection(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleListViewSelect(keyEvent);
        }
    }


    /**
     * Returns int depending on selected transportation mode
     * @return 0 if combobox(transportation mode) is Car, 1 if bike, 2 if walk
     */
    public int getTransportMode() {
        var mode = comboBoxTransport.getValue();
        if (mode.equals("Car")) {
            return 0;
        } else if (mode.equals("Bike")) {
            return 1;
        } else {
            return 2;
        }
    }


    /**
     * Opens point of interest list
     */
    @FXML
    public void openPOIList() {
        // boolean for whether it is already opened or closed
        boolean state = !POIListView.isManaged();

        POIListView.setManaged(state);
        POIListView.setVisible(state);

        if (!state) {
            POIListView.setMaxHeight(0);
        } else {
            POIListView.setMaxHeight(200);
            POIListView.setMinHeight(200);
        }
    }


    /**
     * Event handler for sidebar opening from hamburgermenu
     */
    @FXML
    public void openHamburgerMenu() {
        boolean sidebarState = borderPane.getLeft().isManaged();

        if (!sidebarState) {
            borderPane.getLeft().setManaged(true);
            borderPane.getLeft().setVisible(true);

            searchPane.setManaged(false);
            searchPane.setVisible(false);

        } else {
            borderPane.getLeft().setManaged(false);
            borderPane.getLeft().setVisible(false);
            searchPane.setManaged(true);
            searchPane.setVisible(true);
        }
    }

    // Makes the UI responsive
    private void responsiveHandler(double windowWidth) {
        if (windowWidth <= 510) {
            // Alignment for imageview in searchPane
            if (windowWidth > 460) {
                searchImageView.setTranslateX(searchField.getTranslateX()-30);
            } else if (windowWidth > 440) {
                searchImageView.setTranslateX(searchField.getTranslateX()-10);
            } else {
                searchImageView.setTranslateX(searchField.getTranslateX());
            }

            // Makes sidebar go away
            borderPane.getLeft().setManaged(false);
            borderPane.getLeft().setVisible(false);

            // Scales search pane
            searchPane.setMaxWidth(windowWidth - 100);
            searchPane.setMinWidth(windowWidth - 100);

            // Enables burgermenu button(imageview)
            burgerMenuStackPane.setManaged(true);
            burgerMenuStackPane.setVisible(true);
        } else {
            // enables sidebar again
            borderPane.getLeft().setManaged(true);
            borderPane.getLeft().setVisible(true);

            // Ensures search pane is enabled and visible in case burgermenu has disabled it
            searchPane.setManaged(true);
            searchPane.setVisible(true);

            // disables sidebar again
            burgerMenuStackPane.setManaged(false);
            burgerMenuStackPane.setVisible(false);

            // Removes translatex from imageview
            searchImageView.setTranslateX(0);
        }

        // Scales search pane
        if (windowWidth > 510 && windowWidth < 635) {
            searchPane.setMaxWidth(windowWidth - 325);
            searchPane.setMinWidth(windowWidth - 325);
        }

        // disables bookmark button
        if (windowWidth < 760) {
            POIPane.setVisible(false);
            POIPane.setManaged(false);
        } else {
            POIPane.setVisible(true);
            POIPane.setManaged(true);
        }
    }


    // Updates scalebar
    private void updateScalebar() {
        // What 1 lat corresponds to in km in denmark
        double latDegToKm = 110.574;

        // Minus sidebar
        double screenLat = mapCanvas.getWidth() / mapCanvas.getZoom();

        // Calculate into km
        double screenKm = screenLat * latDegToKm;

        // margin for scalebar inside stackpane
        int offset = 40;

        double ratio = (scaleBarPane.getWidth() - offset) / mapCanvas.getWidth();

        double labelM = (ratio * screenKm) * 1000;

        // 10m - 20m - 50m - 100m - 200m - 500m - 1000m - 2km - 5km - 10km - 20km - 50km - 100km - 200km - 500km
        int[] between = new int[]{1, 2, 5, 10, 20, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
        int meters = 0;
        for (int i = 1; i < between.length; i++) {
            if (labelM < between[i]) {
                meters = between[i - 1];
                break;
            }
        }

        double width = Math.floor((meters / labelM) * (scaleBarPane.getWidth() - offset)); // Math floor to ensure rounded pixels

        String labelText;
        if (width <= scaleBarPane.getWidth() - offset) {
            scalebarImage.setFitWidth(width);

            if (meters > 1000) {
                labelText = "" + (meters/1000) + " km";
            } else {
                labelText = "" + meters + " m";
            }
        } else {
            labelText = "";
        }

        scalebarLabel.setText(labelText);
    }

    /**
     * Switches the search fields & computes the new reverse route if route has been computed already
     */
    @FXML
    public void switchSearchFields() {
        String tmp = routeFrom.getText();
        routeFrom.setText(routeTo.getText());
        routeTo.setText(tmp);

        if (!model.getRoute().isEmpty()) {
            routeButtonAction();
        }
    }

    // Handles points of interest creation
    private void handlePointOfInterestInputDialog(double x, double y) {
        TextInputDialog textInput = new TextInputDialog();
        textInput.setHeaderText(null);
        textInput.setGraphic(null);
        textInput.setContentText("Enter name of Point of Interest:");
        textInput.setTitle("Point of Interest");
        Optional<String> name = textInput.showAndWait();
        if(name.isPresent()){
            if(hasPOIDuplicate(name.get())){
                textInput.setHeaderText("Name already taken!");
                name = textInput.showAndWait();
            }
        }
        name.ifPresent(s -> { if (!hasPOIDuplicate(s)) {
            model.addPointOfInterest(new PointOfInterest((float) x, (float) y, s));
            handlePointOfInterest(s,x,y);
        }
        });
    }

    // Checks whether point of interest already exists with the given name
    private boolean hasPOIDuplicate(String name){
        for(PointOfInterest p: model.getPointsOfInterest()){
            if(p.getName().equals(name)){
                return true;
            }
        }
        return false;
    }


    /**
     * Event handler for point of interest and listview adding
     * @param name String for the name of the point of interest to be saved
     * @param x coordinate for POI
     * @param y coordinate for POI
     */
    public void handlePointOfInterest(String name, double x, double y){
        handleListViewPointOfInterests(name);
        changeThemeColors(model.getColorScheme());
    }

    // handles adding POI to listview
    private void handleListViewPointOfInterests(String name) {
        TextFlow textFlow = new TextFlow();
        Label poi = new Label(name);

        poi.setMinWidth(120);
        poi.setMaxWidth(120);

        Button delete = new Button("-");
        delete.setOnAction((actionEvent -> {
            deletePointOfInterest(name,textFlow);
        }));
        textFlow.getChildren().add(poi);
        textFlow.getChildren().add(delete);

        POIListView.getItems().add(textFlow);
    }

    // reloads/refreshes the point of interest
    private void reloadPointOfInterests() {
        POIListView.getItems().clear();

        for (int i = 0; i < model.getPointsOfInterest().size(); i++) {
            PointOfInterest poi = model.getPointsOfInterest().get(i);
            handlePointOfInterest(poi.getName(), poi.getMaxX(), poi.getMaxY());
        }
    }


    private void deletePointOfInterest(String name, TextFlow flow) {
        for (PointOfInterest p :
                model.getPointsOfInterest()) {
            if (p.getName().equals(name)) {
                model.deletePointOfInterest(p);
                POIListView.getItems().remove(flow);
                mapCanvas.repaint();
                break;
            }
        }
    }

    /**
     * Event handler for click selection on listview for point of interest
     */
    @FXML
    public void handleMouseClickListViewPOI() {
        try {
            TextFlow text = (TextFlow) POIListView.getSelectionModel().getSelectedItems().get(0);
            var nodes = text.getChildren();
            StringBuilder sb = new StringBuilder();
            sb.append(((Label) nodes.get(0)).getText());
            String txt = sb.toString();
            for (PointOfInterest p : model.getPointsOfInterest()) {
                if (p.getName().equals(txt)) {
                    mapCanvas.changeView(p.getMaxX(), p.getMaxY(), mapCanvas.getZoom());
                }
            }
        } catch(Exception ignore) {
        }
    }


    /**
     * Responsible for loading new file, uses a new thread to load the given selected file
     */
    public void loadFileAction() {
        File file = new FileChooser().showOpenDialog(mapCanvas.getNewStage());
        new Thread(() -> {
            try {
                platformRunLater(() -> {
                    if (model.getHasBeenLoaded()) {
                        changeLoadingBar();
                        toggleProcessStatusPane();
                    }
                    disableLoadingStatus(true);
                });

                model.load(file);
                mapCanvas.resetView();
                mapCanvas.initialize(mainAnchorPane);

                platformRunLater(() -> {
                    if (model.getHasBeenLoaded()) {
                        disableNode(loadPane);
                    } else {
                        disableLoadingStatus(false);
                    }
                    reloadPointOfInterests();
                });


            } catch (Exception e) {
                e.printStackTrace();
                platformRunLater(() -> {
                    disableLoadingStatus(false);
                    if (model.getHasBeenLoaded()) {
                        disableNode(loadPane);
                    }
                });
            }
        }).start();

        reloadPointOfInterests();
    }

    /**
     * Responsible for saving the given file - runs in a separate thread
     */
    public void saveFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".bin",".bin"));

        File file = fileChooser.showSaveDialog(mapCanvas.getNewStage());

        new Thread(() -> {
            try {
                platformRunLater(() -> {
                    changeLoadingBar();
                    toggleProcessStatusPane();
                    setProcessStatus(true, false);
                });

                model.save(file);

                platformRunLater(() -> {
                    disableNode(loadPane);
                });


            } catch (IllegalArgumentException invalid) {
                platformRunLater(() -> {
                    disableNode(loadPane);
                    castPopupAlert("Can only save files of type .bin", "Wrong type of file");
                });

                invalid.printStackTrace();
            } catch (Exception e) {
                platformRunLater(() -> {
                    disableNode(loadPane);
                });
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Loads the embedded / initial file defined in Model - in a separate thread
     */
    public void loadInitFile(ActionEvent event) {

        if (event.getSource() == defaultMapButton) {
            model.setInitFile("denmark.bin");
        } else if (event.getSource() == defaultMapButton2) {
            model.setInitFile("fyn.bin");
        }

        disableLoadingStatus(true);

        new Thread(() -> {
            try {
                model.loadInitFile();
                mapCanvas.initialize(mainAnchorPane);

                platformRunLater(() -> {
                    disableLoadingStatus(false);
                    disableNode(loadPane);
                });

            } catch (Exception e) {
                platformRunLater(() -> {
                    castPopupAlert("Something went wrong", "Could not load file");
                });
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Event handler for loading custom file
     */
    @FXML
    public void loadCustomFile() {
        loadFileAction();
    }


    private void disableNode(Node node) {
        node.setManaged(false);
        node.setVisible(false);
    }


    /**
     * Sets the loading bar progress
     * @param enable boolean true if to enable, false if disable
     * @param load boolean true if used for loading, false if used for saving
     */
    public void setProcessStatus(boolean enable, boolean load) {
        if (enable) {
            loadingStatusHBox.setManaged(true);
            loadingStatusHBox.setVisible(true);
        } else {
            loadingStatusHBox.setManaged(false);
            loadingStatusHBox.setVisible(false);
        }

        if (load) {
            loadingStatusLabel.setText("Loading...");
        } else {
            loadingStatusLabel.setText("Saving...");
        }
    }


    private void toggleProcessStatusPane() {
        loadPane.setManaged(true);
        loadPane.setVisible(true);
    }


    /**
     * Changes the container of the progressBar to only contain the progressbar
     */
    public void changeLoadingBar() {
        VBox vBox = loadVBoxContainer;

        for (int i = 0; i < loadVBoxContainer.getChildren().size(); i++) {
            loadVBoxContainer.getChildren().get(i).setManaged(false);
            loadVBoxContainer.getChildren().get(i).setVisible(false);
        }

        loadPane.setLayoutX(264.0);
        loadPane.setLayoutY(288.0);

        loadPane.setPrefHeight(53.0);
        loadPane.setPrefWidth(442.0);
    }

    /**
     * Sets stage
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    // Used for abstraction of platform.runlater to avoid code duplication
    private interface PlatformCall {
        void run();
    }

    private void platformRunLater(PlatformCall platformCall) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                platformCall.run();
            }
        });
    }
}


