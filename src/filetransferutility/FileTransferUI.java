package filetransferutility;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        Parent root = FXMLLoader.load(getClass().getResource("resources\\FileTransferUI.fxml"));
        Scene scene = new Scene(root);
        try{
        primaryStage.getIcons().add(new Image(FileTransferUI.class.getResourceAsStream("resources\\icon.png ")));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        primaryStage.setTitle("File Transfer Utility");
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Closing");
        System.exit(0);
    }
}
