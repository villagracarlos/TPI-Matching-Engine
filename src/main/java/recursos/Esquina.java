package recursos;

public class Esquina {

    private String id;
    private double latitud;
    private double longitud;
    private String calleA;
    private String calleB;
    private String nombreEsquina;

    //La esquina con todos los datos
    public Esquina(String id, double latitud, double longitud, String calleA, String calleB, String nombreEsquina) {
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.calleA = calleA;
        this.calleB = calleB;
        this.nombreEsquina = nombreEsquina;
    }

    public String getId() {
        return id;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getCalleA() {
        return calleA;
    }

    public String getCalleB() {
        return calleB;
    }
    
    public String getNombreEsquina() {
        return nombreEsquina;
    }
}
