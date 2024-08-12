package Model;

import javafx.scene.paint.Color;

/**
 * Enumerator for different types of OSM & MapData features used for drawing
 * Each type has several properties associated with it: color, fill property, fontsize & linewidth
 */
public enum Type {
    UNKNOWN,
    STREETNAME,
    BUILDING,
    PRIMARYWAY,
    MOTORWAY,
    HIGHWAY, //Most ways, on Samsoe at least, are highways
    TERTIARYWAY,
    COASTLINE,
    RESIDENTIAL,
    WATER,
    WATERWAY,
    RAILWAY,
    FARM,
    MEADOW,
    HEATH,
    FOREST,
    ILLUSTRATE,
    ROUTE,
    GRASS,
    ISLAND,
    // Points of interest
    FUEL, BANK, CAFE, CLOTHES, RESTAURANT, SHOPPING, SUPERMARKET,
    PIN,
    SOURCEPIN,
    DESTINATIONPIN,
    BACKGROUND,
    NODE,
    WAY,
    PARK,
    CITYNAME;


    /**
     * Gets the color associated with the given Type - depends on the current theme
     * @param type Type
     * @return the color associated with the given type
     */
    public static Color getColor(Type type) {
        switch (Model.getInstance().getColorScheme()) {
            case 0:
                switch (type) {
                    case FUEL:
                    case BANK:
                    case CAFE:
                    case CLOTHES:
                    case RESTAURANT:
                    case SHOPPING:
                    case SUPERMARKET:
                    case RAILWAY:
                        return Color.rgb(186, 186, 186);
                    case BUILDING:
                        return Color.rgb(217, 208, 201);
                    case MOTORWAY:
                        //return Color.SILVER;
                        return Color.rgb(232, 146, 162);
                    case PRIMARYWAY: //None on Samsoe
                        return Color.rgb(249, 178, 156);
                    case HIGHWAY:
                        return Color.rgb(255, 255, 255);
                    case TERTIARYWAY: //Slightly larger ways
                        return Color.rgb(255, 255, 255);
                    case ILLUSTRATE:
                        return Color.RED;
                    case PARK:
                        return Color.rgb(200, 250, 204);
                    case WATER:
                    case WATERWAY:
                    case BACKGROUND:
                        return Color.rgb(170, 211, 223);
                    case FARM:
                        return Color.rgb(245, 220, 186);
                    case MEADOW:
                        return Color.rgb(205, 235, 176);
                    case HEATH:
                        return Color.rgb(214, 217, 159);
                    case FOREST:
                    case GRASS:
                        return Color.rgb(173, 209, 158);
                    case ROUTE:
                        return Color.PURPLE;
                    case ISLAND:
                        return Color.rgb(238, 240, 213);
                    case RESIDENTIAL:
                        return Color.rgb(224, 223, 223);
                    case STREETNAME:
                    case CITYNAME:
                        return Color.BLACK;

                }
            case 1: // Google Maps Theme
                switch (type) {
                    case FUEL:
                    case BANK:
                    case CAFE:
                    case CLOTHES:
                    case RESTAURANT:
                    case SHOPPING:
                    case SUPERMARKET:
                        return Color.BLACK;
                    case RAILWAY:
                        return Color.rgb(186, 186, 186);
                    case BUILDING:
                        return Color.rgb(241, 243, 244);
                    case MOTORWAY:
                    case PRIMARYWAY:
                        return Color.rgb(248, 212, 112);
                    case HIGHWAY:
                    case TERTIARYWAY:
                        return Color.rgb(255, 255, 255);
                    case ILLUSTRATE:
                        return Color.RED;
                    case PARK:
                        return Color.rgb(197, 232, 197);
                    case WATER:
                    case WATERWAY:
                    case BACKGROUND:
                        return Color.rgb(170, 218, 255);
                    case FARM:
                    case MEADOW:
                    case HEATH:
                    case GRASS:
                    case RESIDENTIAL:
                        return Color.rgb(232, 232, 232);
                    case ISLAND:
                        return Color.rgb(235, 233, 228);
                    case FOREST:
                        return Color.rgb(206, 238, 206);
                    case ROUTE:
                        return Color.rgb(102, 157, 246);
                    case STREETNAME:
                    case CITYNAME:
                        return Color.BLACK;
                }
            case 2: //Dark theme
                switch (type) {
                    case FUEL:
                    case BANK:
                    case CAFE:
                    case CLOTHES:
                    case RESTAURANT:
                    case SHOPPING:
                    case SUPERMARKET:
                        return Color.BLACK;
                    case RAILWAY:
                        return Color.rgb(17, 33, 54);
                    case BUILDING:
                        return Color.rgb(29, 44, 77);
                    case HIGHWAY:
                        return Color.rgb(48, 74, 125);
                    case TERTIARYWAY:
                        return Color.rgb(48, 111, 128);
                    case PRIMARYWAY:
                        return Color.rgb(44, 102, 117);
                    case MOTORWAY:
                        return Color.rgb(34, 151, 181);
                    case ILLUSTRATE:
                        return Color.RED;
                    case WATER:
                    case WATERWAY:
                    case BACKGROUND:
                        return Color.rgb(14, 22, 38);
                    case FARM:
                    case MEADOW:
                    case HEATH:
                    case ISLAND:
                        return Color.rgb(2, 62, 88);
                    case GRASS:
                    case PARK:
                        return Color.rgb(3, 43, 38);
                    case FOREST:
                        return Color.rgb(0, 36, 31);
                    case ROUTE:
                        return Color.rgb(102, 157, 246);
                    case RESIDENTIAL:
                        return Color.rgb(29, 44, 77);
                    case STREETNAME:
                    case CITYNAME:
                        return Color.WHITE;

                }
            default:
                return null;
        }
    }


    /**
     * Gets the fill property of the given Type
     * @param type Type
     * @return boolean
     */
    public static boolean getFill(Type type) {
        if (type == null) {
            return false;
        }
        switch (type) {
            case BUILDING:
            case WATER:
            case FARM:
            case MEADOW:
            case HEATH:
            case FOREST:
            case GRASS:
            case RESIDENTIAL:
            case PARK:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the fontsize associated with the given type
     * @param type Type
     * @return double
     */
    public static double getFontSize(Type type) {
        switch (type) {
            case TERTIARYWAY:
                return 9;
            case PRIMARYWAY:
                return 12;
            case MOTORWAY:
                return 14;
            case HIGHWAY:
            default:
                return 9;
        }
    }

    /**
     * Returns a linewidth associated with the given type
     * @param type Type
     * @return double
     */
    public static double getLineWidth(Type type) {
        switch (type) {
            case HIGHWAY:
            case ROUTE:
                return 3;
            case TERTIARYWAY:
                return 9;
            case WATERWAY:
                return 1.5;
            case PRIMARYWAY:
                return 10;
            case RAILWAY:
                return 1;
            case MOTORWAY:
                return 12;
            default:
                return 1;
        }
    }
}