package Model;

import java.util.Map;

/*
 * Default access restrictions according to the Danish traffic regulations ("Færdselsloven" and "Bekendtgørelse om Vejafmærkning")
 *
 * The regulations distinguish between mofa ("lille knallert") having a maximum speed of 30 km/h and moped ("stor knallert") having a maximum speed of 45 km/h. Mofas are allowed on cycleways.
 * Pedestrians may only use cycleways if there are no adjacent footways. For routing purposes it is safe to assume that pedestrian access is permitted.
 * Rules for 'path' and 'track' based on regulations for access to forests and countryside (Vejledning om adgangsreglerne)
 * Motor vehicles including mopeds and mofas are not allowed in forests. Motor vehicles are allowed on tracks and paths in the countryside if the track or path appears suitable for the type of vehicle. The landowner may however prohibit motorized access using signs.
 * Horses are not allowed in privately owned forests unless the owner has explicitly permitted horse riding. In state owned forests horses are allowed on paved tracks and on unpaved tracks wider than 2.5 meter. In the countryside horses are allowed on suitable tracks and paths.
 * Cycling is allowed on paths that appear suitable for normal bicycles.
 * Access to private forests is only permitted between 6:00 and sunset.
 *
 * Source: https://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Access_restrictions
 */

/**
 * Responsible for determining standard speed limit for highways
 * and other highway properties such as: drivable, bikeable, walkable, roundabout, oneway - all based on further OSM specifications/restrictions
 */
public class HighwayDecoder {


    /**
     * Grabs the maxSpeed value
     * if it already assigned explicitly in the osmfile it will return the assigned value
     * otherwise it returns the standard maxspeed associated with the given type of road
     * @param highwayType the type of highway given as string - the valid types are only types of roads used in openstreetmaps
     * @param highwayValues hashmap used during loading
     * @return int value representing the maxspeed for the given type of road
     */
    public int getMaxSpeed(String highwayType, Map<String, String> highwayValues) {
        String s = "";
        String str = "";
        if (highwayValues.get("maxspeed") != null) {
            str = highwayValues.get("maxspeed");
            if (str.contains(".")) {
                str = s.split("\\.")[0]; // Disregards do
            }
            str = str.replaceAll("\\D", ""); // Removes maxspeed tags such as "DK:Rural", "Implicit", "Default" etc.
        }

        if (!str.equals("")) {
            return Integer.parseInt(str);
        } else {
            return calculateStandardMaxSpeed(highwayType);
        }
    }

    // Predefined speed limits for different types of road
    private int calculateStandardMaxSpeed(String highwayType) {
        switch (highwayType) {
            case "motorway":
                return 130;
            case "trunk":
            case "primary":
            case "secondary":
            case "tertiary":
            case "unclassified":
                return 80;
            case "residential":
                return 50;
            case "living_street":
                return 15;
            default:
                return 50;
        }
    }

    /**
     * Returns whether the type of highway is drivable
     * @param highwayType type of highway as determined during OSMLoad
     * @param highwayValues map from OSMLoad with specific mapped restrictions/specifications
     * @return boolean for whether it's drivable or not
     */
    public boolean isDrivable(String highwayType, Map<String, String> highwayValues) {
        String motor_vehicle = highwayValues.get("motor_vehicle");
        String motorcar = highwayValues.get("motorcar");
        String access = highwayValues.get("access");

        if (motorcar != null) {
            if (motorcar.equals("yes")) {
                return true;
            }
            if (motorcar.equals("no")) {
                return false;
            }
        }

        if (motor_vehicle != null) {
            if (motor_vehicle.equals("yes")) {
                return true;
            }
            if (motor_vehicle.equals("no")) {
                return false;
            }
        }

        if (access != null) {
            if (access.equals("no")) {
                return false;
            }
            if (access.equals("private")) {
                return false;
            }
            if (access.equals("forestry")) {
                return false;
            }
        }

        switch (highwayType) {
            case "motorway":
            case "trunk":
            case "primary":
            case "secondary":
            case "tertiary":
            case "unclassified":
            case "residential":
            case "living_street":
            case "track":
            case "path":
            case "service":
                return true;
            case "bridleway":
            case "cycleway":
            case "footway":
            case "pedestrian":
            case "steps":
                return false;
            default:
                return true;
        }
    }


    /**
     * Returns whether the type of highway is drivable
     * @param highwayType type of highway as determined during OSMLoad
     * @param highwayValues map from OSMLoad with specific mapped restrictions/specifications
     * @return boolean for whether it's drivable or not
     */
    public boolean isBikable(String highwayType, Map<String, String> highwayValues) {
        String bicycle = highwayValues.get("bicycle");
        String access = highwayValues.get("access");

        if (bicycle != null) {
            if (bicycle.equals("no")) {
                return false;
            }
            if (bicycle.equals("yes")) {
                return true;
            }
            if (bicycle.equals("permissive")) {
                return true;
            }
        }

        if (access != null) {
            if (access.equals("no")) {
                return false;
            }
            if (access.equals("private")) {
                return false;
            }
            if (access.equals("forestry")) {
                return false;
            }
        }

        switch (highwayType) {
            case "motorway":
            case "trunk":
            case "bridleway":
            case "footway":
            case "pedestrian":
            case "steps":
                return false;
            case "path":
            case "primary":
            case "secondary":
            case "tertiary":
            case "unclassified":
            case "residential":
            case "living_street":
            case "track":
            case "cycleway":
            default:
                return true;
        }
    }


    /**
     * Returns whether the type of highway is drivable
     * @param highwayType type of highway as determined during OSMLoad
     * @param highwayValues map from OSMLoad with specific mapped restrictions/specifications
     * @return boolean for whether it's drivable or not
     */
    public boolean isWalkable(String highwayType, Map<String, String> highwayValues) {
        String foot = highwayValues.get("foot");
        String access = highwayValues.get("access");

        if (foot != null) {
            if (foot.equals("no")) {
                return false;
            }
            if (foot.equals("yes")) {
                return true;
            }
            if (foot.equals("permissive")) {
                return true;
            }
        }

        if (access != null) {
            if (access.equals("no")) {
                return false;
            }
            if (access.equals("private")) {
                return false;
            }
            if (access.equals("forestry")) {
                return false;
            }
        }

        switch (highwayType) {
            case "motorway":
            case "trunk":
                return false;
            case "primary":
            case "secondary":
            case "tertiary":
            case "unclassified":
            case "residential":
            case "living_street":
            case "track":
            case "path":
                return true;
            case "bridleway":
                return false;
            case "cycleway": //Pedestrians may only use cycleways if there are no adjacent footways. For routing purposes it is safe to assume that pedestrian access is permitted.
                return true;
            case "footway":
            case "pedestrian":
            case "steps":
                return true;
            default:
                return true;
        }
    }


    /**
     * Checks whether the given highway is oneway or not
     * @param highwayValues HashMap used during osm loading mapping
     * @return true if highway is oneway or false if it is not oneway
     */
    public boolean isOneWay(Map<String, String> highwayValues) {
        String isOneWay = highwayValues.get("oneway");
        if (isOneWay != null && isOneWay.equals("yes") || isRoundabout(highwayValues)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Checks whether the given highway is a roundabout or not
     * @param highwayValues hashmap used to determine if the highway is roundabout
     * @return true if is roundabout, false if not roundabout
     */
    public boolean isRoundabout(Map<String, String> highwayValues) {
        String isRoundabout = highwayValues.get("junction");
        if (isRoundabout != null && isRoundabout.equals("roundabout")) {
            return true;
        } else {
            return false;
        }
    }
}
