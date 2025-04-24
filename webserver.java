import java.io.*;
import java.net.*;

public class webserver {
    public static void main(String[] args) {
        int port = 8080; // Use port 8080 as required

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Web Server is running on port " + port);

            while (true) {
                try {
                    Socket client = server.accept();
                    System.out.println("Client connected: " + client.getInetAddress());
                    BufferedReader infrom = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    DataOutputStream outTo = new DataOutputStream(client.getOutputStream());

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
                    
                    String ipAddress = client.getInetAddress().getHostAddress();
                    if (ipAddress.contains("172.17.156.188") || ipAddress.contains("10.0.0.0")) {
                        sendBlocked(outTo);
                        continue;
                    }

                    File file = new File(tokens[1]);
                    if (!file.exists()) {
                        sendNotFound(outTo);
                        continue;
                    }

                    
                   
                    
                    

                    // Read the file and send it to the client
                    String responseBody = "<html><h1>Dummy Web Server is Working</h1></html>";
                    sendResponse(outTo, responseBody);
                    System.out.println("Response sent to client.");

                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
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

    private static void sendBlocked(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 403 Blocked\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = "<html><h1>403 Blocked</h1></html>";
        outTo.writeBytes(statusLine + headers + body);
    }
}
