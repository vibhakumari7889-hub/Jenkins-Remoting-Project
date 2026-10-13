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
                System.getenv().getOrDefault("PORT", "8081"));

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                send(exchange, 405, "Method not allowed",
                        "text/plain; charset=UTF-8");
                return;
            }

            String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="theme-color" content="#f8fafc">
                  <title>Java Remoting | TCP Messaging</title>
                  <style>
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      font-family: "Segoe UI", Arial, sans-serif;
                      background: #f4f7fb;
                      color: #172033;
                      min-height: 100vh;
                    }
                    header {
                      background: #fff;
                      border-bottom: 1px solid #e5eaf2;
                      padding: 18px 24px;
                    }
                    .nav {
                      max-width: 920px;
                      margin: auto;
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      gap: 12px;
                    }
                    .brand { font-weight: 700; font-size: 17px; }
                    .tag {
                      font-size: 12px;
                      color: #475569;
                      background: #f1f5f9;
                      border-radius: 20px;
                      padding: 7px 11px;
                    }
                    main {
                      max-width: 760px;
                      margin: 58px auto;
                      padding: 0 20px;
                    }
                    .eyebrow {
                      color: #2563eb;
                      text-transform: uppercase;
                      letter-spacing: 1.5px;
                      font-size: 12px;
                      font-weight: 700;
                    }
                    h1 {
                      font-size: clamp(30px, 5vw, 43px);
                      line-height: 1.15;
                      margin: 12px 0;
                      letter-spacing: -1.2px;
                    }
                    .intro {
                      color: #64748b;
                      line-height: 1.7;
                      max-width: 590px;
                      margin-bottom: 28px;
                    }
                    .panel {
                      background: white;
                      border: 1px solid #e2e8f0;
                      border-radius: 14px;
                      padding: 26px;
                      box-shadow: 0 8px 28px #0f172a08;
                    }
                    h2 { font-size: 18px; margin: 0 0 8px; }
                    .hint {
                      color: #64748b;
                      font-size: 14px;
                      line-height: 1.6;
                      margin: 0 0 20px;
                    }
                    label {
                      display: block;
                      font-weight: 600;
                      font-size: 14px;
                      margin-bottom: 9px;
                    }
                    textarea {
                      display: block;
                      width: 100%;
                      min-height: 118px;
                      resize: vertical;
                      border: 1px solid #cbd5e1;
                      border-radius: 9px;
                      padding: 13px;
                      font: inherit;
                      font-size: 14px;
                      line-height: 1.5;
                      outline: none;
                    }
                    textarea:focus {
                      border-color: #2563eb;
                      box-shadow: 0 0 0 3px #2563eb1a;
                    }
                    .actions {
                      display: flex;
                      justify-content: space-between;
                      align-items: center;
                      gap: 12px;
                      margin-top: 14px;
                    }
                    .counter { font-size: 12px; color: #64748b; }
                    button {
                      border: 0;
                      border-radius: 8px;
                      background: #1d4ed8;
                      color: white;
                      font: inherit;
                      font-weight: 600;
                      padding: 12px 18px;
                      cursor: pointer;
                    }
                    button:hover { background: #1e40af; }
                    button:disabled { opacity: .6; cursor: wait; }
                    .response {
                      margin-top: 22px;
                      border: 1px solid #e2e8f0;
                      background: #f8fafc;
                      border-radius: 9px;
                      padding: 16px;
                    }
                    .response-head {
                      display: flex;
                      justify-content: space-between;
                      gap: 10px;
                      align-items: center;
                      margin-bottom: 12px;
                    }
                    .status { font-size: 12px; color: #64748b; }
                    pre {
                      margin: 0;
                      min-height: 24px;
                      white-space: pre-wrap;
                      overflow-wrap: anywhere;
                      color: #334155;
                      font: 14px/1.6 Consolas, monospace;
                    }
                    .details {
                      display: grid;
                      grid-template-columns: repeat(2, minmax(0, 1fr));
                      gap: 12px;
                      margin-top: 16px;
                    }
                    .detail {
                      padding: 14px;
                      border: 1px solid #e2e8f0;
                      border-radius: 9px;
                    }
                    .detail strong { display: block; font-size: 13px; }
                    .detail span {
                      display: block;
                      color: #64748b;
                      font-size: 12px;
                      margin-top: 6px;
                    }
                    footer {
                      text-align: center;
                      color: #94a3b8;
                      font-size: 12px;
                      padding: 24px;
                    }
                    @media (max-width: 520px) {
                      main { margin: 36px auto; }
                      .panel { padding: 19px; }
                      .actions { align-items: flex-start; flex-direction: column; }
                      button { width: 100%; }
                    }
                  </style>
                </head>
                <body>
                  <header>
                    <div class="nav">
                      <div class="brand">Java Remoting</div>
                      <span class="tag">Java TCP · HTTP</span>
                    </div>
                  </header>
                  <main>
                    <div class="eyebrow">Networking project</div>
                    <h1>Java TCP Messaging</h1>
                    <p class="intro">
                      Send a message from your browser and view the response
                      returned by the Java TCP server through an HTTP interface.
                    </p>
                    <section class="panel">
                      <h2>Send a message</h2>
                      <p class="hint">
                        Enter a message below. The web server forwards it to the
                        TCP server and displays the server's response.
                      </p>
                      <form id="messageForm">
                        <label for="message">Message</label>
                        <textarea id="message" maxlength="1000" required
                          placeholder="Type your message here..."></textarea>
                        <div class="actions">
                          <span class="counter" id="counter">0 / 1000 characters</span>
                          <button id="sendButton" type="submit">Send message</button>
                        </div>
                      </form>
                      <div class="response" aria-live="polite">
                        <div class="response-head">
                          <strong>Server response</strong>
                          <span class="status" id="status">Waiting for message</span>
                        </div>
                        <pre id="response">Your server response will appear here.</pre>
                      </div>
                      <div class="details">
                        <div class="detail">
                          <strong>Transport</strong>
                          <span>HTTP request to TCP client</span>
                        </div>
                        <div class="detail">
                          <strong>Response source</strong>
                          <span>Java TCP server</span>
                        </div>
                      </div>
                    </section>
                  </main>
                  <footer>Java Remoting Project · Built with Java</footer>
                  <script>
                    const form = document.getElementById('messageForm');
                    const input = document.getElementById('message');
                    const response = document.getElementById('response');
                    const status = document.getElementById('status');
                    const button = document.getElementById('sendButton');
                    const counter = document.getElementById('counter');

                    input.addEventListener('input', () => {
                      counter.textContent = input.value.length + ' / 1000 characters';
                    });

                    form.addEventListener('submit', async (event) => {
                      event.preventDefault();
                      if (!input.value.trim()) {
                        status.textContent = 'Message required';
                        response.textContent = 'Enter a message before sending.';
                        return;
                      }

                      button.disabled = true;
                      status.textContent = 'Sending...';
                      response.textContent = 'Waiting for TCP server response...';

                      try {
                        const result = await fetch('/message', {
                          method: 'POST',
                          headers: { 'Content-Type': 'text/plain; charset=UTF-8' },
                          body: input.value
                        });
                        const body = await result.text();
                        response.textContent = body;
                        status.textContent = result.ok ? 'Response received' : 'Request failed';
                      } catch (error) {
                        status.textContent = 'Connection error';
                        response.textContent = 'Could not reach the web server. Please try again.';
                      } finally {
                        button.disabled = false;
                      }
                    });
                  </script>
                </body>
                </html>
                """;

            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            send(exchange, 200, html, "text/html; charset=UTF-8");
        });

        server.createContext("/message", exchange -> {
            if (!exchange.getRequestMethod().equals("POST")) {
                send(exchange, 405, "Method not allowed",
                        "text/plain; charset=UTF-8");
                return;
            }

            String message;
            try (InputStream in = exchange.getRequestBody()) {
                message = new String(in.readNBytes(1001), StandardCharsets.UTF_8);
            }

            if (message.isBlank() || message.length() > 1000) {
                send(exchange, 400, "Enter a message of 1 to 1000 characters.",
                        "text/plain; charset=UTF-8");
                return;
            }

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(
                        InetAddress.getLoopbackAddress(), TCP_PORT), 3000);
                socket.setSoTimeout(3000);

                try (PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(),
                                StandardCharsets.UTF_8), true);
                     BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(),
                                StandardCharsets.UTF_8))) {
                    out.println(MessageProtocol.decode(
                            MessageProtocol.encode(message)));
                    String reply = in.readLine();
                    send(exchange, reply == null ? 502 : 200,
                            reply == null ? "No response from server." : reply,
                            "text/plain; charset=UTF-8");
                }
            } catch (IOException e) {
                send(exchange, 502, "Java TCP server is unavailable.",
                        "text/plain; charset=UTF-8");
            }
        });

        server.start();
        System.out.println("Web server running on port " + port);
    }

    private static void send(HttpExchange exchange, int status,
                             String message, String contentType)
            throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
