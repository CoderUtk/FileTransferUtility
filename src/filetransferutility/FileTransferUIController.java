package filetransferutility;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class FileTransferUIController {

    Connections connection = new Connections();
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
    public void initialize() {
        serverType.setItems(FXCollections.observableArrayList("Local", "Cloud"));
        serverType.setValue((String) "Local");
        serverType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                System.out.println(serverType.getItems().get((Integer) number2));
                connection.c_serverType = (String) serverType.getItems().get((Integer) number2);
            }
        });
        connectionSelection.setItems(FXCollections.observableArrayList(connection.connections));
        connectionSelection.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                System.out.println(serverType.getItems().get((Integer) number2));
                connection.c_serverType = (String) serverType.getItems().get((Integer) number2);
            }
        });
    }


}
