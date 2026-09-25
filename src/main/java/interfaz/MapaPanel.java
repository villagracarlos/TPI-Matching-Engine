package interfaz;

import grafoDirigido.GrafoDirigido;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// IMPORTS PARA LA ANIMACOIN DEL TAXI
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import recursos.Esquina;

public class MapaPanel extends Pane{
    private ImageView mapaView;
    private Image taxiImg;
    private Image usuarioImg;

    private String[] taxiIds;
    private VBox[] taxiContenedores;
    private int taxiCount;
    private String[] usuarioIds;
    private VBox[] usuarioContenedores;
    private int usuarioCount;

    private SequentialTransition[] animacionesTaxis;
    private int animacionesCount;

    public MapaPanel() {

        taxiIds = new String[1000];
        taxiContenedores = new VBox[1000];
        taxiCount = 0;
        usuarioIds = new String[1000];
        usuarioContenedores = new VBox[1000];
        usuarioCount = 0;

        animacionesTaxis = new SequentialTransition[1000];
        animacionesCount = 0;
        
        // MAPA
        Image mapa = new Image(getClass().getResourceAsStream("/mapas/salta.png"));

        mapaView = new ImageView(mapa);

        mapaView.setFitWidth(1920);
        mapaView.setFitHeight(2078);
        mapaView.setPreserveRatio(false);
        
        this.setPrefSize(mapa.getWidth(),mapa.getHeight());
        
        this.getChildren().add(mapaView);

        // TAXI
        taxiImg = new Image(getClass().getResourceAsStream("/iconos/taxi.png"));

        // USUARIO
        usuarioImg = new Image(getClass().getResourceAsStream("/iconos/man-raising-hand.png"));

    }

    public void agregarTaxi(String id, double x, double y) {

        ImageView taxi = new ImageView(taxiImg);
        taxi.setFitWidth(32);
        taxi.setFitHeight(32);

        Label lbl = new Label(id);
        lbl.setStyle("-fx-background-color: white;" + "-fx-border-color: black;" + "-fx-font-size: 10px;");

        VBox contenedor = new VBox(2);
        contenedor.getChildren().addAll(lbl, taxi);

        contenedor.setLayoutX(x-16);
        //contenedor.setLayoutY(y-16);
        contenedor.setLayoutY(y-35);

        taxiIds[taxiCount] = id;
        taxiContenedores[taxiCount] = contenedor;
        taxiCount++;

        this.getChildren().add(contenedor);
    }

    public void agregarUsuario(String id, double x, double y){
        ImageView usuario = new ImageView(usuarioImg);
        usuario.setFitWidth(32);
        usuario.setFitHeight(32);

        Label lbl = new Label(id);
        lbl.setStyle("-fx-background-color: lightyellow;" + "-fx-border-color: black;" + "-fx-font-size: 10px;");

        VBox contenedor = new VBox(2);
        contenedor.getChildren().addAll(lbl, usuario);

        contenedor.setLayoutX(x-16);
        //contenedor.setLayoutY(y-16);
        contenedor.setLayoutY(y-35);

        usuarioIds[usuarioCount] = id;
        usuarioContenedores[usuarioCount] = contenedor;
        usuarioCount++;

        this.getChildren().add(contenedor);
    }

    public void moverTaxi(String id, double x, double y) {
        for (int i = 0; i < taxiCount; i++) {
            if (id.equals(taxiIds[i])) {
                VBox taxi = taxiContenedores[i];
                taxi.setLayoutX(x-16);
                taxi.setLayoutY(y-16);
                break;
            }
        }
    }

    public void eliminarTaxi(String id) {
        for (int i = 0; i < taxiCount; i++) {
            if (id.equals(taxiIds[i])) {
                VBox taxi = taxiContenedores[i];
                this.getChildren().remove(taxi);
                for (int j = i; j < taxiCount - 1; j++) {
                    taxiIds[j] = taxiIds[j + 1];
                    taxiContenedores[j] = taxiContenedores[j + 1];
                }
                taxiCount--;
                break;
            }
        }
    }

    public void eliminarUsuario(String id) {
        for (int i = 0; i < usuarioCount; i++) {
            if (id.equals(usuarioIds[i])) {
                VBox usuario = usuarioContenedores[i];
                this.getChildren().remove(usuario);
                for (int j = i; j < usuarioCount - 1; j++) {
                    usuarioIds[j] = usuarioIds[j + 1];
                    usuarioContenedores[j] = usuarioContenedores[j + 1];
                }
                usuarioCount--;
                break;
            }
        }
    }

    public boolean existeTaxi(String id) {
        for (int i = 0; i < taxiCount; i++) {
            if (id.equals(taxiIds[i])) {
                return true;
            }
        }
        return false;
    }

    public int cantidadTaxis() {
        return taxiCount;
    }
    
    public void agregarNodo(double x, double y) {

        Circle c = new Circle(x, y, 5);

        c.setFill(Color.RED);

        this.getChildren().add(c);
    }

    private VBox buscarTaxi(String id) {
        for (int i = 0; i < taxiCount; i++) {
            if (id.equals(taxiIds[i])) {
                return taxiContenedores[i];
            }
        }
        return null;
    }

    public void animarTaxiRuta(String taxiId, int[] ruta, GrafoDirigido mapa, Runnable alFinalizar) {
        VBox taxi = buscarTaxi(taxiId);

        if (taxi == null || ruta == null) return;

        SequentialTransition secuencia = new SequentialTransition();

        for (int i = 1; i < ruta.length; i++) {

            Esquina origen = mapa.getEsquina(ruta[i - 1]);

            Esquina destino = mapa.getEsquina(ruta[i]);

            double x1 = ConversorMapa.convertirX(origen.getLongitud());

            double y1 = ConversorMapa.convertirY(origen.getLatitud());

            double x2 = ConversorMapa.convertirX(destino.getLongitud());

            double y2 = ConversorMapa.convertirY(destino.getLatitud());

            TranslateTransition tramo = new TranslateTransition(Duration.millis(150), taxi);

            tramo.setByX(x2 - x1);
            tramo.setByY(y2 - y1);

            secuencia.getChildren().add(tramo);
        }

        secuencia.setOnFinished(e -> {
            taxi.setLayoutX(taxi.getLayoutX() + taxi.getTranslateX());
            taxi.setLayoutY(taxi.getLayoutY() + taxi.getTranslateY());

            taxi.setTranslateX(0);
            taxi.setTranslateY(0);

            if(alFinalizar != null)
                alFinalizar.run();
        });

        animacionesTaxis[animacionesCount] = secuencia;
        animacionesCount++;

        secuencia.play();
    }

    public void limpiarElementos() {
        detenerAnimaciones();
        // Eliminar todos los taxis
        while (taxiCount > 0) {
            VBox taxi = taxiContenedores[0];
            this.getChildren().remove(taxi);
            for (int j = 0; j < taxiCount - 1; j++) {
                taxiIds[j] = taxiIds[j + 1];
                taxiContenedores[j] = taxiContenedores[j + 1];
            }
            taxiIds[taxiCount - 1] = null;
            taxiContenedores[taxiCount - 1] = null;

            taxiCount--;
        }
        // Eliminar todos los usuarios
        while (usuarioCount > 0) {
            VBox usuario = usuarioContenedores[0];
            this.getChildren().remove(usuario);
            for (int j = 0; j < usuarioCount - 1; j++) {
                usuarioIds[j] = usuarioIds[j + 1];
                usuarioContenedores[j] = usuarioContenedores[j + 1];
            }

            usuarioIds[usuarioCount - 1] = null;
            usuarioContenedores[usuarioCount - 1] = null;
            usuarioCount--;
        }
    }

    public void detenerAnimaciones() {
        for (int i = 0; i < animacionesCount; i++) {
            if (animacionesTaxis[i] != null) {
                animacionesTaxis[i].stop();
                animacionesTaxis[i] = null;
            }
        }
        animacionesCount = 0;
    }
}
