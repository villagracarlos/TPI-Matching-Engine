package ElementosInterfaz;
//Principal clase para probar la interfaz definida en vista_simulador.fxml
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //Visualizar la interfaz definida en vista_simulador.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ElementosInterfaz/vista_simulador.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            stage.setTitle("Uver");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}