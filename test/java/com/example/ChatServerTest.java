import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {
    private static final int PORT = 12345;
    private static ExecutorService executorService;

    @BeforeAll
    static void startServer() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> ChatServer.main(new String[]{}));
    }

    @AfterAll
    static void stopServer() {
        executorService.shutdownNow();
    }

    @Test
    void testMultipleClients() throws IOException, InterruptedException {
        Socket[] clients = new Socket[5];
        BufferedReader[] in = new BufferedReader[5];
        PrintWriter[] out = new PrintWriter[5];
        
        for (int i = 0; i < 5; i++) {
            clients[i] = new Socket("localhost", PORT);
            in[i] = new BufferedReader(new InputStreamReader(clients[i].getInputStream()));
            out[i] = new PrintWriter(clients[i].getOutputStream(), true);

            in[i].readLine();  // Wait for the prompt
            out[i].println("Client" + (i + 1));  // Send the client's name
            in[i].readLine();  // Wait for the join confirmation
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i != j) {
                    out[i].println("Message from Client" + (i + 1));
                    String message = in[j].readLine();
                    assertTrue(message.contains("Client" + (i + 1) + ": Message from Client" + (i + 1)));
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            clients[i].close();
        }

        Thread.sleep(1000);
        
        stopServer();
    }
}
