package grafoDirigido;

import contenedores.*;
import recursos.*;

public abstract class AbsGrafoD extends AbsGrafo implements OperacionesGD{
	
	protected MatrizGrafo matrizCostoF,matrizCaminoF;
	protected ListaDoubleLinkedL listaDistancia, listaCamino, listaSolucion;

	public AbsGrafoD(int ordenGrafo){
		super(ordenGrafo);
	}
		
	public abstract void cargarGrafo();
	
	public void muestraDijkstra(int startVertex){
		double currCost; int w;
		
		Dijkstra(startVertex);
		
		for (int v=0; v<getOrden();v++){
			System.out.println("vertice " + v);
			if (v!=startVertex){
				currCost=(double)this.listaDistancia.devolver(v);
				System.out.println("costo desde " + startVertex + " a " + v + "->" + currCost);
			
				System.out.println("mostrando un camino desde "+ v + " a " + startVertex);
				
				w=(int)this.listaCamino.devolver(v);
				
				do{
					System.out.println("camino " + w);
					w=(int)this.listaCamino.devolver(w);
				}while(w!=-1);//recordemos que al inicializar cambiamos todos los -1 salvo el startVertex
			}		
		}
		
	}

	private void Dijkstra(int startVertex) {
    	double minCost, currCost, arcCost; int minVertex, vertex;
    
    // Usar arreglos primitivos O(1) en lugar de listas enlazadas O(N) para el procesamiento interno masivo
    	double[] arrDistancia = new double[getOrden()];
    	int[] arrCamino = new int[getOrden()];
    	int[] arrSolucion = new int[getOrden()];

    // Inicializacion
    	for (int i = 0; i < getOrden(); i++) {
        	arrSolucion[i] = -1;
        	arrCamino[i] = -1;
        	arrDistancia[i] = (double) infinito;
    	}
    	arrSolucion[startVertex] = startVertex;

    	for (int i = 0; i < getOrden(); i++) {
        	if (i != startVertex) {
            	arrDistancia[i] = (double) this.matrizCosto.devolver(startVertex, i);
            	arrCamino[i] = startVertex;
        	}
    	}

    // Bucle principal de coste mínimo
    	for (int i = 1; i < getOrden(); i++) {
				minCost = infinito;
				minVertex = -1;

			for (int w = 0; w < getOrden(); w++) {
					if (w != startVertex) {
						currCost = arrDistancia[w];
						vertex = arrSolucion[w];
					if (currCost < minCost && vertex == -1) {
						minCost = currCost; minVertex = w;
					}
				}
			}

			if (minVertex != -1) {
				arrSolucion[minVertex] = minVertex;
				arrDistancia[minVertex] = minCost;

				for (int v = 0; v < getOrden(); v++) {
					vertex = arrSolucion[v];
					if (vertex == -1) {
						arcCost = (double) this.matrizCosto.devolver(minVertex, v);
						currCost = arrDistancia[v];
						if (minCost + arcCost < currCost) {
							arrDistancia[v] = minCost + arcCost;
							arrCamino[v] = minVertex;
						}
					}
				}
        	}
    	}

    // Reconstruir las listas enlazadas originales al finalizar para no romper la compatibilidad con otras funciones
    	this.listaDistancia = new ListaDoubleLinkedL();
    	this.listaCamino = new ListaDoubleLinkedL();
    	this.listaSolucion = new ListaDoubleLinkedL();
    
    	for(int i = 0; i < getOrden(); i++) {
        	this.listaDistancia.insertar(arrDistancia[i], i);
        	this.listaCamino.insertar(arrCamino[i], i);
        	this.listaSolucion.insertar(arrSolucion[i], i);
    	}
	}
	
	//Del taxi al usuario
	public double etaMinimo(int origen, int destino){
		//calcula la distancia minima desde el vertice origen a todos los demas
		Dijkstra(origen);

		//devuelve el valor de la distancia minima desde el vertice origen al vertice destino
		return (double)this.listaDistancia.devolver(destino);
		
	}

	public void muestraGrafo(){
		double currCost;
		for (int i=0; i<getOrden();i++){
			for (int j=0; j<getOrden();j++){
				if (i!=j){
					currCost=(double)this.matrizCosto.devolver(i, j);
					if (currCost!=infinito){
						System.out.println("costo " + i + " a " + j + "->" + currCost);
					}				
				}
			}			
		}		
	}
	
	public int[] caminoMinimo(int origen, int destino) {

		Dijkstra(origen);

		int[] ruta = new int[getOrden() + 1];
		int longitud = 0;
		int actual = destino;

		while (actual != -1) {
			ruta[longitud++] = actual;
			if (actual == origen) {
				break;
			}
			actual = (int) listaCamino.devolver(actual);
		}

		int[] resultado = new int[longitud];
		for (int i = 0; i < longitud; i++) {
			resultado[i] = ruta[longitud - 1 - i];
		}

		return resultado;
	}
}
