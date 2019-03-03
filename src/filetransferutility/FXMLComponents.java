package filetransferutility;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
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
    Image fileChooserIcon = new Image(FileTransferUI.class.getResourceAsStream("resources/fileOpen.png"));
    Image folderChooserIcon = new Image(FileTransferUI.class.getResourceAsStream("resources/folderOpen.png"));
    //UI components
    @FXML
    Pane pane = new Pane();
    @FXML
    RadioButton selectConnectionRbtn = new RadioButton();
    @FXML
    RadioButton addConnectionRbtn = new RadioButton();
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
    TextField sourceFile = new TextField();
    @FXML
    TextField destination = new TextField();
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
    ProgressBar uploadProgressBar = new ProgressBar();
    @FXML
    Label progressLabel = new Label();
    @FXML
    Label statusMessage = new Label();

}
