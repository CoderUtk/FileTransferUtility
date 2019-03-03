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
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.json.simple.parser.ParseException;

public class FileTransferUIController extends FXMLComponents {

    //Connections connection = new Connections();
    FileTransfer fileTransfer = new FileTransfer();
    File selectedFile;
    File selectedDirectory;

    @FXML
    @SuppressWarnings("unchecked")
    public void initialize() {
        sourceFileChooser.setGraphic(new ImageView(fileChooserIcon));
        sourceFolderChooser.setGraphic(new ImageView(folderChooserIcon));
        serverType.setItems(FXCollections.observableArrayList("Local", "Cloud"));
        serverType.setValue("Local");
        serverType.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
            System.out.println(serverType.getItems().get((Integer) number2));
            fileTransfer.c_serverType = (String) serverType.getItems().get((Integer) number2);
            switch (fileTransfer.c_serverType) {
                case "Local":
                    password.setPromptText("Password");
                    keyFileLocation.setVisible(false);
                    keyFileChooser.setVisible(false);
                    break;
                case "Cloud":
                    password.setPromptText("Passphrase");
                    keyFileLocation.setVisible(true);
                    keyFileChooser.setVisible(true);
                    break;
            }
        });
        connectionSelection.setItems(FXCollections.observableArrayList(fileTransfer.connections));
    }

    public void open_file_chooser(ActionEvent e) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        selectedFile = fileChooser.showOpenDialog(controllerStage);
        controllerStage.setTitle("Choose a File");
        sourceFile.setText(selectedFile.toString());
    }

    public void open_folder_chooser(ActionEvent e) {
        selectedDirectory = directoryChooser.showDialog(controllerStage);
        controllerStage.setTitle("Choose a Folder");
        sourceFile.setText(selectedDirectory.toString());
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
        statusMessage.setText("Connecting.....");
        try {
            upload_file_transfer.connect();
            statusMessage.setText("Connected");
            //System.out.println("From UI: " + Thread.currentThread());
            new Thread(() -> {
                try {
                    upload_file_transfer.upload(sourceFile.getText(), destination.getText());
                } catch (IOException | JSchException ex) {
                    ex.printStackTrace();
                }
            }).start();
            System.out.println("From UI: " + Thread.currentThread());
        } catch (JSchException je) {
            statusMessage.setText(("Unable to connect. Please Check credentials and try again"));
        }
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

}
