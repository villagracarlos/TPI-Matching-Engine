package recursos;
//Taxi y ETA
public class Viaje {
    private Taxi taxi;
    private double eta;
    private double distancia;

    public Viaje(Taxi taxi, double eta, double distancia) {
        this.taxi = taxi;
        this.eta = eta;
        this.distancia = distancia;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public double getEta() {
        return eta;
    }

    public double getDistancia() { return distancia; }
}
