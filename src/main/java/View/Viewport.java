package View;


import Model.Model;
import javafx.scene.transform.Affine;
import java.util.ArrayList;
import java.util.List;

/**
 * Viewport class responsible for calculating the rectangle the viewport is showing
 * as well as mouse coordinates to lon and lat
 */
public class Viewport {
    private List<Double> bounds;

    /**
     * Empty constructor of viewport
     */
    public Viewport() {
    }

    /**
     * Updates the viewport bounds
     * @param gc MapCanvas
     * @param trans Trans
     */
    public void update(MapCanvas gc, Affine trans) {

        double zoom = trans.getMyy();

        double x = (trans.getTx() / zoom);
        double y = (trans.getTy() / zoom);

        double width  = gc.getWidth() / zoom;
        double height = gc.getHeight() / zoom;

        bounds = new ArrayList<>();
        bounds.add(x);
        bounds.add(y);

        bounds.add(width);
        bounds.add(height);
    }

    /**
     * Returns the viewport rectangle as a float[] where the rect is 10% larger on all sides to make up for RectSearch lack of precision
     * @return Returns the viewport rectangle as a float[] array consisting of [x, y, w, h]
     */
    public float[] getRect() {
        float[] viewport = new float[4];
        double x = getX() + getWidth() * 0.1;
        double y = getY() + getHeight() * 0.1;
        double w = x - getWidth() * 1.2;
        double h = y - getHeight() * 1.2;

        // Makes rect smaller if we should illustrate KD trees
        if (Model.getInstance().shouldDrawKDTreeIllustration()) {
            // The smaller box to draw within used for illustrations
            x = getX() - getWidth() * 0.35;
            y = getY() - getHeight() * 0.35;
            w = getX() - getWidth() * 0.65;
            h = getY() - getHeight() * 0.65;
        }

        viewport[0] = (float)x;
        viewport[1] = (float)y;

        viewport[2] = (float)w;
        viewport[3] = (float)h;

        return viewport;
    }

    /**
     * Calculates lon based on mouse coordinates
     * @param x double
     * @param gc MapCanvas
     * @return returns lon * 0.56f
     */
    public double mouseCoordToLon(double x, MapCanvas gc) {
        Affine trans = gc.getTrans();
        double zoom = trans.getMyy();
        double x1 = (trans.getTx() / zoom);
        double width  = (gc.getWidth() - (gc.getWidth() - x)) / zoom;

        return x1 - width;
    }

    /**
     * Calculates lat based on mouse coordinates
     * @param y double
     * @param gc MapCanvas
     * @return returns -lat
     */
    public double mouseCoordToLat(double y, MapCanvas gc) {
        Affine trans = gc.getTrans();
        double zoom = trans.getMyy();
        double y1 = (trans.getTy() / zoom);
        double height  = (gc.getHeight() - (gc.getHeight() - y)) / zoom;

        return y1 - height;
    }

    /**
     * Returns x of the left side of the viewport bounds
     * @return double
     */
    public double getX() {
        return -bounds.get(0);
    }

    /**
     * returns y of the left side of the viewport bounds
     * @return double
     */
    public double getY() {
        return -bounds.get(1);
    }

    /**
     * returns the width of the viewport bounds
     * @return double
     */
    public double getWidth() {
        return -bounds.get(2);
    }

    /**
     * returns the height of the viewport bounds
     * @return double
     */
    public double getHeight() {
        return -bounds.get(3);
    }
}