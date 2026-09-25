package recursos;

public class Usuario {
    private String id;
    private int nodoOrigen;
    private int nodoDestino;
    
    public Usuario(String id, int nodoOrigen) {
        this.id = id;
        this.nodoOrigen = nodoOrigen;
    }
    
    public String getId() {
        return id;
    }

    public void setNodoOrigen(int nodoOrigen) {
        this.nodoOrigen = nodoOrigen;
    }

    public int getNodoOrigen() {
        return nodoOrigen;
    }

    public int getNodoDestino() { return nodoDestino;}

    public void setNodoDestino(int nodoDestino) { this.nodoDestino = nodoDestino;}

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
