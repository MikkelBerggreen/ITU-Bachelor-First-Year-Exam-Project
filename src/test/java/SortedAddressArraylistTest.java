import Model.AddressParser.Address;
import Model.SortedAddressArrayList;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SortedAddressArraylistTest {

    /**
     * Tests if sortedAddressArrayList are sorted correctly
     */
    @Test
    public void sortTest() {
        SortedAddressArrayList addressList = new SortedAddressArrayList();

        Address testAddress = new Address("Bagervej", "1", "2300", "København");
        addressList.add(testAddress);

        testAddress = new Address("Aagervej", "5", "2300", "København");
        addressList.add(testAddress);

        testAddress = new Address("Aagervej", "1", "2300", "København");
        addressList.add(testAddress);

        testAddress = new Address("Aagervej", "1", "2299", "København");
        addressList.add(testAddress);

        testAddress = new Address("Lagkagevej", "20A", "2300", "København");
        addressList.add(testAddress);

        testAddress = new Address("Lagkagevej", "20", "2300", "København");
        addressList.add(testAddress);
        testAddress = new Address("Lagkagevej", "20", "2300", "København"); // Duplicate address
        addressList.add(testAddress);

        addressList.sortByAddress();


        for (int i = 0; i < addressList.size()-1; i++) {
            int j = i + 1;

            assertTrue((addressList.get(i).getFormattedAddress().compareTo(addressList.get(j).getFormattedAddress()) <= 0));
        }
    }



    /**
     * Tests the add() method
     */
    @Test
    public void addTest() {
        SortedAddressArrayList addrList = new SortedAddressArrayList();

        Address testAddress = new Address("Rued Langgaards Vej", "7", "2300", "København");
        Address testAddress2 = new Address("Langagervej", "25", "2500", "Valby");


        assertEquals(addrList.size(), 0);

        addrList.add(testAddress);
        addrList.add(testAddress2);

        assertEquals(addrList.size(), 2);

        Address testAddress3 = new Address();
        testAddress3.setStreet("Langagervej");
        testAddress3.setHouse("25");
        testAddress3.setPostcode("2500");

        addrList.add(testAddress3);

        assertNotEquals(addrList.size(), 3);
        assertEquals(addrList.size(), 2);
    }


    /**
     * Tests the binarySearch(Address address) method
     */
    @Test
    public void binarySearchTest() {
        SortedAddressArrayList addrList = new SortedAddressArrayList();
        Address testAddress = new Address("Langagervej", "25", "2500", "valby");
        Address testAddress2 = new Address("Hojbovej", "20", "2500", "valby");
        addrList.add(testAddress);
        addrList.add(testAddress2);


        // Search with only street, only street and house number. In even list
        assertEquals(addrList.binarySearch(new Address("Langagervej", null, null, null)), testAddress);
        assertEquals(addrList.binarySearch(new Address("Langagervej", "25", null, null)), testAddress);
        assertNotEquals(addrList.binarySearch(new Address("Langagervej", "20", null, null)), testAddress);


        Address testAddress3 = new Address("Danshojvej", "6", "2500", "valby");
        Address testAddress4 = new Address("Fengersvej", "3A", "2500", "valby");
        Address testAddress5 = new Address("Labyrintvej", "0", "2500", "valby");
        Address testAddress6 = new Address("Labyrintvej", "5", "2600", "valby");
        Address testAddress7 = new Address("Labyrintvej", "10", "2500", "valby");
        Address testAddress8 = new Address("Kongevejen", "20", "3200", "Helsinge");
        Address testAddress9 = new Address("Kagevejen", "25", "2500", "valby");

        addrList.add(testAddress3);
        addrList.add(testAddress4);
        addrList.add(testAddress5);
        addrList.add(testAddress6);
        addrList.add(testAddress7);
        addrList.add(testAddress8);
        addrList.add(testAddress9);

        // Ensures list is of odd length
        assertTrue((addrList.size() == 9));

        assertEquals(addrList.binarySearch(new Address("Labyrintvej", "5", null, null)), testAddress6);
        assertEquals(addrList.binarySearch(new Address("Labyrintvej", "0", null, null)), testAddress5);
    }


    /**
     * Tests recommendAddress(String input, boolean isCitySearch) method
     */
    @Test
    public void recommendedAddressTest() {
        SortedAddressArrayList addrList = new SortedAddressArrayList();

        Address testAddress = new Address("Langagervej", "25", "2500", "valby");
        Address testAddress2 = new Address("Hojbovej", "20", "2500", "valby");
        Address testAddress3 = new Address("Danshojvej", "6", "2500", "valby");
        Address testAddress4 = new Address("Fengersvej", "3A", "2500", "valby");
        Address testAddress5 = new Address("Labyrintvej", "0", "2500", "valby");
        Address testAddress6 = new Address("Labyrintvej", "5", "2600", "valby");
        Address testAddress7 = new Address("Labyrintvej", "10", "2500", "valby");
        Address testAddress8 = new Address("Kongevejen", "20", "3200", "Helsinge");
        Address testAddress9 = new Address("Kagevejen", "25", "2500", "valby");

        addrList.add(testAddress);
        addrList.add(testAddress2);
        addrList.add(testAddress3);
        addrList.add(testAddress4);
        addrList.add(testAddress5);
        addrList.add(testAddress6);
        addrList.add(testAddress7);
        addrList.add(testAddress8);
        addrList.add(testAddress9);


        List<Address> recommended = addrList.recommendedAddresses("Labyrintvej", false);

        List<Address> expectedRecommended = new ArrayList<>();
        expectedRecommended.add(testAddress5);
        expectedRecommended.add(testAddress6);
        expectedRecommended.add(testAddress7);

        assertEquals(expectedRecommended, recommended);
        assertFalse(recommended.contains(testAddress4));


        recommended = addrList.recommendedAddresses("Kagevejen", false);
        expectedRecommended = new ArrayList<>();
        expectedRecommended.add(testAddress9);

        assertEquals(expectedRecommended, recommended);
    }
}
