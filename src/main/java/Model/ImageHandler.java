package Model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

/**
 * Handles distributing images to be drawn on canvas to ensure an image is reused when drawn again instead of being created again
 * Singleton design pattern is implemented to ensure the same images can be reused everywhere
 */
public class ImageHandler {

    // Image url id to image
    private Map<Type, Image> typeToImage;

    private static ImageHandler imageHandler;


    private ImageHandler() {
        typeToImage = new HashMap<>();
    }

    /**
     * Creates new instance of ImageHandler if no instance has been instantiated yet
     * Otherwise returns the already instantiated instance of ImageHandler
     * @return ImageHandler
     */
    public static ImageHandler getInstance() {
        if (imageHandler == null) {
            imageHandler = new ImageHandler();
        }

        return imageHandler;
    }


    private boolean checkImage(Type type) {
        // Check if image is already loaded, if not then load
        if (!typeToImage.containsKey(type)) {
            URL url = getClass().getClassLoader().getResource("MapIcons/" + type.name().toLowerCase() + ".png");

            if (url != null) {
                String strUrl = url.toString();
                Image image = new Image(strUrl);
                typeToImage.put(type, image);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the image associated with the given Type
     * @param type Type
     * @return Image
     */
    public Image getImage(Type type) {
        if (checkImage(type)) {
            return typeToImage.get(type);
        } else {
            return null;
        }
    }
}
