package interfaz;

//CALE SOLO PARA OBTENER LAS COORDENADAS SUP IZQUIERDA Y INF DERECHA DEL MAPA, NO TIENE USO EN EL PROGRAMA
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Area {
    public static void main(String[] args) {
        InputStream fIn;
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);

        try {  
            fIn = new FileInputStream("src/main/java/datos/meta_datos_nodos_2k.csv");
            isr = new InputStreamReader(fIn);
            buffer = new BufferedReader(isr);

            
            buffer.readLine();
            String linea = buffer.readLine();
            String[] partes = linea.split(",");
            double latmenor = Double.parseDouble(partes[1]);
            double latmayor = Double.parseDouble(partes[1]);
            double longmenor = Double.parseDouble(partes[2]);
            double longmayor = Double.parseDouble(partes[2]);


            while ((linea = buffer.readLine()) != null) {//Leo linea por linea del archivo .csv
                partes = linea.split(",");
                    try {
                        if(latmenor > Double.parseDouble(partes[1])) latmenor = Double.parseDouble(partes[1]);
                        if(latmayor < Double.parseDouble(partes[1])) latmayor = Double.parseDouble(partes[1]);
                        if(longmenor > Double.parseDouble(partes[2])) longmenor = Double.parseDouble(partes[2]);
                        if(longmayor < Double.parseDouble(partes[2])) longmayor = Double.parseDouble(partes[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: La cadena no contiene un número válido.");
                    }
            }

            fIn.close();

            System.out.println("Esquina 1:("+latmenor+","+longmenor+")");//Esquina 1:(-24.8119525,-65.4363349)
            System.out.println("Esquina 1:("+latmayor+","+longmayor+")");//Esquina 1:(-24.7670606,-65.3907366)
            
        } catch (IOException io) {
            //System.err.println("No se puede abrir el archivo");
            System.err.println(io.getMessage());
        }

        System.out.println("Circunferencia circunscrita del mapa: "+calcularHaversine(-24.8119525,-65.4363349,-24.7670606,-65.3907366)/2+"m");
        
    }

    private static double calcularHaversine(double lon1, double lat1, double lon2, double lat2) {
        double radioTierra = 6371000; // Radio de la Tierra en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radioTierra * c;
    }
}
