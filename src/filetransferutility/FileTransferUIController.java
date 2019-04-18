package filetransferutility;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
        localSource.setText(selectedFile.toString());
    }

    public void open_folder_chooser(ActionEvent e) {
        controllerStage.setTitle("Choose a Folder");
        selectedDirectory = directoryChooser.showDialog(controllerStage);
        if (tabPane.getSelectionModel().getSelectedItem().getText().equals("UPLOAD")) {
            localSource.setText(selectedDirectory.toString());
        } else {
            localDestination.setText(selectedDirectory.toString());
        }
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
        FileTransfer upload_file_transfer = initiateTransfer();
        try {
            uploadStatusMessage.setText("Connecting.....");;
        } catch (IllegalStateException ex) {
        }
        try {
            upload_file_transfer.connect();
            uploadStatusMessage.setText("Connected");
            Task task = new Task<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        upload_file_transfer.setProgressUpdate((workDone, totalWork)
                                -> updateProgress(workDone, totalWork));
                        upload_file_transfer.upload(localSource.getText(), serverDestination.getText());
                    } catch (JSchException | IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
            ProgressBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
            System.out.println("From UI: " + Thread.currentThread());
        } catch (JSchException je) {
            uploadStatusMessage.setText(("Unable to connect. Please Check credentials and try again"));
        }
    }

    public void download(ActionEvent e) throws IOException, FileNotFoundException, ParseException {
        FileTransfer download_file_transfer = initiateTransfer();
        downloadStatusMessage.setText("Connecting.....");
        try {
            download_file_transfer.connect();
            downloadStatusMessage.setText("Connected");
            Task task = new Task<Void>() {
                public Void call() throws Exception {
                    try {
                        download_file_transfer.setProgressUpdate((workDone, totalWork)
                                -> updateProgress(workDone, totalWork));
                        downloadStatusMessage.setText("Downloading....");
                        download_file_transfer.download(serverSource.getText(), localDestination.getText());
                    } catch (JSchException | SftpException | IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
            ProgressBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
//            new Thread(() -> {
//                try {
//                    downloadStatusMessage.setText("Downloading....");
//                    download_file_transfer.download(serverSource.getText(), localDestination.getText());
//                } catch (JSchException | SftpException | IOException ex) {
//                    ex.printStackTrace();
//                }
//            }).start();
        } catch (JSchException je) {
            uploadStatusMessage.setText(("Unable to connect. Please Check credentials and try again"));
        }
    }

    public FileTransfer initiateTransfer() throws IOException, FileNotFoundException, ParseException {
        ProgressBar.setProgress(0.0);
        FileTransfer file_transfer = new FileTransfer();
        if (addConnectionRbtn.isSelected()) {
            try {
                System.out.println(connectionName.getText());
                file_transfer.add_connection(serverType.getValue().toString(), connectionName.getText(), username.getText(), host.getText(), port.getText(), password.getText(), keyFileLocation.getText());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (selectConnectionRbtn.isSelected()) {
            file_transfer.set_connections(connectionSelection.getValue().toString());
        }
        progressLabel.textProperty().bind(ProgressBar.progressProperty().multiply(100).asString("%.2f").concat(" %"));
        return file_transfer;
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
        ProgressBar.setProgress(0.0);
    }
}
