import java.io.*;
import java.util.*;

public class TransazioniParser {

    public static List<Transazione> extractTransazioni(String filename) throws IOException {
        List<Transazione> lista = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        Transazione current = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("\"causale_cbi\"")) {
                int start = line.indexOf(":") + 2;
                int end = line.lastIndexOf("\"");
                current = new Transazione("", line.substring(start, end));
            } else if (line.startsWith("\"descrizione\"")) {
                int start = line.indexOf(":") + 2;
                int end = line.lastIndexOf("\"");
                if (current != null) {
                    current.descrizione = line.substring(start, end);
                    lista.add(current);
                }
            }
        }

        br.close();
        return lista;
    }
}
