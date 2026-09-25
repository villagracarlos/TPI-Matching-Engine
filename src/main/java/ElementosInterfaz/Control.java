package ElementosInterfaz;

//Puente entre la interfaz definida en Test.fxml y la clase Test.java
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.ImageCursor;
import javafx.scene.Cursor;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import recursos.Esquina;
import interfaz.ConversorMapa;
import interfaz.MapaPanel;
import recursos.Taxi;
import recursos.Usuario;
import recursos.Viaje;
import simulacion.ResultadoAsignacion;
import simulacion.Simulador;
import contenedores.ListaDoubleLinkedL;

public class Control {
    //Vinculacion por IDs
    @FXML
    private Button btnAsignar;
    @FXML
    private TextArea txtEntrada;
    @FXML
    private ComboBox<Usuario> cboUsuarios;
    @FXML
    private ComboBox<String> cboDestinos;
    @FXML
    private ListView<String> lstTaxisVisual;
    @FXML
    private MapaPanel mapaPanel;
    @FXML
    private ScrollPane scrollMapa;
    @FXML
    private Button btnReiniciar;

    //Lista especial para manejar los datos de la ListView, obligatorias para JavaFX
    //ObservableList solo se utiliza porque es el mecanismo interno de JavaFX para mostrar datos en un ComboBox y ListView.
    private ObservableList<String> taxisDisponibles = FXCollections.observableArrayList(); 
    private ObservableList<Usuario> usuariosDisponibles = FXCollections.observableArrayList();
    private ObservableList<String> destinosDisponibles = FXCollections.observableArrayList();
    private FilteredList<String> destinosFiltrados;

    private Simulador simulador;
    private boolean enfocarAutomaticamente = true;

    //Para el click en el mapa
    private boolean esperandoClickUsuario = false;

    // Para filtrar la lista de destinos
    private boolean filtrandoDestinos = false;

    private Timeline simulacionAutomatica;
    private boolean simulacionEnCurso = false;
    
    public void initialize() {
        System.out.println("CONTROLADOR INICIALIZADO");
        System.out.println("txtEntrada = " + txtEntrada);

        cargarSimulacion();

        cboUsuarios.setItems(usuariosDisponibles);
        cboUsuarios.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                setText(empty || usuario == null ? null : usuario.getId());
            }
        });
        cboUsuarios.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                setText(empty || usuario == null ? null : usuario.getId());
            }
        });

        cboUsuarios.valueProperty().addListener((obs, oldUser, newUser) -> {
            actualizarColaTaxis(newUser);
            if (newUser != null) {

                cargarDestinos();
                int nodoDestino = newUser.getNodoDestino();

                if (nodoDestino >= 0 && nodoDestino < simulador.getMapa().getOrden()) {
                    String nombreDestino = simulador.getMapa().nombresEsquinas[nodoDestino];

                    filtrandoDestinos = true;

                    destinosFiltrados.setPredicate(s -> true);
                    cboDestinos.getSelectionModel().select(nombreDestino);
                    cboDestinos.getEditor().setText(nombreDestino);

                    filtrandoDestinos = false;
                }
                //cboDestinos.getSelectionModel().select(newUser.getNodoDestino());
                if(enfocarAutomaticamente) {
                    enfocarUsuario(newUser);
                }
            } else {
                cboDestinos.getSelectionModel().clearSelection();
                cboDestinos.getEditor().clear();
            }
        });

        cboDestinos.getSelectionModel().selectedIndexProperty().addListener((obs, viejo, nuevo) -> {
            Usuario usuario = cboUsuarios.getSelectionModel().getSelectedItem();

            String nombreDestino = cboDestinos.getSelectionModel().getSelectedItem();
            int nodoDestino = simulador.getMapa().buscaIndexPorEsquina(nombreDestino);

            if (nodoDestino >= 0) {
                usuario.setNodoDestino(nodoDestino);
            }

            System.out.println("Destino: " + nombreDestino);
            System.out.println("Nodo: " + nodoDestino);
            System.out.println("Usuario: " + usuario.getId());
        });

        cboDestinos.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) e.consume();
        });
        cboDestinos.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.SPACE) e.consume();
        });

        cboDestinos.getEditor().textProperty().addListener((obs, viejo, nuevo) -> {
            if (filtrandoDestinos) return;

            //filtrarDestinos(nuevo);
            // Si el texto coincide con el ítem elegido, no es tipeo sino selección
            String elegido = cboDestinos.getSelectionModel().getSelectedItem();
            if (nuevo != null && nuevo.equals(elegido)) return;

            String filtro = (nuevo == null) ? "" : nuevo.toLowerCase().trim();
            destinosFiltrados.setPredicate(nombre -> nombre.toLowerCase().contains(filtro));

            if (!destinosFiltrados.isEmpty() && !cboDestinos.isShowing()) {
                //cboDestinos.show();
                if (cboDestinos.getEditor().isFocused()) cboDestinos.show();
            }
        });

        // Filtro de la barra espaciadora
        javafx.event.EventHandler<KeyEvent> bloquearEspacio = e -> {
            if (e.getCode() == KeyCode.SPACE) e.consume();
        };

        cboDestinos.addEventFilter(KeyEvent.KEY_PRESSED, bloquearEspacio);
        cboDestinos.addEventFilter(KeyEvent.KEY_RELEASED, bloquearEspacio);
        cboDestinos.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, bloquearEspacio);
        cboDestinos.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, bloquearEspacio);

        cboDestinos.skinProperty().addListener((obs, viejo, skin) -> {
            if (skin instanceof ComboBoxListViewSkin<?> s && s.getPopupContent() instanceof ListView<?> lv) {
                lv.addEventFilter(KeyEvent.KEY_PRESSED, bloquearEspacio);
                lv.addEventFilter(KeyEvent.KEY_RELEASED, bloquearEspacio);
                lv.setFocusTraversable(false);
            }
        });
        // Fin filtro de la barra espaciadora

        lstTaxisVisual.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
                if(nuevo == null) return;

                String id = nuevo.split(" - ")[0];
                Taxi taxi = simulador.buscarTaxi(id);
                if(enfocarAutomaticamente && taxi != null) enfocarTaxi(taxi);
            }
        );

        // CLICK EN EL MAPA
        mapaPanel.setOnMouseClicked(event -> {

            if (!esperandoClickUsuario)
                return;

            double x = event.getX();
            double y = event.getY();

            agregarUsuarioEnCoordenada(x, y);

            esperandoClickUsuario = false;
            mapaPanel.setCursor(Cursor.DEFAULT);
        });
    }// FIN initialize()

    private void cargarSimulacion() {
        usuariosDisponibles.clear();

        int cantTaxis = 0;
        int cantUsuarios = 10;

        simulador = new Simulador(cantTaxis, cantUsuarios);

        cargarDestinos();
        cboDestinos.setEditable(true);

        // Taxis
        Taxi[] taxis = simulador.getTaxis();

        for (int i = 0; i < simulador.getCantidadTaxis(); i++) {

            Taxi taxi = taxis[i];

            Esquina e = simulador.getMapa().getEsquina(taxi.getNodoActual());

            double x = ConversorMapa.convertirX(e.getLongitud());
            double y = ConversorMapa.convertirY(e.getLatitud());

            mapaPanel.agregarTaxi(taxi.getId(), x, y);
        }

        // Usuarios
        ListaDoubleLinkedL usuarios = simulador.getUsuarios();

        for (int i = 0; i < usuarios.tamanio(); i++) {

            Usuario usuario = (Usuario) usuarios.devolver(i);

            Esquina e = simulador.getMapa().getEsquina(usuario.getNodoOrigen());

            double x = ConversorMapa.convertirX(e.getLongitud());
            double y = ConversorMapa.convertirY(e.getLatitud());

            mapaPanel.agregarUsuario(usuario.getId(), x, y);
            usuariosDisponibles.add(usuario);
        }

        if (!usuariosDisponibles.isEmpty()) {
            cboUsuarios.getSelectionModel().selectFirst();
            actualizarColaTaxis(cboUsuarios.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void handleAsignar() {
        Usuario usuarioSeleccionado = cboUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            txtEntrada.appendText("Debe seleccionar un usuario para simular asignación.\n");
            return;
        }

        this.enfocarAutomaticamente = false;

        ResultadoAsignacion resultado = simulador.asignarUsuario(usuarioSeleccionado);

        if (resultado != null) {
            //txtEntrada.appendText(resultado + "\n");

            usuariosDisponibles.remove(usuarioSeleccionado);

            if (!usuariosDisponibles.isEmpty()) {
                cboUsuarios.getSelectionModel().selectFirst();
                actualizarColaTaxis(cboUsuarios.getSelectionModel().getSelectedItem());
            } else {
                cboUsuarios.getSelectionModel().clearSelection();
                actualizarColaTaxis(null);
            }

            
            Taxi taxi = resultado.getTaxi();
            Usuario usuario = resultado.getUsuario();
            // 1ra etapa del viaje
            int[] ruta = simulador.getMapa().caminoMinimo(taxi.getNodoActual(),usuario.getNodoOrigen());
            // 2da etapa del viaje
            int destino = usuario.getNodoDestino(); //esquina elegida en la interfaz, para simular viaje del usuario
            int[] rutaViaje = simulador.getMapa().caminoMinimo(usuario.getNodoOrigen(),destino);

            double distanciaAproximacion = simulador.getMapa().distanciaRuta(ruta); // Distacia para buscar al usuario
            double distanciaViaje = simulador.getMapa().distanciaRuta(rutaViaje); // Distacia del viaje con el usuario
            double distanciaTotal = distanciaAproximacion + distanciaViaje;

            if(resultado.isAsignado()){
                txtEntrada.appendText(
                        "Usuario: " + usuario.getId() + "\n"
                        + "Origen: " + simulador.getMapa().nombresEsquinas[usuario.getNodoOrigen()] + "\n"
                        + "Destino: " + simulador.getMapa().nombresEsquinas[usuario.getNodoDestino()] + "\n"
                        + "\n"
                        + "Taxi asignado: " + taxi.getId() + "\n"
                        + "ETA de llegada: " + tiempoETA(resultado.getEta()) + "\n"
                        + "Distancia taxi -> usuario: " + String.format("%.2f km", distanciaAproximacion) + "\n"
                        + "Distancia usuario -> destino: " + String.format("%.2f km", distanciaViaje) + "\n"
                        + "Distancia total: " + String.format("%.2f km", distanciaTotal) + "\n"
                        + "\n");
            }

            mapaPanel.animarTaxiRuta(taxi.getId(), ruta, simulador.getMapa(), () -> {
                // El pasajero sube al taxi
                mapaPanel.eliminarUsuario(usuario.getId());
                // Pausa de tiempoPausa segundos para simular el usuario subirse al taxi
                int tiempoPausa = 1;
                PauseTransition pausa = new PauseTransition(Duration.seconds(tiempoPausa));
                pausa.setOnFinished(ev -> {
                    mapaPanel.animarTaxiRuta(taxi.getId(), rutaViaje, simulador.getMapa(), () -> {
                        //Se setea el taxi a disponible y se actualiza su posicion logica para que pueda hacer otro viaje
                        taxi.setDisponible(true);
                        taxi.setNodoActual(destino);
                    });
                });
                pausa.play();

            });

        } else {
            txtEntrada.appendText("No quedan taxis disponibles o no se pudo asignar el usuario.\n");
        }

        this.enfocarAutomaticamente = true;
    }

    private void actualizarColaTaxis(Usuario usuario) {
        taxisDisponibles.clear();
        lstTaxisVisual.setItems(taxisDisponibles);

        if (usuario == null) {
            return;
        }

        Viaje[] viajesOrdenados = simulador.calcularColaTaxisPorUsuario(usuario);
        for (int i = 0; i < viajesOrdenados.length; i++) {
            Viaje viaje = viajesOrdenados[i];
            taxisDisponibles.add(viaje.getTaxi().getId() + " - ETA: " + tiempoETA(viaje.getEta()) + " - " + String.format("%.2f km", viaje.getDistancia()));
        }
    }

    @FXML
    private void handleNuevoUsuario() {
        esperandoClickUsuario = true;
        Image img = new Image(getClass().getResourceAsStream("/iconos/man-raising-hand.png"));
        mapaPanel.setCursor(new ImageCursor(img, 16, 16));

        txtEntrada.appendText("Haga clic sobre el mapa para agregar un usuario.\n");
    }

    private int buscarEsquinaMasCercana(double xClick, double yClick) {

        double mejorDist = Double.MAX_VALUE;
        int mejorNodo = -1;

        for (int i = 0; i < simulador.getMapa().getOrden(); i++) {

            Esquina e = simulador.getMapa().getEsquina(i);

            double x = ConversorMapa.convertirX(e.getLongitud());
            double y = ConversorMapa.convertirY(e.getLatitud());

            double dist = Math.sqrt(
                    Math.pow(x - xClick, 2) +
                    Math.pow(y - yClick, 2));

            if (dist < mejorDist) {
                mejorDist = dist;
                mejorNodo = i;
            }
        }

        return mejorNodo;
    }

    private void agregarUsuarioEnCoordenada(double xClick, double yClick) {

        int nodo = buscarEsquinaMasCercana(xClick, yClick);

        if (nodo < 0)
            return;

        Usuario usuario = simulador.crearUsuario(nodo);

        usuariosDisponibles.add(usuario);

        Esquina e = simulador.getMapa().getEsquina(nodo);

        double x = ConversorMapa.convertirX(e.getLongitud());
        double y = ConversorMapa.convertirY(e.getLatitud());

        mapaPanel.agregarUsuario(usuario.getId(), x, y);

        txtEntrada.appendText("Usuario agregado en: " + simulador.getMapa().nombresEsquinas[nodo] + "\n");
    }

    private String tiempoETA(double segundos){
        int totalSegundos = (int) Math.round(segundos);

        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segs = totalSegundos % 60;

        if (horas > 0) return String.format("%02d:%02d:%02d", horas, minutos, segs);
        else return String.format("%02d:%02d", minutos, segs);
    }

    private void centrarMapa(double x, double y) {

        double anchoMapa = mapaPanel.getBoundsInLocal().getWidth();
        double altoMapa = mapaPanel.getBoundsInLocal().getHeight();

        double viewportAncho = scrollMapa.getViewportBounds().getWidth();
        double viewportAlto = scrollMapa.getViewportBounds().getHeight();

        double h = (x - viewportAncho / 2) / (anchoMapa - viewportAncho);
        double v = (y - viewportAlto / 2) / (altoMapa - viewportAlto);

        scrollMapa.setHvalue(Math.max(0, Math.min(1, h)));
        scrollMapa.setVvalue(Math.max(0, Math.min(1, v)));
    }

    private void enfocarUsuario(Usuario usuario){
        Esquina e = simulador.getMapa().getEsquina(usuario.getNodoOrigen());

        double x = ConversorMapa.convertirX(e.getLongitud());
        double y = ConversorMapa.convertirY(e.getLatitud());

        centrarMapa(x, y);
    }

    private void enfocarTaxi(Taxi taxi){
        Esquina e = simulador.getMapa().getEsquina(taxi.getNodoActual());

        double x = ConversorMapa.convertirX(e.getLongitud());
        double y = ConversorMapa.convertirY(e.getLatitud());

        centrarMapa(x,y);
    }

    private void cargarDestinos() {
        destinosDisponibles.clear();

        for (int i = 0; i < simulador.getMapa().getOrden(); i++) {
            String nombre = simulador.getMapa().nombresEsquinas[i];
            destinosDisponibles.add(nombre);
        }
        //cboDestinos.setItems(destinosDisponibles);
        destinosFiltrados = new FilteredList<>(destinosDisponibles, s -> true);
        cboDestinos.setItems(destinosFiltrados);
        cboDestinos.setEditable(true);
    }

    private void filtrarDestinos(String texto) {
        //destinosDisponibles.clear();
        String textoBuscado = texto.toLowerCase().trim();
        ObservableList<String> resultados = FXCollections.observableArrayList();

        for (int i = 0; i < simulador.getMapa().getOrden(); i++) {
            String nombre = simulador.getMapa().nombresEsquinas[i];

            if (nombre.toLowerCase().contains(textoBuscado)) {
                resultados.add(nombre);
            }
        }

        filtrandoDestinos = true;

        cboDestinos.setItems(resultados);

        cboDestinos.getEditor().setText(texto);
        cboDestinos.getEditor().positionCaret(texto.length());

        filtrandoDestinos = false;

        if (!resultados.isEmpty()) cboDestinos.show();
    }

    @FXML
    private void handleReiniciar() {
        if (simulacionAutomatica != null) {
            simulacionAutomatica.stop();
            simulacionAutomatica = null;
        }
        simulacionEnCurso = false;

        // Cancelar una posible creación de usuario mediante click
        esperandoClickUsuario = false;
        mapaPanel.setCursor(Cursor.DEFAULT);

        // Detener cualquier animación de los taxis
        mapaPanel.detenerAnimaciones();

        // Limpiar elementos gráficos
        mapaPanel.limpiarElementos();

        // Limpiar elementos de la interfaz
        usuariosDisponibles.clear();
        destinosDisponibles.clear();
        lstTaxisVisual.getItems().clear();

        cboUsuarios.getSelectionModel().clearSelection();
        cboDestinos.getSelectionModel().clearSelection();
        cboDestinos.getEditor().clear();

        txtEntrada.clear();

        // Crear una simulación completamente nueva
        cargarSimulacion();

        // Seleccionar nuevamente el primer usuario
        if (!usuariosDisponibles.isEmpty()) {
            cboUsuarios.getSelectionModel().selectFirst();
            actualizarColaTaxis(cboUsuarios.getSelectionModel().getSelectedItem());
        }

        txtEntrada.appendText("Simulación reiniciada.\n");
    }

    private void generarUsuarioAutomatico() {

        // Elegir una esquina aleatoria
        int nodoOrigen = (int) (Math.random() * simulador.getMapa().getOrden());

        // Crear usuario
        Usuario usuario = simulador.crearUsuario(nodoOrigen);

        // Mostrarlo en el mapa
        Esquina esquina = simulador.getMapa().getEsquina(nodoOrigen);

        double x = ConversorMapa.convertirX(esquina.getLongitud());
        double y = ConversorMapa.convertirY(esquina.getLatitud());

        mapaPanel.agregarUsuario(usuario.getId(), x, y);

        // Agregarlo al ComboBox
        usuariosDisponibles.add(usuario);

        txtEntrada.appendText("Nuevo usuario automático: " + usuario.getId() + "\n");

        // Intentar asignarle un taxi inmediatamente
        intentarAsignarUsuario(usuario);
    }

    private void intentarAsignarUsuario(Usuario usuario) {

        ResultadoAsignacion resultado = simulador.asignarUsuario(usuario);

        if (resultado == null) return;

        if (!resultado.isAsignado()) {
            txtEntrada.appendText("Usuario " + usuario.getId() + " queda esperando taxi.\n");
            return;
        }

        // El usuario consiguió taxi
        usuariosDisponibles.remove(usuario);

        Taxi taxi = resultado.getTaxi();

        int[] rutaHastaUsuario = simulador.getMapa().caminoMinimo(taxi.getNodoActual(), usuario.getNodoOrigen());

        double distanciaAproximacion = simulador.getMapa().distanciaRuta(rutaHastaUsuario);

        int[] rutaViaje = simulador.getMapa().caminoMinimo(usuario.getNodoOrigen(), usuario.getNodoDestino());

        double distanciaViaje = simulador.getMapa().distanciaRuta(rutaViaje);

        double distanciaTotal = distanciaAproximacion + distanciaViaje;

        txtEntrada.appendText("----------------------------------------\n"
                + "Usuario: " + usuario.getId() + "\n"
                + "Origen: " + simulador.getMapa().nombresEsquinas[usuario.getNodoOrigen()]
                + "\n"
                + "Destino: " + simulador.getMapa().nombresEsquinas[usuario.getNodoDestino()]
                + "\n\n"
                + "Taxi asignado: " + taxi.getId() + "\n"
                + "Distancia taxi -> usuario: " + String.format("%.2f km", distanciaAproximacion) + "\n"
                + "Distancia usuario -> destino: " + String.format("%.2f km", distanciaViaje) + "\n"
                + "Distancia total: " + String.format("%.2f km", distanciaTotal) + "\n"
                + "ETA de llegada: " + tiempoETA(resultado.getEta()) + "\n"
                + "----------------------------------------\n\n"
        );

        // Taxi -> usuario
        mapaPanel.animarTaxiRuta(taxi.getId(), rutaHastaUsuario, simulador.getMapa(), () -> {
                    mapaPanel.eliminarUsuario(usuario.getId());

                    // Usuario -> destino
                    mapaPanel.animarTaxiRuta(taxi.getId(), rutaViaje, simulador.getMapa(), () -> {

                                taxi.setNodoActual(usuario.getNodoDestino());

                                taxi.setDisponible(true);

                                txtEntrada.appendText(taxi.getId() + " terminó el viaje de " + usuario.getId() + ".\n\n");

                                // Buscar otro usuario pendiente
                                asignarSiguientePendiente();
                            }
                    );
                }
        );
    }

    private void asignarSiguientePendiente() {

        ListaDoubleLinkedL usuarios = simulador.getUsuarios();

        if (usuarios.estaVacia()) return;

        Usuario usuario = (Usuario) usuarios.devolver(0);

        intentarAsignarUsuario(usuario);
    }

    @FXML
    private void handleSimularTodos() {

        if (simulacionEnCurso) {
            txtEntrada.appendText("La simulación automática ya está en ejecución.\n");
            return;
        }

        simulacionEnCurso = true;

        txtEntrada.appendText("=== SIMULACIÓN AUTOMÁTICA INICIADA ===\n");

        simulacionAutomatica = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> generarUsuarioAutomatico()));

        simulacionAutomatica.setCycleCount(Timeline.INDEFINITE);

        simulacionAutomatica.play();
    }
}
