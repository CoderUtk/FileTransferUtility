package filetransferutility.ui;

import filetransferutility.main.FileTransferClient;
import filetransferutility.main.FTConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLComponents {

    //FX components
    FileChooser fileChooser = new FileChooser();
    DirectoryChooser directoryChooser = new DirectoryChooser();
    Stage controllerStage = new Stage();
    Image fileChooserIcon = new Image(FileTransferClient.class.getResourceAsStream(FTConstants.FILE_CHOOSER_ICON));
    Image folderChooserIcon = new Image(FileTransferClient.class.getResourceAsStream(FTConstants.FOLDER_CHOOSER_ICON));
    //UI components
    @FXML
    Pane pane = new Pane();
    @FXML
    RadioButton selectConnectionRBtn = new RadioButton();
    @FXML
    RadioButton addConnectionRBtn = new RadioButton();
    @FXML
    TextField connectionName = new TextField();
    @FXML
    TextField username = new TextField();
    @FXML
    TextField host = new TextField();
    @FXML
    TextField port = new TextField();
    @FXML
    TextField keyFileLocation = new TextField();
    @FXML
    TextField localSource = new TextField();
    @FXML
    TextField serverDestination = new TextField();
    @FXML
    PasswordField password = new PasswordField();
    @FXML
    Button sourceFileChooser = new Button();
    @FXML
    Button sourceFolderChooser = new Button();
    @FXML
    Button keyFileChooser = new Button();
    @FXML
    Button uploadBtn = new Button();
    @FXML
    Button downloadBtn = new Button();
    @FXML
    ChoiceBox serverType = new ChoiceBox();
    @FXML
    ChoiceBox connectionSelection = new ChoiceBox();
    @FXML
    ToggleGroup connectionToggleGroup = new ToggleGroup();
    @FXML
    Pane selectConnectionPane = new Pane();
    @FXML
    Pane addConnectionPane = new Pane();
    @FXML
    ProgressBar progressBar = new ProgressBar();
    @FXML
    Label progressLabel = new Label();
    @FXML
    Label uploadStatusMessage = new Label();
    @FXML
    Label downloadStatusMessage = new Label();
    @FXML
    TextField localDestination = new TextField();
    @FXML
    TextField serverSource = new TextField();
    @FXML
    Button destinationFolderChooser = new Button();
    @FXML
    TabPane tabPane = new TabPane();
}
