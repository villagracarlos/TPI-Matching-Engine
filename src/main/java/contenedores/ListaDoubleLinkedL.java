package contenedores;

public class ListaDoubleLinkedL extends Lista1DLinkedL{

	@Override
	public boolean iguales(Object elementoL, Object elemento) {
		//return (boolean)elementoL==(boolean)elemento;
                return elementoL.equals(elemento);
	}

}
