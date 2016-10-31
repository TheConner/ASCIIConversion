package ASCIIConvert;

//TODO: Clean up import statements
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class Converter extends Application {
    private int fontSize = 1;
    private boolean invert = false;
    private Point scrollP = new Point(0, 0);
    // Theres a reason for this, I'm sure there is
    private JTextArea jtextArea;
    private JScrollPane sp;
    private ASCIIConversions convert = new ASCIIConversions();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BufferedImage image = getImageFile(primaryStage);

        VBox vb = new VBox(10);
        HBox hb = new HBox(10);
        final SwingNode textArea = new SwingNode();
        createSwingJTextArea(textArea, image);

        StackPane sPane = new StackPane();
        sPane.getChildren().add(textArea);

        // Needed for use inside the event handlers for the buttons

        Button bZoomIn = new Button("Increase Font");
        bZoomIn.setOnAction(e -> {
            fontSize += 2;
            updateJTextArea();
        });

        Button bZoomOut = new Button("Decrease Font");
        bZoomOut.setOnAction(e -> {
            fontSize -= (fontSize - 2) >= 1 ? 2 : 0;
            updateJTextArea();
        });

        Button bInvert = new Button("Invert Grayscale");
        bInvert.setOnAction(e -> {
            invert = !invert;
            jtextArea.setText(convert.convert(image, invert));
        });

        Separator s = new Separator(Orientation.VERTICAL);

        Button high = new Button("Highlight");
        high.setOnAction(e -> launchHighLightDialog(primaryStage));

        Button bStats = new Button("Show Statistics");
        bStats.setOnAction(e -> {
            Locale locale = new Locale("en", "EN");
            NumberFormat numberFormat = NumberFormat.getInstance(locale);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setTitle("Conversion Output");
            alert.setHeaderText("Renderer Statistics");
            alert.setContentText("Characters used: "
                    + numberFormat.format(image.getHeight() * image.getWidth())
                    + "\nOriginal Image Size " + numberFormat.format(image.getWidth()) + "x"
                    + numberFormat.format(image.getHeight())
                    + "\n\n" + convert.getStats());
            alert.showAndWait();
        });

        hb.getChildren().addAll(bZoomIn, bZoomOut, bInvert, high, s, bStats);
        vb.getChildren().add(hb);
        vb.getChildren().add(sPane);

        Scene scene = new Scene(vb, image.getWidth(), image.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dickens-O-Matic 3000 Super Ultra");
        primaryStage.show();
    }

    private void launchHighLightDialog(Stage s) {
        final Stage dialog = new Stage();
        dialog.setTitle("Choose Highlight Options");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(s);
        VBox fp = new VBox();
        GridPane dialogRoot = new GridPane();
        dialogRoot.setHgap(10);
        dialogRoot.setVgap(10);
        Label[] refSel = new Label[convert.convRefArray.length];
        for(int i = 0; i < convert.convRefArray.length; i++) {
            refSel[i] = new Label("Character: " + convert.convRefArray[i]);
        }

        ColorPicker cb[] = new ColorPicker[convert.convRefArray.length];
        for (int i = 0; i < convert.convRefArray.length; i++) {
            cb[i] = new ColorPicker();
            int finalI = i;
            cb[i].setOnAction(e -> doHighLight(convert.convRefArray[finalI], new Color(
                    (float) cb[finalI].getValue().getRed(),
                    (float) cb[finalI].getValue().getGreen(),
                    (float) cb[finalI].getValue().getBlue(),
                    (float) cb[finalI].getValue().getOpacity())));
            dialogRoot.add(cb[i], 1, i);
        }
        for (int i = 0; i < convert.convRefArray.length; i++) {
            dialogRoot.add(refSel[i], 0, i);
        }
        Button bClear = new Button("Clear All");
        bClear.setOnAction(e -> removeHighlight());

        fp.getChildren().addAll(dialogRoot, bClear);
        Scene dialogScene = new Scene(fp);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void removeHighlight() {
        jtextArea.getHighlighter().removeAllHighlights();
    }

    private void doHighLight(char c, Color cl) {
        SwingUtilities.invokeLater(() -> {
            Highlighter hl1 = jtextArea.getHighlighter();
            Highlighter.HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(cl);
            for (int i = 0; i < convert.imgArray.length; i++) {
                try {
                    if (convert.imgArray[i] == c) {
                        hl1.addHighlight(i, i + 1, hp);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private BufferedImage getImageFile(Stage s) {
        AtomicReference<File> f = new AtomicReference<>();
        BufferedImage image = null;
        while (image == null) {
            f.set(getInitialFile(s));
            try {
                image = ImageIO.read(f.get());
            } catch (Exception e) {
                System.exit(0);
            }
        }
        return image;
    }

    private void createSwingJTextArea(final SwingNode swingNode, BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            jtextArea = new JTextArea(image.getHeight() / 2, image.getWidth());
            jtextArea.setText(convert.convert(image, invert));
            jtextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, fontSize));
            jtextArea.setEditable(true);
            sp = new JScrollPane(jtextArea);
            sp.getViewport().addChangeListener(l -> scrollP = sp.getViewport().getViewPosition());
            swingNode.setContent(sp);
        });
    }

    private void updateJTextArea() {
        jtextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, fontSize));
        sp.getViewport().setViewPosition(scrollP);
    }

    private File getInitialFile(Stage stage) {
        File f;
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image to Convert");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("GIF", "*.gif")
            );
            f = fileChooser.showOpenDialog(stage);
            return f;
        } catch (Exception e) {
            showAngerDialog(
                    new String[]{"Error", "Wrong image type", "Only JPG PNG and GIF are supported"});
        }
        return null;
    }

    private void showAngerDialog(String s[]) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(s[0]);
        alert.setHeaderText(s[1]);
        alert.setContentText(s[2]);
        alert.showAndWait();
    }
}
