package simulacion;

import recursos.Taxi;
import recursos.Usuario;

public class ResultadoAsignacion {
    private Usuario usuario;
    private Taxi taxi;
    private double eta;
    private boolean asignado;

    public ResultadoAsignacion(Usuario usuario, Taxi taxi, double eta, boolean asignado) {

        this.usuario = usuario;
        this.taxi = taxi;
        this.eta = eta;
        this.asignado = asignado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public double getEta() {
        return eta;
    }

    public boolean isAsignado() {
        return asignado;
    }

    @Override
    public String toString() {

        if (!asignado) {
            return "No hay taxis disponibles para "
                    + usuario.getId();
        }

        return "Taxi asignado: " + taxi.getId() + " | Usuario: " + usuario.getId() + " | ETA: " + String.format("%.2f", eta) + " segundos";
    }
}
