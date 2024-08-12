import Model.AddressParser.Address;
import Model.AddressParser.AddressParser;
import Model.SortedAddressArrayList;
import org.junit.Test;


import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddressParserTest {

    SortedAddressArrayList addrList = new SortedAddressArrayList();

    /**
     * Tests different input cases for address parser
     * Creates new addresses identical to input and checks whether the formattedAddress passes the AddressParser by checking if it's same object
     */
    @Test
    public void inputParseTest() {
        // Regular address
        Address testAddress = new Address("Langagervej", "25", "2500", "Valby");
        insertToAddressList(testAddress);
        Address tmpAddr = AddressParser.parse(testAddress.getFormattedAddress(), getAddrList());
        assertEquals(testAddress, tmpAddr); // Tests whether the objects are equals


        // Fake address
        String customFormattedAddress = "Langagervej 25, 2501 Valby";
        Address falseAddr = AddressParser.parse(customFormattedAddress, getAddrList());
        assertNotEquals(testAddress, falseAddr); // Test that the object returned is not same as the created


        // Regular address
        Address testAddress1 = new Address("Langdalen", "11", "8305", "Samso");
        insertToAddressList(testAddress1);

        // Tests only partial input
        Address compareAddress = AddressParser.parse("Langdalen", getAddrList());
        assertEquals(testAddress1, compareAddress);

        compareAddress = AddressParser.parse("Langdalen 11", getAddrList());
        assertEquals(testAddress1, compareAddress);

        // With comma
        compareAddress = AddressParser.parse("Langdalen 11, 8305", getAddrList());
        assertEquals(testAddress1, compareAddress);

        // Without comma
        compareAddress = AddressParser.parse("Langdalen 11 8305", getAddrList());
        assertEquals(testAddress1, compareAddress);
    }


    /**
     * Tests the changeCityToStreet() method of AddressParser which sets city to the current value of street and sets street to null
     */
    @Test
    public void changeCityToStreetTest() {
        Address testAddress = new Address("Langdalen", "11", "8305", "Samso");

        AddressParser.changeCityToStreet(testAddress);

        assertEquals(testAddress.getCity(), "Langdalen");
        assertNotEquals(testAddress.getStreet(), "Langdalen");
        assertNull(testAddress.getStreet());
    }


    /**
     * Tests the method upperCaseEveryFirstLetter() of AddressParser which upper cases every first letter in a string
     */
    @Test
    public void upperCaseEveryFirstLetterTest() {
        Address testAddress = new Address("langdalen paa bryggen", "11", "8305", "samso");

        String formattedUpperCased = AddressParser.upperCaseEveryFirstLetter(testAddress.getFormattedAddress());

        assertEquals("Langdalen Paa Bryggen 11, 8305 Samso", formattedUpperCased);
        assertEquals(AddressParser.upperCaseEveryFirstLetter(testAddress.getStreet()), "Langdalen Paa Bryggen");
        assertEquals(AddressParser.upperCaseEveryFirstLetter(testAddress.getCity()), "Samso");
        assertNotEquals(AddressParser.upperCaseEveryFirstLetter(testAddress.getStreet()), testAddress.getStreet());
    }

    


    private void insertToAddressList(Address addr) {
        addrList.add(addr);
    }
    private SortedAddressArrayList getAddrList() {
        return addrList;
    }
}
