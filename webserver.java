import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class webserver {
    public static void main(String[] args) {
        int port = 8080; 

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
                    if (isIpBlocked(ipAddress)) {
                        sendBlocked(outTo, ipAddress);
                        client.close();
                        continue;
                    }

                    // Validate the requested file path
                    String filePath = tokens[1];
                    if (filePath.startsWith("/")) {
                        filePath = filePath.substring(1); 
                    }
                    if (filePath.isEmpty()) {
                        filePath = "index.html"; 
                    }

                    File file = new File(filePath); 
                    if (!file.exists()) {
                        sendNotFound(outTo);
                        continue;
                    }
                    
                    // Determine MIME type
                    String mimeType = getMimeType(filePath);

                    // Read file contents
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    
                    //  Send it to the client
                    sendFileResponse(outTo, fileContent, mimeType);
                    System.out.println("Response sent to client.");
                    System.out.println("Requested file path: " + filePath + "\nMIME type: " + mimeType);
                    client.close();

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
    
    private static boolean isIpBlocked(String ip) {
        return ip.equals("127.0.0.17") || ip.startsWith("10.");
    }

    private static void sendFileResponse(DataOutputStream outTo, byte[] fileContent, String mimeType) throws IOException {
        String statusLine = "HTTP/1.1 200 OK\r\n";
        String headers = "Content-Type: " + mimeType + "\r\n" +
                         "Content-Length: " + fileContent.length + "\r\n\r\n";
        outTo.writeBytes(statusLine + headers);
        outTo.write(fileContent);
    }
    
    private static void sendBadRequest(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 400 Bad Request\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = """
        <html>
        <head><title>400 Bad Request</title></head>
        <body style="font-family: Arial, sans-serif; text-align: center; margin-top: 50px;">
            <h1 style="color: red;">400 Bad Request</h1>
            <p>Your browser sent a request that this server could not understand.</p>
            <p>Please check your request and try again.</p>
        </body>
        </html>
        """;
        outTo.writeBytes(statusLine + headers + body);
    }

    private static void sendNotFound(DataOutputStream outTo) throws IOException {
        String statusLine = "HTTP/1.1 404 Not Found\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = """
        <html>
        <head><title>404 Not Found</title></head>
        <body style="font-family: Arial, sans-serif; text-align: center; margin-top: 50px;">
            <h1 style= "color: orange;">404 Not Found</h1>
            <p>The page you are looking for does not exist.</p>
            <p>Please check the URL and try again.</p>
        </body>
        </html>
        """;
        outTo.writeBytes(statusLine + headers + body);
    }

    private static void sendBlocked(DataOutputStream outTo, String ipAddress) throws IOException {
        String statusLine = "HTTP/1.1 403 Forbidden\r\n";
        String headers = "Content-Type: text/html\r\n\r\n";
        String body = """
        <html>
        <head><title>403 Forbidden</title></head>
        <body style="font-family: Arial, sans-serif; text-align: center; margin-top: 50px;">
            <h1 style="color: red;">403 Forbidden</h1>
            <p>Your IP address (%s) is blocked from accessing this server.</p>
        </body>
        </html>
        """.formatted(ipAddress);
        outTo.writeBytes(statusLine + headers + body);
    }

    private static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream"; 
        }
    }
}
