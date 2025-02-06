import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat Server started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server running on: " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender, boolean includeSender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (includeSender || client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask client for their name
            out.print("Enter your name: ");
            out.flush(); // Ensure prompt is sent immediately
            this.clientName = in.readLine();

            if (clientName == null || clientName.trim().isEmpty()) {
                clientName = "Anonymous";
            }

            System.out.println(clientName + " has joined the chat.");
            ChatServer.broadcast("🔵 " + clientName + " has joined the chat!", this, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println(clientName + ": " + clientMessage);
                ChatServer.broadcast("💬 " + clientName + ": " + clientMessage, this, true);
            }
        } catch (IOException e) {
            System.out.println(clientName + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.broadcast("❌ " + clientName + " has left the chat.", this, true);
            ChatServer.removeClient(this);
        }
    }
}
