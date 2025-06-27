package Chat;

import OOP.Message;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.*;

public class ChatConnectionManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ChatCenterController chatCenterController;
    private boolean running;
    private int userId;

    public ChatConnectionManager(String host, int port, int userId, ChatCenterController controller) throws Exception {
        System.out.println("Attempting to connect to server at " + host + ":" + port + " for userId: " + userId);
        this.chatCenterController = controller;
        this.userId = userId;
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(0);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            running = true;

            Message joinMessage = new Message("JOIN_CHAT", userId, null, null);
            out.writeObject(joinMessage);
            out.flush();
            System.out.println("Sent JOIN_CHAT message for userId: " + userId);

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            closeConnection();
            throw new Exception("Không thể kết nối đến server: " + e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void sendMessage(Message message) {
        if (!isConnected()) {
            System.err.println("Cannot send message: No connection to server");
            showErrorAlert("Mất kết nối với server. Vui lòng thử lại.");
            return;
        }
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
                System.out.println("Sent message: command=" + message.getCommand() + ", messageId=" + message.getMessageId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to send message: " + e.getMessage());
            showErrorAlert("Không thể gửi tin nhắn: " + e.getMessage());
            closeConnection();
        }
    }

    private void receiveMessages() {
        while (running && isConnected()) {
            try {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    System.out.println("Received message: command=" + message.getCommand() + ", messageId=" + message.getMessageId());
                    if ("SEND_MESSAGE".equals(message.getCommand()) ||
                            "ICON_MESSAGE".equals(message.getCommand()) ||
                            "IMAGE_MESSAGE".equals(message.getCommand()) ||
                            "FILE_MESSAGE".equals(message.getCommand()) ||
                            "AUDIO_RECORD_MESSAGE".equals(message.getCommand()) ||
                            "ENCRYPTED_FILE_MESSAGE".equals(message.getCommand()) ||
                            "BROADCAST_MESSAGE".equals(message.getCommand()) ||
                            "DELETE_MESSAGE".equals(message.getCommand()) ||
                            "SHARE_DECK".equals(message.getCommand())) {
                        chatCenterController.addMessage(message);
                    } else if ("JOIN_SUCCESS".equals(message.getCommand())) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "Đã tham gia phòng chat!",
                                    ButtonType.OK);
                            alert.showAndWait();
                        });
                    } else {
                        System.err.println("Unknown command: " + message.getCommand());
                    }
                }
            } catch (EOFException | SocketException e) {
                System.err.println("Connection lost: " + e.getMessage());
                Platform.runLater(() -> showErrorAlert("Mất kết nối với server."));
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void closeConnection() {
        running = false;
        try {
            if (isConnected()) {
                Message leaveMessage = new Message("LEAVE_CHAT", userId, null, null);
                synchronized (out) {
                    out.writeObject(leaveMessage);
                    out.flush();
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
                System.out.println("Connection closed for userId: " + userId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.showAndWait();
        });
    }
}


//
//
//
//package Chat;
//
//import OOP.Message;
//import javafx.application.Platform;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ButtonType;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//
//import java.io.*;
//import java.net.*;
//
//public class ChatConnectionManager {
//    private Socket socket;
//    private ObjectOutputStream out;
//    private ObjectInputStream in;
//    private ChatCenterController chatCenterController;
//    private boolean running;
//    private int userId;
//
//    public ChatConnectionManager(String host, int port, int userId, ChatCenterController controller) throws Exception {
//        System.out.println("Attempting to connect to server at " + host + ":" + port + " for userId: " + userId);
//        this.chatCenterController = controller;
//        this.userId = userId;
//        try {
//            socket = new Socket(host, port);
//            socket.setSoTimeout(0);
//            out = new ObjectOutputStream(socket.getOutputStream());
//            in = new ObjectInputStream(socket.getInputStream());
//            running = true;
//
//            Message joinMessage = new Message("JOIN_CHAT", userId, null, null);
//            out.writeObject(joinMessage);
//            out.flush();
//            System.out.println("Sent JOIN_CHAT message for userId: " + userId);
//
//            new Thread(this::receiveMessages).start();
//        } catch (IOException e) {
//            System.err.println("Failed to connect to server: " + e.getMessage());
//            closeConnection();
//            throw new Exception("Không thể kết nối đến server: " + e.getMessage(), e);
//        }
//    }
//
//    public boolean isConnected() {
//        return socket != null && !socket.isClosed() && socket.isConnected();
//    }
//
//    public void sendMessage(Message message) {
//        if (!isConnected()) {
//            System.err.println("Cannot send message: No connection to server");
//            showErrorAlert("Mất kết nối với server. Vui lòng thử lại.");
//            return;
//        }
//        try {
//            synchronized (out) {
//                out.writeObject(message);
//                out.flush();
//                System.out.println("Sent message: command=" + message.getCommand() + ", messageId=" + message.getMessageId());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Failed to send message: " + e.getMessage());
//            showErrorAlert("Không thể gửi tin nhắn: " + e.getMessage());
//            closeConnection();
//        }
//    }
//
//    private void receiveMessages() {
//        while (running && isConnected()) {
//            try {
//                Object obj = in.readObject();
//                if (obj instanceof Message) {
//                    Message message = (Message) obj;
//                    System.out.println("Received message: command=" + message.getCommand() + ", messageId=" + message.getMessageId());
//                    if ("SEND_MESSAGE".equals(message.getCommand()) ||
//                            "ICON_MESSAGE".equals(message.getCommand()) ||
//                            "IMAGE_MESSAGE".equals(message.getCommand()) ||
//                            "FILE_MESSAGE".equals(message.getCommand()) ||
//                            "AUDIO_RECORD_MESSAGE".equals(message.getCommand()) ||
//                            "ENCRYPTED_FILE_MESSAGE".equals(message.getCommand()) ||
//                            "BROADCAST_MESSAGE".equals(message.getCommand()) ||
//                            "DELETE_MESSAGE".equals(message.getCommand())) {
//                        chatCenterController.addMessage(message);
//                    } else if ("JOIN_SUCCESS".equals(message.getCommand())) {
//                        Platform.runLater(() -> {
//                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
//                                    "Đã tham gia phòng chat!",
//                                    ButtonType.OK);
//                            alert.showAndWait();
//                        });
//                    } else {
//                        System.err.println("Unknown command: " + message.getCommand());
//                    }
//                }
//            } catch (EOFException | SocketException e) {
//                System.err.println("Connection lost: " + e.getMessage());
//                Platform.runLater(() -> showErrorAlert("Mất kết nối với server."));
//                break;
//            } catch (Exception e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//    }
//
//
//    public void closeConnection() {
//        running = false;
//        try {
//            if (isConnected()) {
//                Message leaveMessage = new Message("LEAVE_CHAT", userId, null, null);
//                synchronized (out) {
//                    out.writeObject(leaveMessage);
//                    out.flush();
//                }
//            }
//        } catch (Exception ignored) {
//        } finally {
//            try {
//                if (in != null) in.close();
//                if (out != null) out.close();
//                if (socket != null) socket.close();
//                System.out.println("Connection closed for userId: " + userId);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void showErrorAlert(String message) {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
//            alert.showAndWait();
//        });
//    }
//}