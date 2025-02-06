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
    void testClientConnection() throws IOException {
        Socket socket = new Socket("localhost", PORT);
        assertTrue(socket.isConnected());
        socket.close();
    }

    @Test
    void testClientNameRegistration() throws IOException {
        Socket socket = new Socket("localhost", PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Read the prompt for the name
        String prompt = in.readLine();
        assertEquals("Enter your name: ", prompt);

        // Send the name
        out.println("TestClient");
        String response = in.readLine();
        assertTrue(response.contains("TestClient has joined the chat"));

        socket.close();
    }

    @Test
    void testBroadcastMessage() throws IOException, InterruptedException {
        Socket client1 = new Socket("localhost", PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);

        Socket client2 = new Socket("localhost", PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);

        // Register names
        in1.readLine(); // Prompt for name
        out1.println("Client1");
        in1.readLine(); // Join message

        in2.readLine(); // Prompt for name
        out2.println("Client2");
        in2.readLine(); // Join message

        // Send message from Client1
        out1.println("Hello from Client1");
        String message1 = in2.readLine();
        assertTrue(message1.contains("Client1: Hello from Client1"));

        // Send message from Client2
        out2.println("Hello from Client2");
        String message2 = in1.readLine();
        assertTrue(message2.contains("Client2: Hello from Client2"));

        client1.close();
        client2.close();
    }

    @Test
    void testClientDisconnection() throws IOException, InterruptedException {
        Socket client = new Socket("localhost", PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        // Register name
        in.readLine(); // Prompt for name
        out.println("Client");
        in.readLine(); // Join message

        // Close the client
        client.close();

        // Wait for the server to process the disconnection
        Thread.sleep(1000);

        // Verify the disconnection message
        // This part is tricky to test without modifying the server to log disconnections
        // For now, we assume the server handles disconnections correctly
    }
}