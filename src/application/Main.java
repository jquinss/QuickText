package application;
	
import controllers.QuickTextController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("..\\view\\QuickText.fxml"));
			primaryStage.setTitle("QuickText");
			primaryStage.getIcons().add(new Image("/images/logo.png"));
			VBox root = (VBox) fxmlLoader.load();
			final QuickTextController controller = fxmlLoader.getController();
			controller.setStage(primaryStage);
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("..\\styles\\application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
