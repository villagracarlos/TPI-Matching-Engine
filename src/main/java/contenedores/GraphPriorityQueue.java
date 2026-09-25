package contenedores;
import recursos.Nodo;
import recursos.Viaje;
//La cola de prioridad de los taxis por ETA
public class GraphPriorityQueue extends ColaPrioridad {
	
	public boolean esMenor(Object objA, Object objB){
		return ((Viaje)objA).getEta() < ((Viaje) objB).getEta();
	}

	public boolean esMayor(Object objA, Object objB){
		return ((Viaje)objA).getEta() > ((Viaje) objB).getEta();
	}
	
	public boolean iguales(Object objA, Object objB){
		return ((Viaje)objA).getEta() == ((Viaje) objB).getEta();
	}
	
	
	public void muestra(){
		Nodo aux;
		Viaje propuesta;		
		if (!estaVacia()){
			aux = frenteC;
			while (aux != null){
				propuesta = (Viaje)aux.getNodoInfo();
				System.out.println("    |-- Cola -> " + propuesta.getTaxi().getId() + " - ETA: " + String.format("%.2f", propuesta.getEta()) + " min");
				aux = aux.getNextNodo();
			}			
		}else{
			System.out.println("    |-- Cola de prioridad vacía (Ningún taxi puede llegar al destino).");
		}		
	}
}
