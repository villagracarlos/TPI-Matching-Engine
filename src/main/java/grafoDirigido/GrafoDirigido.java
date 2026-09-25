package grafoDirigido;

import contenedores.MatrizGrafo;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import recursos.Esquina;
import recursos.MatrizSalta;

public class GrafoDirigido extends AbsGrafoD {

    public String[] nombresEsquinas, idsEsquinas, nombresGeojson, tiposGeojson, idNodo;
    private Esquina[] esquinasPorId;
    private int totalEsquinasMeta, totalTiposGeojson;

    public GrafoDirigido(int ordenGrafo) {
        super(ordenGrafo);
        this.nombresEsquinas = new String[20000];
        this.idsEsquinas = new String[20000];
        this.esquinasPorId = new Esquina[20000];
        this.nombresGeojson = new String[20000];
        this.tiposGeojson = new String[20000];
        this.totalEsquinasMeta = 0;
        this.totalTiposGeojson = 0;
    }

    @Override
    public void cargarGrafo() {
        String rutaMetaDatos = "src/main/java/datos/meta_datos_nodos_2k.csv";
        String rutaGeojson = "src/main/java/datos/SaltaCompleto.geojson";

        try {
            this.totalTiposGeojson = 0;
            this.totalEsquinasMeta = 0;

            try {
                String content = new String(Files.readAllBytes(Paths.get(rutaGeojson)));
                JSONObject json = new JSONObject(content);
                JSONArray features = json.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject props = features.getJSONObject(i).getJSONObject("properties");
                    String name = props.optString("name", "").toLowerCase().trim();
                    String highway = props.optString("highway", "residential");
                    if (!name.isEmpty()) {
                        this.nombresGeojson[this.totalTiposGeojson] = name;
                        this.tiposGeojson[this.totalTiposGeojson] = highway;
                        this.totalTiposGeojson++;
                    }
                }
                System.out.println("Diccionario GeoJSON cargado.");
            } catch (Exception e) {
                System.out.println("Advertencia: No se pudo cargar el GeoJSON.");
            }

            BufferedReader brMeta = new BufferedReader(new InputStreamReader(new FileInputStream(rutaMetaDatos), "UTF-8"));
            brMeta.readLine();
            String metaLine;

            while ((metaLine = brMeta.readLine()) != null) {
                String[] parts = metaLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 6) {
                    String idNodoReal = parts[0].trim();
                    double latitud = Double.parseDouble(parts[1].trim());
                    double longitud = Double.parseDouble(parts[2].trim());
                    String calleA = parts[3].replace("\"", "").trim();
                    String calleB = parts[4].replace("\"", "").trim();
                    String nombreEsquina = parts[5].replace("\"", "").trim();

                    this.idsEsquinas[this.totalEsquinasMeta] = idNodoReal;
                    this.esquinasPorId[this.totalEsquinasMeta] = new Esquina(idNodoReal, latitud, longitud, calleA, calleB, nombreEsquina);
                    this.totalEsquinasMeta++;
                }
            }
            brMeta.close();

            System.out.println("Cargando matriz de conexiones...");
            MatrizSalta datos = new MatrizSalta();

            this.ordenGrafo = datos.getNroFilas();
            System.out.println("Total de esquinas detectadas: " + this.ordenGrafo);

            this.idNodo = new String[this.ordenGrafo];
            for (int i = 0; i < this.ordenGrafo; i++) {
                this.idNodo[i] = datos.getIdNodo(i);
                Esquina esquina = buscarEsquinaPorId(this.idNodo[i]);
                String nombreReal = (esquina != null) ? esquina.getNombreEsquina() : "Intersección " + this.idNodo[i];
                this.nombresEsquinas[i] = nombreReal;
            }

            this.matrizCosto = new MatrizGrafo(this.ordenGrafo);
            this.infinito = 10000.0;

            for (int i = 0; i < this.ordenGrafo; i++) {
                String idOrigenReal = this.idNodo[i];
                Esquina origen = buscarEsquinaPorId(idOrigenReal);
                String origenCalleA = (origen != null) ? origen.getCalleA() : "";
                String origenCalleB = (origen != null) ? origen.getCalleB() : "";

                for (int j = 0; j < this.ordenGrafo; j++) {
                    if (i == j) {
                        this.matrizCosto.actualizar(0.0, i, j);
                        continue;
                    }

                    int conexion = datos.devolverConexion(i, j);
                    if (conexion == 1) {
                        String idDestinoReal = this.idNodo[j];
                        Esquina destino = buscarEsquinaPorId(idDestinoReal);

                        String destinoCalleA = (destino != null) ? destino.getCalleA() : "";
                        String destinoCalleB = (destino != null) ? destino.getCalleB() : "";

                        String calleDeViaje = "residential";
                        if (origenCalleA.equals(destinoCalleA) || origenCalleA.equals(destinoCalleB)) {
                            calleDeViaje = origenCalleA;
                        } else if (origenCalleB.equals(destinoCalleA) || origenCalleB.equals(destinoCalleB)) {
                            calleDeViaje = origenCalleB;
                        }

                        String calleBuscada = calleDeViaje.toLowerCase().trim();
                        String tipoVia = buscarTipoGeojson(calleBuscada);
                        if (tipoVia.equals("residential") && (calleBuscada.contains("av ") || calleBuscada.contains("avenida"))) {
                            tipoVia = "primary";
                        }

                        double distanciaMetros = 100.0;
                        if (origen != null && destino != null) {
                            distanciaMetros = calcularHaversine(origen.getLongitud(), origen.getLatitud(), destino.getLongitud(), destino.getLatitud());
                        }

                        double velocidadMS = obtenerVelocidadMS(tipoVia);
                        double etaIdealSegundos = distanciaMetros / velocidadMS;
                        double traficoSorpresa = Math.random() * 4.0;
                        etaIdealSegundos = Math.round(((etaIdealSegundos) + traficoSorpresa) * 100.0) / 100.0;

                        this.matrizCosto.actualizar(etaIdealSegundos, i, j);
                    } else {
                        this.matrizCosto.actualizar(this.infinito, i, j);
                    }
                }
            }
            System.out.println("Grafo dirigido cargado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error crítico al estructurar el Grafo: " + e.getMessage());
        }
    }

    private Esquina buscarEsquinaPorId(String idBuscado) {
        if (idBuscado == null) {
            return null;
        }
        for (int i = 0; i < this.totalEsquinasMeta; i++) {
            if (this.idsEsquinas[i] != null && this.idsEsquinas[i].equals(idBuscado)) {
                return this.esquinasPorId[i];
            }
        }
        return null;
    }

    public int buscaIndexPorEsquina(String esquina){
        if (esquina == null) {
            return -1;
        }
        for (int i = 0; i < this.totalEsquinasMeta; i++) {
            if (this.esquinasPorId[i].getNombreEsquina().equals(esquina)) {
                return i;
            }
        }
        return -1;
    }

    private String buscarTipoGeojson(String calleBuscada) {
        if (calleBuscada == null || calleBuscada.isEmpty()) {
            return "residential";
        }
        for (int i = 0; i < this.totalTiposGeojson; i++) {
            if (this.nombresGeojson[i] != null && this.nombresGeojson[i].equals(calleBuscada)) {
                return this.tiposGeojson[i];
            }
        }
        return "residential";
    }

    private double calcularHaversine(double lon1, double lat1, double lon2, double lat2) {
        double radioTierra = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radioTierra * c;
    }

    public double obtenerVelocidadMS(String tipoCalle) {
        switch (tipoCalle) {
            case "primary":
                return 45.0 / 3.6;
            case "secondary":
                return 35.0 / 3.6;
            case "residential":
            case "tertiary":
                return 25.0 / 3.6;
            default:
                return 20.0 / 3.6;
        }
    }

    public Esquina getEsquina(int indice) {
        String id = idNodo[indice];
        return buscarEsquinaPorId(id);
    }

    public double distanciaRuta(int[] ruta) {
        double distanciaTotal = 0;

        if (ruta == null || ruta.length < 2) return 0;

        for (int i = 0; i < ruta.length - 1; i++) {
            Esquina origen = getEsquina(ruta[i]);
            Esquina destino = getEsquina(ruta[i + 1]);
            distanciaTotal += calcularHaversine(origen.getLongitud(), origen.getLatitud(), destino.getLongitud(), destino.getLatitud());
        }

        return distanciaTotal / 1000.0;
    }


}
