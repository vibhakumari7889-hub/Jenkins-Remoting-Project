
package com.example.remoting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageProtocolTest {

    @Test
    void shouldEncodeAndDecodeMessage() {
        String original = "Hello Jenkins Remoting";

        byte[] encoded = MessageProtocol.encode(original);
        String decoded = MessageProtocol.decode(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void shouldSupportUnicodeMessages() {
        String original = "Hello नमस्ते";

        assertEquals(
                original,
                MessageProtocol.decode(MessageProtocol.encode(original))
        );
    }

    @Test
    void shouldRejectNullMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MessageProtocol.encode(null)
        );
    }

    @Test
    void shouldRejectNullData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MessageProtocol.decode(null)
        );
    }
}
