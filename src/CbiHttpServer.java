import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;



public class CbiHttpServer {

    private static final int SERVER_PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final String API_ENDPOINT_PATH = "/api/findCbiId";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext(API_ENDPOINT_PATH, new CbiHandler());
        server.setExecutor(null); // Usa un executor di default
        System.out.println("Server avviato sulla porta " + SERVER_PORT + ". Endpoint: " + API_ENDPOINT_PATH);
        System.out.println("Esempio di richiesta: http://localhost:" + SERVER_PORT + API_ENDPOINT_PATH + "?description=Bonifico%20stipendio");
        server.start();
    }

    static class CbiHandler implements HttpHandler {
        private final Map<String, CbiEntry> cbiMap;

        public CbiHandler() {
            // Inizializza la mappa CBI una sola volta
            this.cbiMap = Codeid.getCbiMap();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            int statusCode = 200;

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String descriptionParam = null;

                if (query != null) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        int idx = pair.indexOf("=");
                        if (idx > 0 && "description".equals(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()))) {
                            descriptionParam = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                            break;
                        }
                    }
                }

                if (descriptionParam != null && !descriptionParam.trim().isEmpty()) {
                    try {
                        // 1. Estrai la parte significativa della descrizione
                        String meaningfulPart = CbiMatcher.extractInitialMeaningfulPart(descriptionParam);
                        
                        // 2. Trova l'ID CBI più vicino usando la parte significativa
                        if (meaningfulPart.isEmpty()) {
                            // Se la parte significativa è vuota (es. solo numeri o simboli),
                            // usa un input di default o gestisci come errore.
                            // Qui usiamo la descrizione originale in lowercase come fallback,
                            // oppure potremmo restituire l'ID di default "50" direttamente.
                            // Per ora, tentiamo con la descrizione originale processata da findClosestCbiId.
                             response = CbiMatcher.findClosestCbiId(descriptionParam, cbiMap);
                        } else {
                             response = CbiMatcher.findClosestCbiId(meaningfulPart, cbiMap);
                        }

                    } catch (Exception e) {
                        response = "Errore durante l'elaborazione della richiesta: " + e.getMessage();
                        statusCode = 500;
                        e.printStackTrace();
                    }
                } else {
                    response = "Parametro 'description' mancante o vuoto.";
                    statusCode = 400;
                }
            } else {
                response = "Metodo non supportato. Usare GET.";
                statusCode = 405; // Method Not Allowed
            }

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=" + StandardCharsets.UTF_8.name());
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Per test locali, abilita CORS
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
