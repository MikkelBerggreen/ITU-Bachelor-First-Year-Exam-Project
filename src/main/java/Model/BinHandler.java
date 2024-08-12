package Model;

import Model.MapComponents.PointOfInterest;
import Model.Pathfinding.Graph;
import Model.Tree.KDTree;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * responsible for reading the given binary file
 */
public class BinHandler {

    /**
     * Saves core fields of the program as a .bin file
     * The order it is saved in has to correspond to the order it is loaded in as well and vice versa
     * @param filename String
     */
    public static void save(String filename) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

            Model model = Model.getInstance();

            List<Float> bounds = new ArrayList<>();
            bounds.add(model.getMaxLat());
            bounds.add(model.getMaxLon());
            bounds.add(model.getMinLat());
            bounds.add(model.getMinLon());

            out.writeObject(bounds);

            List<SortedAddressArrayList> sortedLists = new ArrayList<>();
            sortedLists.add(model.getOSMAddresses());
            sortedLists.add(model.getOSMCities());

            out.writeObject(sortedLists);

            out.writeObject(model.getGraph());

            out.writeObject(model.getIslands());

            // Highway trees:
            List<KDTree> KDTrees = new ArrayList<>();
            KDTrees.add(model.getHighwayTree());
            KDTrees.add(model.getAreaTree());
            KDTrees.add(model.getWaterTree());
            KDTrees.add(model.getBuildingTree());
            KDTrees.add(model.getMapIconTree());
            KDTrees.add(model.getTertiarywayTree());
            KDTrees.add(model.getPrimarywayTree());
            KDTrees.add(model.getHeathTree());
            KDTrees.add(model.getMeadowTree());
            KDTrees.add(model.getForestTree());
            KDTrees.add(model.getFarmTree());
            KDTrees.add(model.getWaterwayTree());
            KDTrees.add(model.getCityNamesTree());
            KDTrees.add(model.getVillageNamesTree());
            KDTrees.add(model.getParkTree());
            KDTrees.add(model.getRailwayTree());

            out.writeObject(KDTrees);

            out.writeObject(model.getPointsOfInterest());

            out.reset();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads the core fields of the program from .bin file
     * The order it is loaded in has to correspond to the order it is saved in as well and vice versa
     * @param inputStream InputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void load(InputStream inputStream) throws IOException, ClassNotFoundException {
        Model model = Model.getInstance();

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inputStream));

        model.getOSMHandler().initOSMHandler();

        List<Float> bounds = (List<Float>) in.readObject();
        model.getOSMHandler().setMaxLat(bounds.get(0));
        model.getOSMHandler().setMaxLon(bounds.get(1));
        model.getOSMHandler().setMinLat(bounds.get(2));
        model.getOSMHandler().setMinLon(bounds.get(3));

        List<SortedAddressArrayList> sortedLists = (List<SortedAddressArrayList>) in.readObject();
        model.getOSMHandler().setOSMAddresses(sortedLists.get(0));
        model.getOSMHandler().setOSMCities(sortedLists.get(1));

        model.getOSMHandler().setGraph((Graph)  in.readObject());

        model.getOSMHandler().setIslands((List<Drawable>) in.readObject());

        List<KDTree> KDTrees = (List<KDTree>) in.readObject();
        model.getOSMHandler().setKDTrees(KDTrees);

        model.setPointsOfInterest((List<PointOfInterest>) in.readObject());

        in.close();
    }
}
