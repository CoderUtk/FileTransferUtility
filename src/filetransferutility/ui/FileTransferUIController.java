package filetransferutility.ui;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import filetransferutility.client.FileTransfer;
import filetransferutility.main.FTConstants;
import filetransferutility.main.Messages;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;

public class FileTransferUIController extends FXMLComponents {

    FileTransfer fileTransfer = new FileTransfer();
    File selectedFile;
    File selectedDirectory;

    @FXML
    @SuppressWarnings("unchecked")
    public void initialize() {
        sourceFileChooser.setGraphic(new ImageView(fileChooserIcon));
        sourceFolderChooser.setGraphic(new ImageView(folderChooserIcon));
        serverType.setItems(FXCollections.observableArrayList(
                FTConstants.PASSWORD_BASED,
                FTConstants.KEY_BASED
        ));
        serverType.setValue(FTConstants.PASSWORD_BASED);
        serverType.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
            System.out.println(serverType.getItems().get((Integer) number2));
            fileTransfer.setAuthType((String) serverType.getItems().get((Integer) number2));
            if (fileTransfer.getAuthType().equals(FTConstants.PASSWORD_BASED)) {
                password.setPromptText(FTConstants.PASSWORD);
                keyFileLocation.setVisible(false);
                keyFileChooser.setVisible(false);
            } else if (fileTransfer.getAuthType().equals(FTConstants.KEY_BASED)) {
                password.setPromptText(FTConstants.PASSPHRASE);
                keyFileLocation.setVisible(true);
                keyFileChooser.setVisible(true);
            }
        });
        connectionSelection.setItems(FXCollections.observableArrayList(fileTransfer.getConnectionList()));
    }

    public void openFileChooser(ActionEvent event) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        selectedFile = fileChooser.showOpenDialog(controllerStage);
        controllerStage.setTitle(Messages.CHOOSE_FILE_MSG);
        localSource.setText(selectedFile.toString());
    }

    public void openFolderChooser(ActionEvent event) {
        controllerStage.setTitle(Messages.CHOOSE_FOLDER_MSG);
        selectedDirectory = directoryChooser.showDialog(controllerStage);
        if (tabPane.getSelectionModel().getSelectedItem().getText().equals(FTConstants.UPLOAD)) {
            localSource.setText(selectedDirectory.toString());
        } else {
            localDestination.setText(selectedDirectory.toString());
        }
    }

    public void chooseKeyFile(ActionEvent event) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"));
        selectedFile = fileChooser.showOpenDialog(controllerStage);
        controllerStage.setTitle(Messages.CHOOSE_FILE_MSG);
        keyFileLocation.setText(selectedFile.toString());
    }

    public void addNewConnection(ActionEvent event) {
        selectConnectionRBtn.setToggleGroup(connectionToggleGroup);
        addConnectionRBtn.setToggleGroup(connectionToggleGroup);
        selectConnectionPane.setVisible(false);
        addConnectionPane.setVisible(true);
    }

    public void selectConnection(ActionEvent event) {
        selectConnectionRBtn.setToggleGroup(connectionToggleGroup);
        addConnectionRBtn.setToggleGroup(connectionToggleGroup);
        selectConnectionPane.setVisible(true);
        addConnectionPane.setVisible(false);
    }

    public void upload(ActionEvent event) throws IOException, ParseException {
        FileTransfer uploadObj = initiateTransfer();
        try {
            uploadStatusMessage.setText(Messages.CONNECTING_MSG);
        } catch (IllegalStateException ex) {
        }
        try {
            uploadObj.connect();
            uploadStatusMessage.setText(Messages.CONNECTED_MSG);
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        uploadObj.setProgressUpdate((workDone, totalWork)
                                -> updateProgress(workDone, totalWork));
                        uploadObj.upload(localSource.getText(), serverDestination.getText());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
            progressBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
            System.out.println("From UI: " + Thread.currentThread());
        } catch (JSchException je) {
            uploadStatusMessage.setText((Messages.CONNECTION_FAIL_MSG));
        }
    }

    public void download(ActionEvent event) throws IOException, ParseException {
        FileTransfer downloadObj = initiateTransfer();
        downloadStatusMessage.setText(Messages.CONNECTING_MSG);
        try {
            downloadObj.connect();
            downloadStatusMessage.setText(Messages.CONNECTED_MSG);
            Task<Void> task = new Task<Void>() {
                public Void call() throws Exception {
                    try {
                        downloadObj.setProgressUpdate((workDone, totalWork)
                                -> updateProgress(workDone, totalWork));
                        downloadStatusMessage.setText(Messages.DOWNLOADING_MSG);
                        downloadObj.download(serverSource.getText(), localDestination.getText());
                    } catch (JSchException | SftpException | IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
            progressBar.progressProperty().bind(task.progressProperty());
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
            uploadStatusMessage.setText((Messages.CONNECTION_FAIL_MSG));
        }
    }

    public FileTransfer initiateTransfer() throws IOException, ParseException {
        FileTransfer transferObj = new FileTransfer();
        if (addConnectionRBtn.isSelected()) {
            try {
                System.out.println(connectionName.getText());
                transferObj.addConnection(serverType.getValue().toString(), connectionName.getText(), username.getText(), host.getText(), port.getText(), password.getText(), keyFileLocation.getText());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (selectConnectionRBtn.isSelected()) {
            transferObj.setConnections(connectionSelection.getValue().toString());
        }
        progressLabel.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%.2f").concat(" %"));
        return transferObj;
    }

    public void testConnection(ActionEvent event) {
        connectionStatus.setText(Messages.CONNECTING_MSG);
        try {
            FileTransfer connectionObj = initiateTransfer();
            connectionObj.connect();
            connectionStatus.setTextFill(Color.web(FTConstants.GREEN_COLOR));
            connectionStatus.setText(Messages.SUCCESS_MSG);
        } catch (Exception e) {
            connectionStatus.setTextFill(Color.web(FTConstants.RED_COLOR));
            if (e instanceof NullPointerException || e == null) {
                connectionStatus.setText(Messages.FAILURE_MSG);
            } else
                connectionStatus.setText(e.getMessage());
        }
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
        progressBar.setProgress(0.0);
    }
}
