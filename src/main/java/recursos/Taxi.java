package recursos;

public class Taxi {
    private String id;
    //el nodo que esta posicionado el taxi
    private int nodoActual;
    private boolean disponible;
    
    public Taxi(String id, int nodoActual) {
        this.id = id;
        this.nodoActual = nodoActual;
        this.disponible = true; // Inicialmente, el taxi está disponible
    }
    
    public String getId() {
        return id;
    }

    public int getNodoActual() {
        return nodoActual;
    }

    public void setNodoActual(int nodoActual) {
        this.nodoActual = nodoActual;
    }
    
    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
