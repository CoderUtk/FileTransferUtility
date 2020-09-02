package filetransferutility.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FileTransferClient extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(FileTransferClient.class.getResource(FTConstants.FXML_PATH));
            Scene scene = new Scene(root);

            primaryStage.getIcons().add(new Image(FileTransferClient.class.getResourceAsStream(FTConstants.ICON_PATH)));
            primaryStage.setTitle(FTConstants.TITLE);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println(FTConstants.END_TITLE);
        System.exit(0);
    }
}
