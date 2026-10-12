
package com.example.remoting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        String message = args.length > 0
                ? String.join(" ", args)
                : "Hello from MessageClient";

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter output = new PrintWriter(
                     socket.getOutputStream(), true,
                     StandardCharsets.UTF_8);
             BufferedReader input = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream(),
                             StandardCharsets.UTF_8))) {

            output.println(MessageProtocol.decode(
                    MessageProtocol.encode(message)));

            String response = input.readLine();
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.err.println("Could not communicate with server: "
                    + e.getMessage());
        }
    }
}
