package transformer;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import javafx.util.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
public class Screen1Controller implements Initializable, ControlledScreen {

    private static final Logger logger = LogManager.getLogger(Screen1Controller.class);
    @FXML
    private ImageView imageView;
    @FXML
    private MenuBar menuBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label labelForRGB;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private BufferedImage bufferedImage;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider slider;
    @FXML
    private BarChart barChart1;
    @FXML
    private BarChart barChart2;
    @FXML
    private BarChart barChart3;
    @FXML
    private BarChart barChart4;

    private ScreensController myController;

    private long getSumOfPixelValueSquares() {
        long sum = 0;
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        for (int i = 0; i < imageView.getImage().getWidth(); i++) {
            for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                int red = (int) Math.round(255 * colorReaded.getRed());
                sum += red * red;
            }
        }
        return sum;
    }

    private double getAveragePixelValue() {
        int sum = 0;
        int count = 0;
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        for (int i = 0; i < imageView.getImage().getWidth(); i++) {
            for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                int red = (int) Math.round(255 * colorReaded.getRed());
                sum += red;
                count++;
            }
        }
        return sum / count;
    }

    private ImageView getImageView() {
        AnchorPane anchorPane = (AnchorPane) myController.getScreens().get("menu");
        anchorPane.getChildren();
        ScrollPane scrollPane = null;
        for (Node node : anchorPane.getChildren()) {
            if (node instanceof ScrollPane) {
                scrollPane = (ScrollPane) node;
            }
        }
        if (scrollPane.getContent() != null) {
            return (ImageView) scrollPane.getContent();
        } else {
            throw new NullPointerException();
        }

    }

    @FXML
    private void handleOpenFile(ActionEvent event) throws IOException {
        if (slider != null) {
            slider.setValue(1);
        }
        if ((imageView.getImage() != null && showNewImageAlert() == ButtonType.OK) || imageView.getImage() == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select image to open");
            fileChooser.getExtensionFilters().add(createExtensionFilterForImages());
            File chosenFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
            if (chosenFile != null) {
                Image initialImage = new Image(new FileInputStream(chosenFile));
                fitImageViewToImage(initialImage);
                centerLabel();
                imageView.setImage(initialImage);
                registerResizeListenersToStage();
            }
        }
        updateStageOfBufforedImage();
        clearBarCharts();
    }

    private void updateStageOfBufforedImage() {
        this.bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
    }

    @FXML
    private void handleSaveFile(ActionEvent event) throws IOException {
        if (imageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select place where to save image");
            fileChooser.setInitialFileName("image");
            fileChooser.getExtensionFilters().addAll(createExtensionFiltersForSavingImage());
            File file = fileChooser.showSaveDialog(imageView.getScene().getWindow());
            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                String imageFormat = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(2);
                ImageIO.write(image, imageFormat, file);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nothing to save!");
            alert.setHeaderText(null);
            alert.setContentText("There is no image that can be saved!");
            alert.show();
        }
    }

    private List<ExtensionFilter> createExtensionFiltersForSavingImage() {
        List<ExtensionFilter> extensionFilters = new ArrayList<>();
        extensionFilters.add(new ExtensionFilter("*.PNG Files", "*.png"));
        extensionFilters.add(new ExtensionFilter("*.JPG Files", "*.jpg"));
        extensionFilters.add(new ExtensionFilter("*.JPEG Files", "*.jpeg"));
        extensionFilters.add(new ExtensionFilter("*.BMP Files", "*.bmp"));
        extensionFilters.add(new ExtensionFilter("*.WBMP Files", "*.wbmp"));
        return extensionFilters;
    }

    private void registerResizeListenersToStage() {
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.heightProperty().addListener((Observable observable) -> {
            fitNodesToParent(stage);
            centerLabel();
        });
        stage.widthProperty().addListener((Observable observable) -> {
            fitNodesToParent(stage);
            centerLabel();
        });
    }

    private ButtonType showNewImageAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to open new image? Any unsaved changes will be lost!");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }

    private void fitImageViewToImage(Image initialImage) {
        imageView.setFitHeight(initialImage.getHeight());
        imageView.setFitWidth(initialImage.getWidth());
        imageView.setPreserveRatio(true);
    }

    private void centerLabel() {
        labelForRGB.translateXProperty().bind(scrollPane.widthProperty().subtract(labelForRGB.widthProperty()).divide(2));
    }

    private void fitNodesToParent(Stage stage) {
        menuBar.setPrefWidth(stage.getWidth());
        scrollPane.setPrefWidth(stage.getWidth() - 30);
        scrollPane.setPrefHeight(stage.getHeight() - 100);
    }

    @FXML
    private void handleZoom(MouseEvent event) {
        if (this.slider.getValue() > 0) {
            resizeImageBy(this.slider.getValue());
        }
    }

    @FXML
    private void resetImage(ActionEvent event) {
        resizeImageBy(1);
        slider.setValue(1);
    }

    private void resizeImageBy(double resizeValue) {
        if (bufferedImage != null) {
            BufferedImage scaledImg = new BufferedImage((int) (bufferedImage.getWidth() * resizeValue), (int) (bufferedImage.getHeight() * resizeValue), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = scaledImg.createGraphics();
            graphics.drawImage(bufferedImage, 0, 0, (int) (bufferedImage.getWidth() * resizeValue), (int) (bufferedImage.getHeight() * resizeValue), null);
            graphics.dispose();
            imageView.setFitHeight(scaledImg.getHeight());
            imageView.setFitWidth(scaledImg.getWidth());
            imageView.setImage(SwingFXUtils.toFXImage(scaledImg, null));
        }
    }

    private void changeBufforedImageSize(double resizeValue) {
        if (bufferedImage != null) {
            BufferedImage scaledImg = new BufferedImage((int) (bufferedImage.getWidth() * resizeValue), (int) (bufferedImage.getHeight() * resizeValue), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = scaledImg.createGraphics();
            graphics.drawImage(bufferedImage, 0, 0, (int) (bufferedImage.getWidth() * resizeValue), (int) (bufferedImage.getHeight() * resizeValue), null);
            graphics.dispose();
        }
    }


    @FXML
    private void handleGetRGB(MouseEvent event) throws IOException {
        try {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            Color color = new Color(image.getRGB((int) event.getX(), (int) event.getY()));
            labelForRGB.setText("RGB: " + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        } catch (NullPointerException ex) {
            logger.warn("handleGetRGB - no image, ignoring method");
        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.warn("handleGetRGB - trying to get RGB outside image, ignoring method");
        }
    }

    @FXML
    private void handleRGB(MouseEvent event) throws IOException {
        if (event.getButton() == MouseButton.PRIMARY) {
            handleGetRGB(event);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            handleSetRGB(event);
        }
    }

    @FXML
    private void handleSetRGB(MouseEvent event) {
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            try {
                Color newColorForPixel = new java.awt.Color((float) colorPicker.getValue().getRed(),
                        (float) colorPicker.getValue().getGreen(),
                        (float) colorPicker.getValue().getBlue(),
                        (float) colorPicker.getValue().getOpacity());
                image.setRGB((int) event.getX(), (int) event.getY(), newColorForPixel.getRGB());
                imageView.setImage(SwingFXUtils.toFXImage(image, null));
                updateStageOfBufforedImage();
                changeBufforedImageSize(1 / slider.getValue());
                updateStageOfBufforedImage();
                slider.setValue(1);
            } catch (NoSuchElementException ex) {
                logger.info("handleSetRGB - no value specified for RGB, retrying");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setWindowPreferences();
    }

    public void setWindowPreferences() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        menuBar.setPrefWidth(screenBounds.getWidth());
        scrollPane.setPrefWidth(screenBounds.getWidth() - 30);
        scrollPane.setPrefHeight(screenBounds.getHeight() - 100);
    }

    @FXML
    private void mouseDragDropped(DragEvent event) {
        Dragboard dragBoard = event.getDragboard();
        boolean isDropComplete = false;
        if (dragBoard.hasFiles()) {
            isDropComplete = true;
            File draggedFile = dragBoard.getFiles().get(0);
            Platform.runLater(() -> {
                try {
                    if ((imageView.getImage() != null && showNewImageAlert() == ButtonType.OK) || imageView.getImage() == null) {
                        Image initialImage = new Image(new FileInputStream(draggedFile.getAbsolutePath()));
                        fitImageViewToImage(initialImage);
                        centerLabel();
                        registerResizeListenersToStage();
                        imageView.setImage(initialImage);
                    }
                } catch (FileNotFoundException ex) {
                    logger.info("mouseDragDropped - file cannot be found");
                }
            });
        }
        event.setDropCompleted(isDropComplete);
        event.consume();
    }

    @FXML
    private void mouseDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        boolean isFileImage = db.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".bmp")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".wbmp");
        if (db.hasFiles() && isFileImage) {
            e.acceptTransferModes(TransferMode.COPY);
        } else {
            e.consume();
        }
    }

    private ExtensionFilter createExtensionFilterForImages() {
        return new ExtensionFilter("Image files", "*.bmp", "*.jpg", "*.jpeg", "*.png", "*.wbmp");
    }

    @FXML
    private void checkColor(ContextMenuEvent event) {
        javafx.scene.paint.Color color = this.colorPicker.getValue();
        logger.info("Green: " + color.getGreen() + " Red: " + color.getRed() + " Blue: " + color.getBlue());
    }


    @SuppressWarnings("unchecked")
    @FXML
    private void showChart() throws FileNotFoundException, MalformedURLException {
        if (barChart1.getData().isEmpty()) {
            XYChart.Series seriesRed = new XYChart.Series();
            XYChart.Series seriesGreen = new XYChart.Series();
            XYChart.Series seriesBlue = new XYChart.Series();
            XYChart.Series seriesMedium = new XYChart.Series();
            Image image = getImageView().getImage();
            ArrayList<Integer> listOfRedColorRepresentation = new ArrayList<>();
            ArrayList<Integer> listOfGreenColorRepresentation = new ArrayList<>();
            ArrayList<Integer> listOfBlueColorRepresentation = new ArrayList<>();
            ArrayList<Integer> listOfMediumColorRepresentation = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                listOfBlueColorRepresentation.add(0);
                listOfGreenColorRepresentation.add(0);
                listOfRedColorRepresentation.add(0);
                listOfMediumColorRepresentation.add(0);
            }
            PixelReader pixelReader = image.getPixelReader();
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(i, j);
                    //Red
                    int index = (int) Math.round(255 * color.getRed());
                    int number = listOfRedColorRepresentation.get(index);
                    int mediumNumber = listOfMediumColorRepresentation.get(index);
                    number++;
                    listOfRedColorRepresentation.set(index, number);
                    mediumNumber++;
                    listOfMediumColorRepresentation.set(index, mediumNumber);
                    //Green
                    index = (int) Math.round(255 * color.getGreen());
                    number = listOfGreenColorRepresentation.get(index);
                    mediumNumber = listOfMediumColorRepresentation.get(index);
                    number++;
                    listOfGreenColorRepresentation.set(index, number);
                    mediumNumber++;
                    listOfMediumColorRepresentation.set(index, mediumNumber);
                    //Blue
                    index = (int) Math.round(255 * color.getBlue());
                    number = listOfBlueColorRepresentation.get(index);
                    mediumNumber = listOfMediumColorRepresentation.get(index);
                    number++;
                    listOfBlueColorRepresentation.set(index, number);
                    mediumNumber++;
                    listOfMediumColorRepresentation.set(index, mediumNumber);
                }
            }
            for (int i = 0; i < 256; i++) {
                listOfMediumColorRepresentation.set(i, listOfMediumColorRepresentation.get(i) / 3);
            }
            for (int i = 0; i < 256; i++) {
                int value = listOfRedColorRepresentation.get(i);
                seriesRed.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), value));
                value = listOfGreenColorRepresentation.get(i);
                seriesGreen.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), value));
                value = listOfBlueColorRepresentation.get(i);
                seriesBlue.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), value));
                value = listOfMediumColorRepresentation.get(i);
                seriesMedium.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), value));
            }
            barChart1.getData().add(seriesRed);
            barChart2.getData().add(seriesGreen);
            barChart3.getData().add(seriesBlue);
            barChart4.getData().add(seriesMedium);
            System.out.println("showChart");
        }
    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }

    @FXML
    private void showMainWindow(ActionEvent event) {
        myController.setScreen(ScreensFramework.MAIN_SCREEN);
    }

    @FXML
    private void showHistogramWindow(ActionEvent event) {
        myController.setScreen(ScreensFramework.HISTOGRAM_SCREEN);
    }

    @FXML
    private void showHistogram2Window(ActionEvent event) {
        myController.setScreen(ScreensFramework.HISTOGRAM2_SCREEN);
    }


    private void clearBarCharts() {
        AnchorPane anchorPane = (AnchorPane) myController.getScreens().get("histogram2");
        anchorPane.getChildren();
        ScrollPane scrollPane = null;
        for (Node node : anchorPane.getChildren()) {
            if (node instanceof ScrollPane) {
                scrollPane = (ScrollPane) node;
                GridPane gridPane = (GridPane) scrollPane.getContent();
                for (Node nodeIn : gridPane.getChildren()) {
                    BarChart barChart = (BarChart) nodeIn;
                    barChart.getData().clear();
                }
            }
        }
    }

    @FXML
    private void stretchHistogram(ActionEvent event) {
        if (imageView.getImage() != null) {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Pass range values");
            ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            TextField fromTextField = new TextField();
            fromTextField.setPromptText("From");
            TextField toTextField = new TextField();
            toTextField.setPromptText("To");
            grid.add(new Label("From:"), 0, 0);
            grid.add(fromTextField, 1, 0);
            grid.add(new Label("To:"), 0, 1);
            grid.add(toTextField, 1, 1);
            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            loginButton.setDisable(true);
            fromTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.trim().isEmpty());
            });
            toTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.trim().isEmpty());
            });
            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> fromTextField.requestFocus());
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return new Pair<>(fromTextField.getText(), toTextField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();
            final int[] fromValue = new int[1];
            final int[] toValue = new int[1];
            result.ifPresent(usernamePassword -> {
                fromValue[0] = Integer.parseInt(usernamePassword.getKey());
                toValue[0] = Integer.parseInt(usernamePassword.getValue());
            });
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green = (int) Math.round(255 * colorReaded.getGreen());
                        int blue = (int) Math.round(255 * colorReaded.getBlue());
                        int minIntesivity = fromValue[0];
                        int maxIntesivity = toValue[0];
                        if (red < minIntesivity) {
                            red = 0;
                        } else if (red > maxIntesivity) {
                            red = 255;
                        } else {
                            red = (red - minIntesivity) * 255 / (maxIntesivity - minIntesivity);
                        }
                        if (green < minIntesivity) {
                            green = 0;
                        } else if (green > maxIntesivity) {
                            green = 255;
                        } else {
                            green = (green - minIntesivity) * 255 / (maxIntesivity - minIntesivity);
                        }
                        if (blue < minIntesivity) {
                            blue = 0;
                        } else if (blue > maxIntesivity) {
                            blue = 255;
                        } else {
                            blue = (blue - minIntesivity) * 255 / (maxIntesivity - minIntesivity);
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }

    @FXML
    private void decideLightChange(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        if ("lightButton".equals(menuItem.getId())) {
            changeLight(true);
        } else {
            changeLight(false);
        }
    }

    @FXML
    private void decidePreetyGreyScale(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        if ("Preety Grey Scale".equals(menuItem.getId())) {
            grayScaleImage(true);
        } else {
            grayScaleImage(false);
        }
    }

    private void changeLight(boolean light) {
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green = (int) Math.round(255 * colorReaded.getGreen());
                        int blue = (int) Math.round(255 * colorReaded.getBlue());

                        if (!light) {
                            red = (int) (0.0039 * red * red);
                            green = (int) (0.0039 * green * green);
                            blue = (int) (0.0039 * blue * blue);
                        } else {
                            double x = 0.002 * red * red;
                            int r = red + (int) x;
                            x = 0.002 * green * green;
                            int g = green + (int) x;
                            x = 0.002 * blue * blue;
                            int b = blue + (int) x;

                            if (r < 255) {
                                red = r;
                            }
                            if (g < 255) {
                                green = g;
                            }
                            if (b < 255) {
                                blue = b;
                            }
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @FXML
    private void equalizeImage(ActionEvent actionEvent) {
        Image image = getImageView().getImage();
        if (image != null) {
            ArrayList<Integer> listOfRedColorRepresentation = new ArrayList<>();
            ArrayList<Integer> listOfGreenColorRepresentation = new ArrayList<>();
            ArrayList<Integer> listOfBlueColorRepresentation = new ArrayList<>();
            ArrayList<Double> pmfRedValueList = new ArrayList<>();
            ArrayList<Double> pmfGreenValueList = new ArrayList<>();
            ArrayList<Double> pmfBlueValueList = new ArrayList<>();
            ArrayList<Double> cdfRedValueList = new ArrayList<>();
            ArrayList<Double> cdfGreenValueList = new ArrayList<>();
            ArrayList<Double> cdfBlueValueList = new ArrayList<>();
            ArrayList<Integer> newRedLevelValueList = new ArrayList<>();
            ArrayList<Integer> newGreenLevelValueList = new ArrayList<>();
            ArrayList<Integer> newBlueLevelValueList = new ArrayList<>();

            for (int i = 0; i < 256; i++) {
                listOfBlueColorRepresentation.add(0);
                listOfGreenColorRepresentation.add(0);
                listOfRedColorRepresentation.add(0);
            }
            int allPixelCount = 0;
            PixelReader pixelReader = image.getPixelReader();

            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(i, j);
                    int index = (int) Math.round(255 * color.getRed());
                    int number = listOfRedColorRepresentation.get(index);
                    number++;
                    listOfRedColorRepresentation.set(index, number);
                    index = (int) Math.round(255 * color.getGreen());
                    number = listOfGreenColorRepresentation.get(index);
                    number++;
                    listOfGreenColorRepresentation.set(index, number);
                    index = (int) Math.round(255 * color.getBlue());
                    number = listOfBlueColorRepresentation.get(index);
                    number++;
                    listOfBlueColorRepresentation.set(index, number);
                    allPixelCount++;
                }
            }

            for (int i = 0; i < 256; i++) {
                pmfRedValueList.add(Double.valueOf(listOfRedColorRepresentation.get(i)) / ((double) allPixelCount));
                pmfGreenValueList.add(Double.valueOf(listOfGreenColorRepresentation.get(i)) / ((double) allPixelCount));
                pmfBlueValueList.add(Double.valueOf(listOfBlueColorRepresentation.get(i)) / ((double) allPixelCount));
            }

            cdfRedValueList.add(pmfRedValueList.get(0));
            cdfGreenValueList.add(pmfGreenValueList.get(0));
            cdfBlueValueList.add(pmfBlueValueList.get(0));

            for (int i = 1; i < 256; i++) {
                cdfRedValueList.add(pmfRedValueList.get(i) + cdfRedValueList.get(i - 1));
                cdfGreenValueList.add(pmfGreenValueList.get(i) + cdfGreenValueList.get(i - 1));
                cdfBlueValueList.add(pmfBlueValueList.get(i) + cdfBlueValueList.get(i - 1));
            }

            for (int i = 0; i < 256; i++) {
                newRedLevelValueList.add((int) (cdfBlueValueList.get(i) * 255));
                newGreenLevelValueList.add((int) (cdfGreenValueList.get(i) * 255));
                newBlueLevelValueList.add((int) (cdfBlueValueList.get(i) * 255));
            }


            if (imageView.getImage() != null) {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                    for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                        try {
                            javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                            int red = (int) Math.round(255 * colorReaded.getRed());
                            int green = (int) Math.round(255 * colorReaded.getGreen());
                            int blue = (int) Math.round(255 * colorReaded.getBlue());
                            red = newRedLevelValueList.get(red);
                            green = newGreenLevelValueList.get(green);
                            blue = newBlueLevelValueList.get(blue);
                            Color color = new Color(red, green, blue);
                            bufferedImage.setRGB(i, j, color.getRGB());
                        } catch (java.lang.IllegalArgumentException ex) {
                            System.out.println("error");
                            logger.info("handleSetRGB - no value specified for RGB, retrying");
                        }
                    }
                }
                imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                updateStageOfBufforedImage();
                changeBufforedImageSize(1 / slider.getValue());
                updateStageOfBufforedImage();
                slider.setValue(1);
            }
            clearBarCharts();
            System.out.println("showChart");
        }
    }


    private void grayScaleImage(Boolean preety) {
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green = (int) Math.round(255 * colorReaded.getGreen());
                        int blue = (int) Math.round(255 * colorReaded.getBlue());
                        if (!preety) {
                            int grey = (red + green + blue) / 3;
                            red = grey;
                            green = grey;
                            blue = grey;
                            ;
                        } else {
                            int grey = (red + green + blue);
                            red = (int) (0.3 * grey);
                            green = (int) (0.59 * grey);
                            blue = (int) (0.11 * grey);
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }

    @FXML
    private void thresholdImage(ActionEvent event) {
        grayScaleImage(false);
        int borderValue = 0;
        if (imageView.getImage() != null) {
            TextInputDialog dialog = new TextInputDialog("0");
            dialog.setTitle("Thresholding");
            dialog.setHeaderText("Give a threshold value");
            dialog.setContentText("Value:");
            Optional<String> result = dialog.showAndWait();
            try {
                if (result.isPresent()) {
                    borderValue = Integer.parseInt(result.get());
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Wrong value");
                alert.setContentText("Please give correct input value");
                alert.showAndWait();
            }
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green;
                        int blue;


                        if (red > borderValue) {
                            red = 255;
                            green = 255;
                            blue = 255;
                        } else {
                            red = 0;
                            green = 0;
                            blue = 0;
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }


    private ArrayList<Integer> getHistogram() {
        Image image = imageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        ArrayList<Integer> histogram = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            histogram.add(0);
        }
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                javafx.scene.paint.Color color = pixelReader.getColor(i, j);
                int index = (int) Math.round(255 * color.getRed());
                int number = histogram.get(index);
                number++;
                histogram.set(index, number);
            }
        }
        return histogram;
    }

    private int getNumberOfPixelsInImage() {
        int numberOfPixels = 0;
        for (int i = 0; i < imageView.getImage().getWidth(); i++) {
            for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                numberOfPixels++;
            }
        }
        return numberOfPixels;
    }

    @FXML
    private void thresholdImageWithOtsu(ActionEvent event) {
        grayScaleImage(false);
        int borderValue = otsu();
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();

            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green;
                        int blue;
                        if (red > borderValue) {
                            red = 255;
                            green = 255;
                            blue = 255;
                        } else {
                            red = 0;
                            green = 0;
                            blue = 0;
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }

    @FXML
    private void thresholdImageWithNiblack(ActionEvent event) {
        long timeStart = System.currentTimeMillis();
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Pass values");
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField fromTextField = new TextField();
        fromTextField.setPromptText("k");
        TextField toTextField = new TextField();
        toTextField.setPromptText("wideness");
        grid.add(new Label("k:"), 0, 0);
        grid.add(fromTextField, 1, 0);
        grid.add(new Label("wideness:"), 0, 1);
        grid.add(toTextField, 1, 1);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        fromTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        toTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> fromTextField.requestFocus());
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(fromTextField.getText(), toTextField.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        final double[] fromValue = new double[1];
        final int[] toValue = new int[1];
        result.ifPresent(usernamePassword -> {
            fromValue[0] = Double.parseDouble(usernamePassword.getKey());
            toValue[0] = Integer.parseInt(usernamePassword.getValue());
        });
        double k = fromValue[0];
        int wideness = toValue[0];
        int numberOfPixels = getNumberOfPixelsInImage();
        double averagePixelValue = getAveragePixelValue();
        long sumOfPixelValueSquares = getSumOfPixelValueSquares();
        grayScaleImage(false);
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            int wide = (int) imageView.getImage().getWidth();
            int high = (int) imageView.getImage().getHeight();
            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green;
                        int blue;
                        int borderValue = (int) getNiblack(i, j, pixelReader, wideness, k, wide, high);
                        if (red > borderValue) {
                            red = 255;
                            green = 255;
                            blue = 255;
                        } else {
                            red = 0;
                            green = 0;
                            blue = 0;
                        }
                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
        long timeStop = System.currentTimeMillis();
        System.out.println(timeStop - timeStart);
    }


    private int otsu() {
        double p0 = 0;
        double p1 = 0;
        double uT = 0;
        double u0 = 0;
        double u1 = 0;
        double oT = 0;
        double uB = 0;
        double oTconst;
        ArrayList<Double> n = new ArrayList<Double>();
        ArrayList<Integer> histogram = getHistogram();
        for (int i = 0; i < histogram.size(); i++) {
            n.add(0d);
        }
        ArrayList<Double> P = getProbability(histogram);
        for (int t = 0; t < P.size(); t++) {
            p0 = p1 = uT = u0 = u1 = oTconst = uB = oT = 0;
            n.set(t, (double) 0);
            for (int i = 0; i <= t; i++) {
                p0 += P.get(i);
            }
            for (int i = t + 1; i < P.size(); i++) {
                p1 += P.get(i);
            }
            for (int i = 0; i < P.size(); i++) {
                uT += (i * P.get(i));
            }
            for (int i = 0; i <= t; i++) {
                u0 += (i * P.get(i));
            }
            for (int i = t + 1; i < P.size(); i++) {
                u1 += (i * P.get(i));
            }
            oT = 0;
            for (int i = 0; i < P.size(); i++) {
                oTconst = i - uT;
                oT += ((oTconst * oTconst) * P.get(i));
            }
            uB = p0 * p1 * (u0 / p0 - u1 / p1) * (u0 / p0 - u1 / p1);
            n.set(t, uB / oT);
        }
        return getMaxIndex(n);
    }

    private int getMaxIndex(ArrayList<Double> n) {
        double max = 0;
        int index = -1;

        for (double d : n) {
            if (d > max) {
                max = d;
                index++;
            }
        }
        return index;
    }

    private ArrayList<Double> getProbability(ArrayList<Integer> histogram) {
        ArrayList<Double> array = new ArrayList<>();
        for (int i = 0; i < histogram.size(); i++) {
            array.add(0d);
        }
        double sum = getSumOfHistogram(histogram);
        for (int i = 0; i < histogram.size(); i++) {
            array.set(i, (double) histogram.get(i) / sum);
        }
        return array;
    }

    private double getSumOfHistogram(ArrayList<Integer> histogram) {
        double sum = 0;
        for (Integer i : histogram) {
            sum += i;
        }
        return sum;
    }

    private int getNiblack(int i, int j, PixelReader pixelReader, int wideness, double k, int wide, int high) {
        double border = 0;
        int avg = getAvgFromSquare(pixelReader, wideness, i, j, wide, high);
        int deviation = getDeviationFromSquare(pixelReader, wideness, i, j, wide, high);
        border = avg + k * deviation;
        return (int) border;
    }

    private int getDeviationFromSquare(PixelReader pixelReader, int wideness, int i, int j, int wide, int high) {
        int sum = 0;
        int count = 0;
        int g = wideness / 2;
        int mean = getAvgFromSquare(pixelReader, wideness, i, j, wide, high);
        int minusI = i - g;
        int minusJ = j - g;
        int plusI = i + g;
        int plusJ = j + g;
        try {
            for (int x = minusI; x <= plusI; x++) {
                for (int y = minusJ; y <= plusJ; y++) {
                    if (x > 0 && y > 0 && x <= wide && y <= high) {
                        int red = (int) Math.round(255 * pixelReader.getColor(x, y).getRed());
                        count++;
                        sum += Math.pow(red - mean, 2);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return (int) Math.sqrt(sum / count);
    }

    private int getAvgFromSquare(PixelReader pixelReader, int wideness, int i, int j, int wide, int high) {
        int sum = 0;
        int count = 0;
        int g = wideness / 2;
        int minusI = i - g;
        int minusJ = j - g;
        int plusI = i + g;
        int plusJ = j + g;
        try {
            for (int x = minusI; x <= plusI; x++) {
                for (int y = minusJ; y <= plusJ; y++) {
                    if (x > 0 && y > 0 && x <= wide && y <= high) {
                        int red = (int) Math.round(255 * pixelReader.getColor(x, y).getRed());
                        sum += red;
                        count++;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return sum / count;
    }

    @FXML
    private void changeTransitionLogo() {
        if (imageView.getImage() != null) {
            BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            PixelReader pixelReader = imageView.getImage().getPixelReader();

            for (int i = 0; i < imageView.getImage().getWidth(); i++) {
                for (int j = 0; j < imageView.getImage().getHeight(); j++) {
                    try {
                        javafx.scene.paint.Color colorReaded = pixelReader.getColor(i, j);
                        int red = (int) Math.round(255 * colorReaded.getRed());
                        int green = (int) Math.round(255 * colorReaded.getRed());
                        int blue = (int) Math.round(255 * colorReaded.getRed());
                        int alfa = (int) Math.round(255 * colorReaded.getHue());
                        if (alfa!=0 && red==3 && green==3 && blue==3) {
                            red = 255;
                            green = 255;
                            blue = 255;


                            Color color = new Color(red, green, blue);
                            image.setRGB(i, j, color.getRGB());
                        }
                    } catch (java.lang.IllegalArgumentException ex) {
                        System.out.println("error");
                        logger.info("handleSetRGB - no value specified for RGB, retrying");
                    }
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateStageOfBufforedImage();
            changeBufforedImageSize(1 / slider.getValue());
            updateStageOfBufforedImage();
            slider.setValue(1);
        }
    }
}