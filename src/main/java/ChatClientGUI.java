import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

public class ChatClientGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String name;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClientGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.addActionListener(this::sendMessage);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void startClient() {
        try {
            String serverIp = JOptionPane.showInputDialog(frame, "Enter Server IP:", "Server Connection", JOptionPane.QUESTION_MESSAGE);
            if (serverIp == null || serverIp.trim().isEmpty()) return;

            Socket socket = new Socket(serverIp, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            name = JOptionPane.showInputDialog(frame, "Enter Your Name:", "User Info", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) {
                name = "Anonymous";
            }
            out.println(name);

            // Start listening for messages
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            showError("Connection failed. Please check the server IP.");
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.contains(name + ":")) {
                    appendMessage("🟢 you: " + serverMessage.substring(serverMessage.indexOf(":") + 1));
                } else {
                    appendMessage(serverMessage);
                }
            }
        } catch (IOException e) {
            appendMessage("⚠️ Connection closed.");
        }
    }

    private void sendMessage(ActionEvent event) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientGUI client = new ChatClientGUI();
            client.startClient();
        });
    }
}
