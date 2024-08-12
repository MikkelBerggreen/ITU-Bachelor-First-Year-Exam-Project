package Model.AddressParser;

import Model.Model;

import java.util.regex.*;
import Model.SortedAddressArrayList;

/**
 * AddressParser used to parse input addresses
 */
public class AddressParser {

    // MOVE TO MAIN SOMETIME
    private static Model model = Model.getInstance();

    static String regex = "^ *(?<street>[0-9]{0,3}[\\p{L} .\\-]+)? *(?<house>[0-9]{1,3}[A-z]*)? *(?<postcode>[0-9]{1,4})? *(?<city>[\\p{L} .\\-]+)? *$"; // Exception if city is not with capitalized letters.
    // TODO: 16/04/2020 remove commas from the regex

    static Pattern pattern = Pattern.compile(regex);

    /**
     * Parses the input and returns address from the main addresslist from Model
     * @param input String
     * @return Address from OSMAddresses in Model
     */
    public static Address parse(String input) {
        SortedAddressArrayList addresses = model.getOSMAddresses();
        return parse(input, addresses);
    }

    /**
     * Parses the input and returns address with binary search
     * @param input String
     * @param addresses SortedAddressArrayList to return address from
     * @return address from the given list (2nd parameter)
     */
    public static Address parse(String input, SortedAddressArrayList addresses) {
        try {

            input = input.replaceAll(",", "");

            var matcher = pattern.matcher(input);
            Address addr;

            if (matcher.matches()) {
                addr = new Address(upperCaseEveryFirstLetter(matcher.group("street")),
                        upperCaseEveryFirstLetter(matcher.group("house")),
                        upperCaseEveryFirstLetter(matcher.group("postcode")),
                        upperCaseEveryFirstLetter(matcher.group("city")));
                if (addresses.binarySearch(addr) == null) {
                    return addr;
                } else {
                    // Get real OSM address
                    return addresses.binarySearch(addr);
                }
            } else {
                throw new IllegalArgumentException("Cannot parse: " + input);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Changes the city field of the address to the current street and sets street to null
     * @param address the address
     * @return Address with city set as street and street set to null
     */
    public static Address changeCityToStreet(Address address) {
        address.setCity(address.getStreet());
        address.setStreet(null);
        return address;
    }


    /**
     * Upper cases every first letter of the string - first letter is defined as the first letter of the string or letter following a space
     * @param input String
     * @return String with upper cased every first letter
     */
    public static String upperCaseEveryFirstLetter(String input) {
        if (input == null) {
            return input;
        }
        String[] words = input.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }

}