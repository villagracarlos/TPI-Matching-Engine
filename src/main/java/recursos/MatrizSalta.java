package recursos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MatrizSalta{
    
    protected int[][] matriz;
    protected String[] idNodo;
    
    public MatrizSalta(){
        try{
            this.cargaMatriz();
        }catch(IOException io){
            System.err.println("Error en base de datos");
        }
    }
    
    public int getNroFilas(){ return this.matriz.length;}
    public int getNroColumnas(){ return this.matriz[0].length;}
    
    private void cargaMatriz() throws IOException {
        InputStream fIn;
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);

        try {  
            fIn = new FileInputStream("src/main/java/datos/matriz_nodos_2k.csv");
            isr = new InputStreamReader(fIn);
            buffer = new BufferedReader(isr);

            String linea; 
            if((linea = buffer.readLine()) != null){//Compruebo que no sea un archivo vacio
                String[] partes = linea.split(",");
                
                this.matriz = new int[partes.length-1][partes.length-1];
                this.idNodo = new String[partes.length-1];
                
                for(int k = 1; k < partes.length; k++){ //Leo primer linea del archivo .csv
                    this.idNodo[k-1] = partes[k];
                }
                
                int i = 1;
                while ((linea = buffer.readLine()) != null) {//Leo linea por linea del archivo .csv
                    partes = linea.split(",");
                    for(int j = 1; j < partes.length ; j++){
                        try {
                            this.matriz[i-1][j-1] = Integer.parseInt(partes[j]);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: La cadena no contiene un número válido.");
                        }
                    }
                    i++;
                }
            }

            fIn.close();

        } catch (IOException io) {
            //System.err.println("No se puede abrir el archivo");
            System.err.println(io.getMessage());
        }
    }
    
    //Para poder sincronizar con los nombres
    public String getIdNodo(int indice){
        return this.idNodo[indice];
    }

    public void muestraMatriz(){    //(NO RECOMEDABLE USAR, MATRIZ DEMASIADO GRANDE) METODO PARA PURO TESTEO, ELIMINAR LUEGO
        System.out.print("ID_Nodo");
        for(int k=0; k < this.idNodo.length; k++){//Muestra todos los ID de los Nodos
            if(k ==  0) System.out.print(this.idNodo[k]);
            else System.out.print(","+this.idNodo[k]);
        }
        System.out.println();
        
        for(int i=0; i < this.matriz.length; i++){
            System.out.println(this.idNodo[i+1]);
            for(int j=0; j < this.matriz.length; j++){
                    System.out.print(","+this.matriz[i][j]);
            }
            System.out.println();
        }
    }
    
    public void muestraMatriz(int limite){ //(Matriz "recortada")METODO PARA PURO TESTEO, ELIMINAR LUEGO
        if(limite > idNodo.length){
            limite = idNodo.length;
        }
        System.out.print("ID_Nodo");
        for(int k=0; k < limite; k++){//Muestra todos los ID de los Nodos
            System.out.print(","+this.idNodo[k]);
        }
        System.out.println();
        
        for(int i=0; i < limite-1; i++){
            System.out.print(this.idNodo[i]);
            for(int j=0; j < limite-1; j++){
                    System.out.print(","+this.matriz[i][j]);
            }
            System.out.println();
        }
    }
    
    public int devolverConexion(int posicionFila, int posicionColumna){ //Devuelve si hay conexion o no desde la matriz
        int conec=0;

        if (posicionFila>=getNroFilas() || posicionFila<0){
                System.out.println("Error devuelve. Posicion fila inexistente ");
        }else{
                if (posicionColumna>=getNroColumnas() || posicionColumna<0){
                        System.out.println("Error devuelve. Posicion columna inexistente ");
                }else{
                        conec = this.matriz[posicionFila][posicionColumna];
                }				
        }		
        return conec;
    }
    
    public int devolverConexion(String id1, String id2){ //Devuelve si hay conexion o no dados 2 ID's (Destino a Origen)
        boolean estaId1 = false;
        boolean estaId2 = false;
        boolean encontrados = false;
        boolean primeroId1 = false;
        int aux = 0;
        
        int i = 0;
        while(i < this.idNodo.length && !encontrados){
            if(this.idNodo[i].equals(id1) || this.idNodo[i].equals(id2)){
                if(this.idNodo[i].equals(id1)){
                    if(!estaId2){
                        primeroId1 = true;
                        aux = i;
                    }
                    estaId1 = true;
                }else{
                    if(!estaId1){
                        aux = i;
                    }
                    estaId2 = true;
                }
            }
            if(estaId1 && estaId2){
                encontrados = true;
            }
            i++;
        }
        
        if(encontrados){
            if(primeroId1){
                return this.matriz[aux][i-1];
            }else{
                return this.matriz[i-1][aux];
            }
        }else{
            if(estaId1){
                System.out.println("ERROR: "+id2+" es invalido");
            }else{
                System.out.println("ERROR: "+id1+" es invalido");
            }
            return -1;
        }
        
    }
    
    public String getConexionIDs(int posicionFila, int posicionColumna){//Decuelve los ID's de una conexion
        //Sera necesario comprobar si hay conexion?
        if(posicionFila < getNroFilas() && posicionColumna < getNroColumnas()){
            return this.idNodo[posicionFila]+", "+this.idNodo[posicionColumna];
        }else{
            return "Fuera de rango";
        }
    }
}
