
package com.example.remoting;

import java.nio.charset.StandardCharsets;

public final class MessageProtocol {

    private MessageProtocol() {
    }

    public static byte[] encode(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String decode(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        return new String(data, StandardCharsets.UTF_8);
    }
}
