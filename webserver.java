import java.io.*;
import java.net.*;

public class WebServer {
    public static void main(String[] args) {
        int port = 8080; // Use port 8080 as required

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Web Server is running on port " + port);

            while (true) {
                try (Socket client = server.accept();
                     BufferedReader infrom = new BufferedReader(new InputStreamReader(client.getInputStream()));
                     DataOutputStream outTo = new DataOutputStream(client.getOutputStream())) {

                    // Read the request line
                    String requestLine = infrom.readLine();
                    if (requestLine == null || requestLine.isEmpty()) {
                        sendBadRequest(outTo);
                        continue;
                    }

                    System.out.println("Request: " + requestLine);
                    String[] tokens = requestLine.split(" ");

                    // Validate HTTP request format
                    if (tokens.length < 2 || (!tokens[0].equals("GET") && !tokens[0].equals("HEAD"))) {
                        sendBadRequest(outTo);
                        continue;
                    }

                    String responseBody = "<html><h1>Dummy Web Server is Working</h1></html>";
                    sendResponse(outTo, responseBody);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void sendResponse(DataOutputStream outTo, String body) throws IOException {
        String statusLine = "HTTP/1.1 200 OK\r\n";
        String headers = "Content-Type: text/html\r\n" +
                         "Content-Length: " + body.length() + "\r\n\r\n";
        outTo.writeBytes(statusLine + headers + body);
    }

    private static void sendBadRequest(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 400 Bad Request\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = "<html><h1>400 Bad Request</h1></html>";
        outTo.writeBytes(statusLine + headers + body);
    }

    private static void sendNotFound(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 404 Not Found\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = "<html><h1>404 Not Found</h1></html>";
        outTo.writeBytes(statusLine + headers + body);
    }

    private static void sendForbidden(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 403 Forbidden\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = "<html><h1>403 Forbidden</h1></html>";
        outTo.writeBytes(statusLine + headers + body);
    }
}
    
