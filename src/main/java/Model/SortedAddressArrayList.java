package Model;

import Model.AddressParser.Address;
import Model.AddressParser.AddressParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Model.AddressParser.AddressParser.upperCaseEveryFirstLetter;


/**
 * Class containing a sorted list of Addresses
 * Supports binary search as well as a neighbor binary search implementation used for address suggestions
 */
public class SortedAddressArrayList implements Serializable {
    private ArrayList<Address> list;
    private boolean isSorted;
    private int size=0;

    /**
     * Constructor for SortedAddressArrayList
     * Initializes an empty ArrayList
     */
    public SortedAddressArrayList() {
        list = new ArrayList<>();
        isSorted = false;
    }

    /**
     * Sorts the list
     */
    public void sortByAddress() {
        if (!isSorted) {
            Collections.sort(list);
            isSorted = true;
        }
    }

    /**
     * Returns an address through binary search
     * @param address Address
     * @return Address
     */
    public Address get(Address address) {
        return binarySearch(address);
    }

    /**
     * Gets an Address from index
     * @param index int
     * @return Address
     */
    public Address get(long index) {
        return list.get((int)index);
    }

    /**
     * Returns an address through binary search
     * @param address Address
     * @return Address or null if no result
     */
    public Address binarySearch(Address address) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Address midElement = list.get(mid);
            int res = address.compareTo(midElement);

            if (res == 0) {
                return midElement;
            }
            if (res > 0) {
                low = mid + 1;
            }
            if (res < 0) {
                high = mid - 1;
            }
        }
        return null;
    }


    /**
     * Returns an list of recommended addresses found through binary search
     * @param input String to search for
     * @param isCitySearch boolean for whether it is city names we're searching for
     * @return List of Address
     */
    public List<Address> recommendedAddresses(String input, boolean isCitySearch) {
        if (input.isEmpty()) {
            return null;
        }

        Address searchAddress = AddressParser.parse(input);

        if (searchAddress == null) {
            return new ArrayList<>();
        }

        if (isCitySearch) {
            // Checks if only city is being searched for
            if (searchAddress.getStreet() != null && searchAddress.getHouse() != null) {
                return new ArrayList<>();
            }

            // When creating new address from parser with searched input, the "supposed" city becomes street. Thus we switch them:
            AddressParser.changeCityToStreet(searchAddress);
        }

        return neighborBinarySearch(input, searchAddress);
    }

    private List<Address> neighborBinarySearch(String input, Address searchAddress) {
        int low = 0;
        int high = list.size() - 1;
        int mid = 0;
        int lastMid = 0;

        if (searchAddress == null) {
            return new ArrayList<>();
        }

        while (low <= high) {
            mid = (low + high) / 2;

            // Make sure that we don't end in an endless loop
            if(mid == lastMid) {
                break;
            }
            lastMid = mid;

            Address midElement = list.get(mid);
            int res = searchAddress.compareTo(midElement);

            if (res == 0) {
                break;
            }
            if (res > 0) {
                low = mid + 1;
            }
            if (res < 0) {
                high = mid - 1;
            }
        }

        List<Address> results = new ArrayList<>();
        for(int i = -20; i < 20; i++) {
            try {
                Address addr = list.get(mid + i);

                // Strips the commas away
                String suggestion = addr.getFormattedAddress().replaceAll(",", "");
                input = input.replaceAll(",", "");
                input = upperCaseEveryFirstLetter(input);


                String[] parts = input.split(" ");
                boolean contains = true;
                for (int j = 0; j < parts.length; j++) {
                    if (!suggestion.contains(parts[j])) {
                        contains = false;
                        break;
                    }
                }

                if (contains) {
                    results.add(addr);
                }
            } catch(Exception ignore) {
            }
        }
        return results;
    }


    /**
     * Returns the size of the list
     * @return int
     */
    public int size() {
        return size;
    }

    /**
     * Adds an Address to the list
     * City has been set as an requirement to be inserted to avoid unsearchable addresses
     * @param component Address
     */
    public void add(Address component) {
        if (component.getCity() != null) {
            list.add(component);
            size++;
        }
    }
}
