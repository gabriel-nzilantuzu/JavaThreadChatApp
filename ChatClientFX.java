import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

public class ChatClientFX extends Application {
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private String name;
    private Socket socket;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Client");

        // Chat area (non-editable)
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: #f4f4f4;");

        // Input field
        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("-fx-font-size: 14px;");

        // Send button
        sendButton = new Button("📤 Send");
        sendButton.setStyle("-fx-font-size: 14px;");
        sendButton.setOnAction(e -> sendMessage());

        // Press Enter to send message
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Layout setup
        HBox inputBox = new HBox(10, messageField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setHgrow(messageField, Priority.ALWAYS);

        VBox root = new VBox(10, chatArea, inputBox);
        root.setPadding(new Insets(10));

        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.show();

        // Start client logic
        startClient(primaryStage);
    }

    private void startClient(Stage primaryStage) {
        try {
            // Get Server IP
            TextInputDialog ipDialog = new TextInputDialog("192.168.X.X");
            ipDialog.setTitle("Server Connection");
            ipDialog.setHeaderText("Enter Server IP:");
            String serverIp = ipDialog.showAndWait().orElse("");
            if (serverIp.trim().isEmpty()) return;

            // Connect to server
            socket = new Socket(serverIp, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Get Username
            TextInputDialog nameDialog = new TextInputDialog("Anonymous");
            nameDialog.setTitle("User Info");
            nameDialog.setHeaderText("Enter Your Name:");
            name = nameDialog.showAndWait().orElse("Anonymous");
            out.println(name);

            // Start listening for messages
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            showError("❌ Connection failed! Check the server IP.");
        }
    }

    private void receiveMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.startsWith(name + ":")) {
                    appendMessage("🟢 You: " + serverMessage.substring(serverMessage.indexOf(":") + 1), "blue");
                } else {
                    appendMessage(serverMessage, "black");
                }
            }
        } catch (IOException e) {
            appendMessage("⚠️ Connection closed.", "red");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void appendMessage(String message, String color) {
        chatArea.appendText(message + "\n");
    }

    private void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(errorMessage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
