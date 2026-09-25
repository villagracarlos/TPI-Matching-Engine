package interfaz;

//Clase para convertir las coordenadas reales en pixeles del mapa
public class ConversorMapa {

    // LIMITES REALES DEL MAPA

    private static final double LAT_SUP = -24.7670606;
    private static final double LAT_INF = -24.8119525;

    private static final double LON_IZQ = -65.4363349;
    private static final double LON_DER = -65.3907366;

    // TAMAÑO DE LA IMAGEN

    private static final double ANCHO = 1920;
    private static final double ALTO = 2078;

    //Regla de 3 simple
    public static double convertirX(double longitud) {
        return ((longitud - LON_IZQ) /(LON_DER - LON_IZQ)) * ANCHO;
    }

    public static double convertirY(double latitud) {
        return ((LAT_SUP - latitud) /(LAT_SUP - LAT_INF)) * ALTO;
    }
}