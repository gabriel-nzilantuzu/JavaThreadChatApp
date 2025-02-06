import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        try {
            System.out.print("Enter server IP: ");
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String serverIp = consoleInput.readLine();

            Socket socket = new Socket(serverIp, 12345);
            System.out.println("Connected to the chat server.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your name: ");
            String name = userInput.readLine();
            out.println(name);

            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.contains(name + ":")) {
                            System.out.println("you: " + serverMessage.substring(serverMessage.indexOf(":") + 1));
                        } else {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });

            receiveThread.start();

            String clientMessage;
            while ((clientMessage = userInput.readLine()) != null) {
                out.println(clientMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}