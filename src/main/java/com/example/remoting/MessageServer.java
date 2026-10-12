
package com.example.remoting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageServer {

    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Message server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is ready. Waiting for a client...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader input = new BufferedReader(
                             new InputStreamReader(
                                     clientSocket.getInputStream(),
                                     StandardCharsets.UTF_8));
                     PrintWriter output = new PrintWriter(
                             clientSocket.getOutputStream(), true,
                             StandardCharsets.UTF_8)) {

                    System.out.println("Client connected.");

                    String message = input.readLine();

                    if (message != null) {
                        System.out.println("Received: " + message);
                        output.println("Message received: " + message);
                    }
                } catch (IOException e) {
                    System.err.println("Client communication error: "
                            + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}
