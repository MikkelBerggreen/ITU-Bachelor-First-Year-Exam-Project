package Model.AddressParser;

import Model.OSMNode;
import java.io.Serializable;

/**
 * Address containing street, house, postcode and city as well as a OSMNode defining its location
 */
public class Address implements Comparable<Address>, Serializable {
    private String street, house, postcode, city;
    private OSMNode node;

    /**
     * Constructor for address
     * @param street String
     * @param house String
     * @param postcode String
     * @param city String
     */
    public Address(String street, String house, String postcode, String city) {
        this.street = street;
        this.house = house;
        this.postcode = postcode;
        this.city = city;
    }

    /**
     * Empty constructor for address
     */
    public Address() {
    }


    /**
     * Converts house number to float value, by converting chars into ascii values
     * @return float value of house number
     */
    private float convertHouseToValue() {

        if (house == null) {
            return 0;
        }

        String numbers = house.replaceAll("[^0-9]", "");
        String letters = house.replaceAll("[^A-Za-z]", "");
        float num = 0;

        if (!numbers.isEmpty()) {
            num += Integer.parseInt(numbers);
        }

        if (!letters.isEmpty()) {
            String upper = letters.toUpperCase();

            for (int i = 0; i < upper.length(); i++) {
                int letter = upper.charAt(i); // Char and int is freely converted
                letter -= 64;

                float val = letter;
                val /= Math.pow(100, i + 1);

                num += val;
            }
        }
        return num;
    }

    /**
     * CompareTo
     * @param address address to compare with
     * @return a value < 0 if the address to compare with is larger, = 0 if equal, and > 0 if smaller
     */
    @Override
    public int compareTo(Address address) {

        if (this.getStreet() != null && address.getStreet() != null) {
            int compareStreet = this.getStreet().compareToIgnoreCase(address.getStreet());
            if (compareStreet != 0) {
                return compareStreet;
            }
        }

        if (this.getHouse() != null && address.getHouse() != null) {
            float h1 = this.convertHouseToValue();
            float h2 = address.convertHouseToValue();
            int compareHouse = Float.compare(h1, h2);
            if (compareHouse != 0) {
                return compareHouse;
            }
        }
        if (this.getPostcode() != null && address.getPostcode() != null) {
            int comparePostcode = this.getPostcode().compareTo(address.getPostcode());
            if (comparePostcode != 0) {
                return comparePostcode;
            }
        }
        if (this.getCity() != null && address.getCity() != null) {
            int compareCity = this.getCity().compareToIgnoreCase(address.getCity());
            if (compareCity != 0) {
                return compareCity;
            }
        }
        return 0;
    }

    /**
     * Gets the formatted address of the address as a string with comma dividing housenumber and zipcode
     * @return formatted address as string
     */
    public String getFormattedAddress() {
        String addr = "";

        if (street != null && !street.isEmpty()) {
            addr += street;
            addr += " ";
        }
        if (house != null && !house.isEmpty()) {
            addr += house;;
            addr += ", ";
        }
        if (postcode != null && !postcode.isEmpty()) {
            addr += postcode;;
            addr += " ";
        }
        if (city != null && !city.isEmpty()) {
            addr += city;
        }
        return addr;
    }


    /**
     * @return address as string
     */
    public String toString() {
        return street + " " + house + ", " + "\n" + postcode + " " + city;
    }

    /**
     * Setter for street variable
     * @param street String
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Setter for house variable
     * @param house String
     */
    public void setHouse(String house) {
        this.house = house;
    }

    /**
     * Setter for postcode variable
     * @param postcode String
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Setter for city variable
     * @param city String
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Setter for OSMNode variable
     * @param node the OSMNode
     */
    public void setNode(OSMNode node) {
        this.node = node;
    }

    /**
     * Getter for street
     * @return street as string
     */
    public String getStreet() {
        return street;
    }

    /**
     * Getter for house
     * @return house as string
     */
    public String getHouse() {
        return house;
    }

    /**
     * Getter for postcode
     * @return postcode as string
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Getter for city
     * @return city as string
     */
    public String getCity() {
        return city;
    }

    /**
     * Getter for OSMNode
     * @return OSMNode
     */
    public OSMNode getNode() {
        return node;
    }
}
