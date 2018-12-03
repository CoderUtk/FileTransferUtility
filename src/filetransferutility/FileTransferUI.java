package filetransferutility;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FileTransferUI extends Application {

    JSONParser parser = new JSONParser();
    JSONObject json_object;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FileTransferUI.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("File Transfer Utility");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
