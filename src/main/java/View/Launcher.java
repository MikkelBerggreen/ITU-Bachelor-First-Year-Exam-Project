package View;

import Controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
 * Launcher
 */
public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view.fxml"));
        Scene scene = loader.load();
        Controller controller = loader.getController();

        String stylesheet = getClass().getClassLoader().getResource("css/stylesheet.css").toString();
        scene.getStylesheets().add(stylesheet);


        primaryStage.setScene(scene);
        controller.setStage(primaryStage);
        primaryStage.getIcons().add(new Image(getClass().getResource("/MapIcons/pin.png").toString()));
        primaryStage.setTitle("Danmarkskort");
        primaryStage.show();
    }
}
