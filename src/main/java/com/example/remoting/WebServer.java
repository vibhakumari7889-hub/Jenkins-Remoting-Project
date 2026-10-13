
package com.example.remoting;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class WebServer {

    private static final int TCP_PORT = 5000;

    public static void main(String[] args) throws Exception {
        Thread tcpServer = new Thread(() -> {
            try {
                MessageServer.main(new String[0]);
            } catch (Exception e) {
                System.err.println("TCP server error: " + e.getMessage());
            }
        });
        tcpServer.setDaemon(true);
        tcpServer.start();

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {
            String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport"
                        content="width=device-width, initial-scale=1">
                  <title>Java Remoting Project</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      max-width: 700px;
                      margin: 60px auto;
                      padding: 20px;
                      background: #f4f7fb;
                      color: #172554;
                    }
                    main {
                      background: white;
                      padding: 28px;
                      border-radius: 12px;
                      box-shadow: 0 4px 18px #0001;
                    }
                    textarea {
                      width: 100%;
                      box-sizing: border-box;
                      padding: 12px;
                      margin: 12px 0;
                    }
                    button {
                      background: #1d4ed8;
                      color: white;
                      padding: 12px 20px;
                      border: 0;
                      border-radius: 6px;
                      cursor: pointer;
                    }
                    pre {
                      white-space: pre-wrap;
                      overflow-wrap: anywhere;
                    }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Java Remoting Project</h1>
                    <p>
                      Send a message to the Java TCP server
                      through this web interface.
                    </p>
                    <form id="messageForm">
                      <label for="message">Your message</label>
                      <textarea id="message" maxlength="1000"
                        required rows="4"
                        placeholder="Enter a message"></textarea>
                      <button type="submit">Send Message</button>
                    </form>
                    <h2>Server Response</h2>
                    <pre id="response">No message sent yet.</pre>
                  </main>
                  <script>
                    const form = document.getElementById('messageForm');
                    const response = document.getElementById('response');

                    form.addEventListener('submit', async (event) => {
                      event.preventDefault();
                      response.textContent = 'Sending...';

                      try {
                        const result = await fetch('/message', {
                          method: 'POST',
                          headers: {
                            'Content-Type': 'text/plain; charset=UTF-8'
                          },
                          body: document.getElementById('message').value
                        });

                        response.textContent = await result.text();
                      } catch (error) {
                        response.textContent =
                          'Request failed. Please try again.';
                      }
                    });
                  </script>
                </body>
                </html>
                """;

            if (!exchange.getRequestMethod().equals("GET")) {
                send(exchange, 405, "Method not allowed");
                return;
            }

            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });

        server.createContext("/message", exchange -> {
            if (!exchange.getRequestMethod().equals("POST")) {
                send(exchange, 405, "Method not allowed");
                return;
            }

            String message;
            try (InputStream in = exchange.getRequestBody()) {
                message = new String(
                        in.readNBytes(1001), StandardCharsets.UTF_8);
            }

            if (message.isBlank() || message.length() > 1000) {
                send(exchange, 400,
                        "Enter a message of 1 to 1000 characters.");
                return;
            }

            try (Socket socket = new Socket(
                    InetAddress.getLoopbackAddress(), TCP_PORT);
                 PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8),
                    true);
                 BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8))) {

                out.println(MessageProtocol.decode(
                        MessageProtocol.encode(message)));

                String reply = in.readLine();
                send(exchange, 200,
                        reply == null ? "No response from server." : reply);
            } catch (IOException e) {
                send(exchange, 502, "Java TCP server is unavailable.");
            }
        });

        server.start();
        System.out.println("Web server running on port " + port);
    }

    private static void send(
            HttpExchange exchange, int status, String message)
            throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
