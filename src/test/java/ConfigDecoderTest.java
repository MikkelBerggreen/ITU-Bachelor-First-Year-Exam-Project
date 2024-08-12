import Model.HighwayDecoder;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigDecoderTest {
    HighwayDecoder decoder = new HighwayDecoder();


    /**
     * Tests the int getMaxSpeed(String highwayType, Map<String, String> highwayValues) method
     */
    @Test
    public void getMaxSpeedTest() {
        Map<String, String> highwayValues = new HashMap<>();

        String highwayType = "tertiary";
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(80, speedLimit);


        highwayValues.put("maxspeed", "55");
        speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(55, speedLimit);


        highwayValues.clear();
        highwayType = "living_street";
        speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(15, speedLimit);

        highwayValues.clear();
        highwayType = "residential";
        speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(50, speedLimit);

        highwayValues.clear();
        highwayType = "motorway";
        speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(130, speedLimit);

        highwayValues.clear();
        highwayValues.put("DK:rural", "110"); // Tests whether removal of tags are done properly
        highwayType = "motorway";
        speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);
        assertEquals(130, speedLimit);
    }

    /**
     * Tests the boolean isDrivable(String highwayType, Map<String, String> highwayValues) method
     */
    @Test
    public void isDrivable() {
        Map<String, String> highwayValues = new HashMap<>();
        String highwayType = "tertiary";

        highwayValues.put("access", "private");
        assertFalse(decoder.isDrivable(highwayType, highwayValues));

        highwayValues.clear();
        highwayValues.put("motorcar", "no");
        assertFalse(decoder.isDrivable(highwayType, highwayValues));

        highwayValues.clear();
        highwayValues.put("motorcar", "yes");
        assertTrue(decoder.isDrivable(highwayType, highwayValues));

        highwayValues.clear();
        highwayType = "cycleway";
        assertFalse(decoder.isDrivable(highwayType, highwayValues));

        highwayValues.clear();
        highwayType = "pedestrian";
        assertFalse(decoder.isDrivable(highwayType, highwayValues));
    }

    /**
     * Tests the boolean isBikable(String highwayType, Map<String, String> highwayValues)
     */
    @Test
    public void isBikable() {
        Map<String, String> highwayValues = new HashMap<>();
        String highwayType = "tertiary";
        assertTrue(decoder.isBikable(highwayType, highwayValues));

        highwayType = "motorway";
        assertFalse(decoder.isBikable(highwayType, highwayValues));

        highwayType = "residential";
        assertTrue(decoder.isBikable(highwayType, highwayValues));

        highwayType = "residential";
        highwayValues.put("bicycle", "no");
        assertFalse(decoder.isBikable(highwayType, highwayValues));

        highwayValues.clear();
        highwayType = "cycleway";
        highwayValues.put("access", "no");
        assertFalse(decoder.isBikable(highwayType, highwayValues));
    }

    /**
     * Tests the boolean isWalkable(String highwayType, Map<String, String> highwayValues)
     */
    @Test
    public void isWalkable() {
        Map<String, String> highwayValues = new HashMap<>();
        String highwayType = "tertiary";
        assertTrue(decoder.isWalkable(highwayType, highwayValues));

        highwayType = "motorway";
        assertFalse(decoder.isWalkable(highwayType, highwayValues));

        highwayType = "residential";
        assertTrue(decoder.isWalkable(highwayType, highwayValues));

        highwayType = "residential";
        highwayValues.put("foot", "no");
        assertFalse(decoder.isWalkable(highwayType, highwayValues));

        highwayValues.clear();
        highwayType = "cycleway";
        highwayValues.put("access", "no");
        assertFalse(decoder.isWalkable(highwayType, highwayValues));
    }

    /**
     * Tests the boolean isOneWay(Map<String, String> highwayValues) method
     */
    @Test
    public void isOneWay() {
        Map<String, String> highwayValues = new HashMap<>();

        highwayValues.put("oneway", "yes");
        assertTrue(decoder.isOneWay(highwayValues));


        highwayValues.clear();
        highwayValues.put("junction", "roundabout");
        assertTrue(decoder.isOneWay(highwayValues));

        highwayValues.clear();
        highwayValues.put("oneway", "no");
        assertFalse(decoder.isOneWay(highwayValues));
    }


    /**
     * Tests the boolean isRoundabout(Map<String, String> highwayValues) method
     */
    @Test
    public void isRoundabout() {
        Map<String, String> highwayValues = new HashMap<>();

        highwayValues.put("junction", "roundabout");

        assertTrue(decoder.isRoundabout(highwayValues));

        highwayValues.clear();
        assertNotEquals(true, decoder.isRoundabout(highwayValues));
        assertFalse(decoder.isOneWay(highwayValues));
    }



    /**
     * This test depicts the following scenario:
     * <way>
     * <tag k="access" v="private"/>
     * <tag k="highway" v="tertiary"/>
     * <tag k="bicycle" v="yes"/>
     * <tag k="foot" v="no"/>
     * </way>
     */
    @Test
    public void tertiaryPrivateAccessRestrictionTest() {
        String highwayType = "tertiary";
        Map<String, String> highwayValues = new HashMap<>();
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);

        highwayValues.put("access", "private");
        highwayValues.put("bicycle", "yes");
        highwayValues.put("foot", "no");

        assertEquals(80, speedLimit);
        assertTrue(decoder.isBikable(highwayType, highwayValues));
        assertFalse(decoder.isWalkable(highwayType, highwayValues));
        assertFalse(decoder.isDrivable(highwayType, highwayValues));
    }


    /**
     * This test depicts the following scenario:
     * <way>
     * <tag k="access" v="private"/>
     * <tag k="highway" v="tertiary"/>
     * <tag k="bicycle" v="no"/>
     * <tag k="foot" v="yes"/>
     * <tag k="motor_vehicle" v="no"/>
     * </way>
     */
    @Test
    public void tertiaryPrivateAccessRestrictionTest2() {
        String highwayType = "tertiary";

        Map<String, String> highwayValues = new HashMap<>();
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);

        highwayValues.put("access", "private");
        highwayValues.put("bicycle", "no");
        highwayValues.put("foot", "yes");
        highwayValues.put("motor_vehicle", "no");

        assertEquals(80, speedLimit);
        assertFalse(decoder.isBikable(highwayType, highwayValues));
        assertTrue(decoder.isWalkable(highwayType, highwayValues));
        assertFalse(decoder.isDrivable(highwayType, highwayValues));
    }


    /**
     * This test depicts the following scenario:
     * <way>
     * <tag k="access" v="private"/>
     * <tag k="highway" v="tertiary"/>
     * <tag k="bicycle" v="no"/>
     * <tag k="foot" v="yes"/>
     * <tag k="motor_vehicle" v="no"/>
     * </way>
     */
    @Test
    public void tertiaryPrivateAccessRestrictionTest3() {
        String highwayType = "tertiary";

        Map<String, String> highwayValues = new HashMap<>();
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);

        highwayValues.put("access", "private");
        highwayValues.put("motor_vehicle", "yes");

        assertEquals(80, speedLimit);
        assertFalse(decoder.isBikable(highwayType, highwayValues));
        assertFalse(decoder.isWalkable(highwayType, highwayValues));
        assertTrue(decoder.isDrivable(highwayType, highwayValues));
    }



    /**
     * This test depicts the following scenario:
     * <way>
     * <tag k="highway" v="motorway"/>
     * </way>
     */
    @Test
    public void motorwayTest() {
        String highwayType = "motorway";

        Map<String, String> highwayValues = new HashMap<>();
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);

        assertEquals(130, speedLimit);
        assertFalse(decoder.isBikable(highwayType, highwayValues));
        assertFalse(decoder.isWalkable(highwayType, highwayValues));
        assertTrue(decoder.isDrivable(highwayType, highwayValues));
    }
    

    /**
     * This test depicts the following scenario:
     * <way>
     * <tag k="highway" v="residential"/>
     * </way>
     */
    @Test
    public void residentialTest() {
        String highwayType = "residential";

        Map<String, String> highwayValues = new HashMap<>();
        int speedLimit = decoder.getMaxSpeed(highwayType, highwayValues);

        assertEquals(50, speedLimit);
        assertTrue(decoder.isBikable(highwayType, highwayValues));
        assertTrue(decoder.isWalkable(highwayType, highwayValues));
        assertTrue(decoder.isDrivable(highwayType, highwayValues));
    }

}
