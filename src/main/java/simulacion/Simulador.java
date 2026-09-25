package simulacion;

import contenedores.ListaDoubleLinkedL;
import grafoDirigido.GrafoDirigido;
import contenedores.GraphPriorityQueue;
import recursos.*;

public class Simulador {
    private static final double PROB_RECHAZO = 0.20;

    private GrafoDirigido mapaSalta;
    private int totalEsquinas, totalTaxis, siguienteIdUsuario;
    private ListaDoubleLinkedL usuarios;
    private Taxi[] taxis;

    public Simulador(int cantidadTaxis, int cantidadUsuarios) {
        cargarMapa();
        totalEsquinas = cantEsquinas();

        usuarios = new ListaDoubleLinkedL();

        cargarTaxis(cantidadTaxis);
        cargarUsuarios(cantidadUsuarios);

        siguienteIdUsuario = cantidadUsuarios + 1;
    }

    private void cargarMapa() {
        mapaSalta = new GrafoDirigido(1);
        mapaSalta.cargarGrafo();
    }

    private int cantEsquinas() {
        this.totalEsquinas = this.mapaSalta.getOrden();
        System.out.println("Total de esquinas en el mapa: " + totalEsquinas);
        return totalEsquinas;
    }

    private void cargarTaxis(int cantidadTaxis) {
        this.taxis = new Taxi[cantidadTaxis];
        this.totalTaxis = 0;
        for (int i = 1; i <= cantidadTaxis; i++) {
            int nodoAleatorio = (int) (Math.random() * totalEsquinas);
            this.taxis[totalTaxis] = new Taxi("Taxi " + i, nodoAleatorio);
            System.out.println("[+] " + this.taxis[totalTaxis].getId() + " posicionado en: " + mapaSalta.nombresEsquinas[nodoAleatorio]);
            totalTaxis++;
        }
    }

    private void cargarUsuarios(int cantidadUsuarios) {
        System.out.println("--------------------------------------------------");
        for (int i = 1; i <= cantidadUsuarios; i++) {
            int nodoAleatorio = (int) (Math.random() * totalEsquinas);

            Usuario usuario = new Usuario("Pasajero " + i, nodoAleatorio);
            usuario.setNodoDestino(generarDestinoAleatorio());

            usuarios.insertar(usuario, usuarios.tamanio());

            System.out.println("[.] " + usuario.getId() + " solicito viaje desde: " + mapaSalta.nombresEsquinas[nodoAleatorio]);
        }
    }

    private ResultadoAsignacion procesarUsuario(Usuario usuario) {
        GraphPriorityQueue colaTaxis = new GraphPriorityQueue();
        for (int i = 0; i < totalTaxis; i++) {
            Taxi taxi = taxis[i];
            if (taxi.isDisponible()) {
                int origen = taxi.getNodoActual();
                int destino = usuario.getNodoOrigen();
                double etaActual = mapaSalta.etaMinimo(origen, destino);
                double distancia = mapaSalta.distanciaRuta(mapaSalta.caminoMinimo(origen, destino));
                if (etaActual < 10000.0) {
                    Viaje propuesta = new Viaje(taxi, etaActual,distancia);
                    colaTaxis.meter(propuesta);
                }
            }
        }

        System.out.println("    [*] Estado de la cola de prioridad para este pasajero:");
        colaTaxis.muestra();
        System.out.println("    ----------------------------------------------------------");

        Taxi taxiAsignado = null;
        double etaAsignado = 10000.0;

        while (!colaTaxis.estaVacia()) {
            Viaje mejorViaje = (Viaje) colaTaxis.sacar();
            Taxi mejorTaxiCandidato = mejorViaje.getTaxi();
            double etaCandidato = mejorViaje.getEta();
            boolean conductorRechaza = Math.random() < PROB_RECHAZO;

            if (conductorRechaza) {
                System.out.println("    [x] El " + mejorTaxiCandidato.getId() + " rechazo la solicitud.");
            } else {
                taxiAsignado = mejorTaxiCandidato;
                etaAsignado = etaCandidato;
                System.out.println("    [-] El " + mejorTaxiCandidato.getId() + " acepto el viaje y llegaria en " + String.format("%.2f", etaCandidato) + " minutos.");
                break;
            }
        }

        if (taxiAsignado != null && etaAsignado < 10000.0) {
            System.out.println("    [ASIGNACIÓN EXITOSA] -> Se despacha definitivamente el " + taxiAsignado.getId());
            System.out.println("    [TIEMPO ESTIMADO]    -> " + String.format("%.2f", etaAsignado) + " minutos de espera.");
            taxiAsignado.setDisponible(false);
            return new ResultadoAsignacion(usuario, taxiAsignado, etaAsignado, true);
        } else {
            return new ResultadoAsignacion(usuario, null, 0, false);
        }
    }

    public ResultadoAsignacion asignarSiguienteUsuario() {
        if (!usuarios.estaVacia()){
            Usuario usuario = (Usuario)usuarios.devolver(0);
            usuarios.eliminar(0);
            return procesarUsuario(usuario);
        }
        return null;
    }

    public Taxi[] getTaxis() {
        return taxis;
    }

    public ListaDoubleLinkedL getUsuarios() {
        return usuarios;
    }

    public int getCantidadTaxis() {
        return totalTaxis;
    }

    public int getCantidadUsuarios() {
        return usuarios.tamanio();
    }

    public void agregarUsuario(Usuario usuario) {
        usuarios.insertar(usuario, usuarios.tamanio());
    }

    public GrafoDirigido getMapa() {
        return mapaSalta;
    }

    public Viaje[] calcularColaTaxisPorUsuario(Usuario usuario) {
        GraphPriorityQueue colaTaxis = new GraphPriorityQueue();
        for (int i = 0; i < totalTaxis; i++) {
            Taxi taxi = taxis[i];
            if (taxi.isDisponible()) {
                int origen = taxi.getNodoActual();
                int destino = usuario.getNodoOrigen();
                double etaActual = mapaSalta.etaMinimo(origen, destino);
                double distancia = mapaSalta.distanciaRuta(mapaSalta.caminoMinimo(origen, destino));
                if (etaActual < 10000.0) {
                    colaTaxis.meter(new Viaje(taxi, etaActual,distancia));
                }
            }
        }

        Viaje[] temporal = new Viaje[totalTaxis];
        int totalViajes = 0;
        while (!colaTaxis.estaVacia()) {
            temporal[totalViajes++] = (Viaje) colaTaxis.sacar();
        }
        Viaje[] viajesOrdenados = new Viaje[totalViajes];
        for (int i = 0; i < totalViajes; i++) {
            viajesOrdenados[i] = temporal[i];
        }
        return viajesOrdenados;
    }

    public ResultadoAsignacion asignarUsuario(Usuario usuario) {
        ResultadoAsignacion resultado = procesarUsuario(usuario);
        if (resultado != null && resultado.isAsignado()) {
            int pos = usuarios.buscar(usuario);
            if(pos != -1) usuarios.eliminar(pos);
        }
        return resultado;
    }

    public Usuario getPrimerUsuario() {
        if (!usuarios.estaVacia()) return (Usuario)usuarios.devolver(0);
        return null;
    }

    public int generarDestinoAleatorio() {
        return (int)(Math.random() * mapaSalta.getOrden());
    }

    public Usuario crearUsuario(int nodo) {
        Usuario usuario = new Usuario("Pasajero " + siguienteIdUsuario++, nodo);
        int destino = generarDestinoAleatorio();
        // Evitar que origen y destino sean iguales
        while (destino == nodo) {
            destino = generarDestinoAleatorio();
        }
        usuario.setNodoDestino(destino);
        agregarUsuario(usuario);
        return usuario;
    }

    public Taxi buscarTaxi(String id) {
        for (int i = 0; i < taxis.length; i++) {
            if (taxis[i] != null && taxis[i].getId().equals(id)) {
                return taxis[i];
            }
        }
        return null;
    }
}