package transformer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by mironp on 20.03.2017.
 */
public class ScreensFramework extends Application {

    public static final String MAIN_SCREEN = "menu";
    public static final String MAIN_SCREEN_FXML = "/fxml/menu.fxml";
    public static final String HISTOGRAM_SCREEN = "histogram";
    public static final String HISTOGRAM_SCREEN_FXML = "/fxml/histogram.fxml";
    public static final String HISTOGRAM2_SCREEN = "histogram2";
    public static final String HISTOGRAM2_SCREEN_FXML = "/fxml/histogram2.fxml";

    @Override
    public void start(Stage primaryStage) {

        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen(ScreensFramework.MAIN_SCREEN,
                ScreensFramework.MAIN_SCREEN_FXML);
        mainContainer.loadScreen(ScreensFramework.HISTOGRAM_SCREEN,
                ScreensFramework.HISTOGRAM_SCREEN_FXML);
        mainContainer.loadScreen(ScreensFramework.HISTOGRAM2_SCREEN,
                ScreensFramework.HISTOGRAM2_SCREEN_FXML);
        mainContainer.setScreen(ScreensFramework.MAIN_SCREEN);

        Group root = new Group();
        root.getChildren().addAll(mainContainer);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Biometry");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/cat-icon-9.png")));
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.getScene().getStylesheets().add(ScreensFramework.class.getResource("/styles/Theme.css").toExternalForm());
        primaryStage.show();
    }
}