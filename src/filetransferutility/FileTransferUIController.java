package filetransferutility;

import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.parser.ParseException;

public class FileTransferUIController {

    //Connections connection = new Connections();
    FileTransfer fileTransfer = new FileTransfer();
    FileChooser fileChooser = new FileChooser();
    File selectedFile;
    Stage controllerStage = new Stage();
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
    public void initialize() {
        serverType.setItems(FXCollections.observableArrayList("Local", "Cloud"));
        serverType.setValue((String) "Local");
        serverType.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
            System.out.println(serverType.getItems().get((Integer) number2));
            fileTransfer.c_serverType = (String) serverType.getItems().get((Integer) number2);
        });
        connectionSelection.setItems(FXCollections.observableArrayList(fileTransfer.connections));
//        connectionSelection.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
//                System.out.println(connectionSelection.getItems().get((Integer) number2));
//                fileTransfer.c_selectedConnection = (String) serverType.getItems().get((Integer) number2);
//            }
//        });
    }

    public void open_file_chooser(ActionEvent e) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        selectedFile = fileChooser.showOpenDialog(controllerStage);
        controllerStage.setTitle("Choose a File");
        sourceFile.setText(selectedFile.toString());
    }

    public void key_file_chooser(ActionEvent e) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        selectedFile = fileChooser.showOpenDialog(controllerStage);
        controllerStage.setTitle("Choose a File");
        keyFileLocation.setText(selectedFile.toString());
    }

    public void add_new_connection(ActionEvent e) {
        selectConnectionRbtn.setToggleGroup(connectionToggleGroup);
        addConnectionRbtn.setToggleGroup(connectionToggleGroup);
        selectConnectionPane.setVisible(false);
        addConnectionPane.setVisible(true);
    }

    public void select_connection(ActionEvent e) {
        selectConnectionRbtn.setToggleGroup(connectionToggleGroup);
        addConnectionRbtn.setToggleGroup(connectionToggleGroup);
        selectConnectionPane.setVisible(true);
        addConnectionPane.setVisible(false);
    }

    public void upload(ActionEvent e) throws IOException, FileNotFoundException, ParseException, JSchException {
        uploadProgressBar.setProgress(0.0);
        FileTransfer upload_file_transfer = new FileTransfer(uploadProgressBar);
        if (addConnectionRbtn.isSelected()) {
            try {
                System.out.println(connectionName.getText());
                upload_file_transfer.add_connection(serverType.getValue().toString(), connectionName.getText(), username.getText(), host.getText(), port.getText(), password.getText(), keyFileLocation.getText());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (selectConnectionRbtn.isSelected()) {
            upload_file_transfer.set_connections(connectionSelection.getValue().toString());
        }
        progressLabel.textProperty().bind(uploadProgressBar.progressProperty().multiply(100).asString("%.2f").concat(" %"));
        upload_file_transfer.connect();
        System.out.println("From UI: " + Thread.currentThread());
        new Thread(() -> {
            upload_file_transfer.upload(sourceFile.getText(), destination.getText());
        }).start();
        System.out.println("From UI: " + Thread.currentThread());

    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

}
