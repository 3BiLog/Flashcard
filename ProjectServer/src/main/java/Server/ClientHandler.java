package Server;

import OOP.*;
import database.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final ConcurrentHashMap<Integer, ObjectOutputStream> clientOutputs = new ConcurrentHashMap<>();
    private Integer currentUserId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            socket.setSoTimeout(0);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            while (!socket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof User) {
                        handleCommand((User) obj);
                    } else if (obj instanceof boThe) {
                        handleBoThe((boThe) obj);
                    } else if (obj instanceof Card) {
                        handleCard((Card) obj);
                    } else if (obj instanceof Stat) {
                        handleStat((Stat) obj);
                    } else if (obj instanceof Message) {
                        handleMessage((Message) obj);
                    } else {
                        sendResponse("INVALID_REQUEST");
                    }
                } catch (EOFException | SocketException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) throws IOException {
        if ("JOIN_CHAT".equals(message.getCommand())) {
            handleJoinChat(message);
        } else if ("SEND_MESSAGE".equals(message.getCommand()) ||
                "ICON_MESSAGE".equals(message.getCommand()) ||
                "IMAGE_MESSAGE".equals(message.getCommand()) ||
                "FILE_MESSAGE".equals(message.getCommand()) ||
                "AUDIO_RECORD_MESSAGE".equals(message.getCommand()) ||
                "ENCRYPTED_FILE_MESSAGE".equals(message.getCommand()) ||
                "SHARE_DECK".equals(message.getCommand())) {
            handleSendMessage(message);
        } else if ("LEAVE_CHAT".equals(message.getCommand())) {
            handleLeaveChat(message);
        } else if ("DELETE_MESSAGE".equals(message.getCommand())) {
            handleDeleteMessage(message);
        } else {
            sendResponse("UNKNOWN_COMMAND: " + message.getCommand());
        }
    }

    private void handleJoinChat(Message message) throws IOException {
        currentUserId = message.getUserId();
        clientOutputs.put(currentUserId, out);
        sendResponse(new Message("JOIN_SUCCESS", 0, "System", null));
        System.out.println("User joined: userId=" + currentUserId);
    }

    private void handleSendMessage(Message message) throws IOException {
        String messageId = message.getMessageId() != null ? message.getMessageId() : java.util.UUID.randomUUID().toString();
        Message broadcastMessage = new Message(
                message.getCommand(),
                message.getUserId(),
                message.getUsername(),
                message.getContent(),
                message.getFileData(),
                message.getFileName(),
                message.getFileType(),
                message.getTimestamp(),
                messageId
        );
        System.out.println("Broadcasting message: command=" + broadcastMessage.getCommand() +
                ", messageId=" + messageId +
                ", userId=" + broadcastMessage.getUserId());
        broadcastMessage(broadcastMessage);
        sendResponse("SEND_SUCCESS");
    }

    private void handleDeleteMessage(Message message) throws IOException {
        System.out.println("Handling DELETE_MESSAGE: messageId=" + message.getMessageId() +
                ", userId=" + message.getUserId() +
                ", username=" + message.getUsername());
        Message deleteMessage = new Message("DELETE_MESSAGE", message.getMessageId(), message.getUserId(), message.getUsername());
        broadcastMessage(deleteMessage);
        sendResponse("DELETE_SUCCESS");
    }

    private void handleLeaveChat(Message message) throws IOException {
        if (currentUserId != null) {
            clientOutputs.remove(currentUserId);
            broadcastMessage(new Message("BROADCAST_MESSAGE", 0, "System",
                    message.getUsername() + " đã rời phòng chat!"));
        }
        sendResponse("SUCCESS");
    }

    private void broadcastMessage(Message message) {
        List<Integer> disconnectedClients = new ArrayList<>();
        for (Map.Entry<Integer, ObjectOutputStream> entry : clientOutputs.entrySet()) {
            try {
                synchronized (entry.getValue()) {
                    entry.getValue().writeObject(message);
                    entry.getValue().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                disconnectedClients.add(entry.getKey());
            }
        }
        for (Integer userId : disconnectedClients) {
            clientOutputs.remove(userId);
        }
    }

    private void handleCommand(User request) throws IOException {
        switch (request.getCommand()) {
            case "LOGIN":
                int userId = new LoginController().authenticate(request.getUsername(), request.getPassword());
                if (userId > 0) {
                    User resp = new User("LOGIN_RESPONSE", userId, request.getUsername(), request.getEmail(), null);
                    sendResponse(resp);
                } else {
                    sendResponse(null);
                }
                break;
            case "REGISTER":
                boolean success = new RegisterController().register(
                        request.getUsername(), request.getEmail(), request.getPassword());
                sendResponse(success ? "SUCCESS" : "FAIL");
                break;
            case "CHECK_EMAIL":
                try {
                    boolean exists = new LoginController().checkEmailExists(request.getEmail());
                    sendResponse(exists);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendResponse(false);
                }
                break;
            case "RESET_PASSWORD":
                try {
                    boolean resetOk = new LoginController().resetPassword(request.getEmail(), request.getPassword());
                    sendResponse(resetOk);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendResponse(false);
                }
                break;
            case "GET_USER":
                User user = new ProfileController().getUserById(request.getId());
                sendResponse(user);
                break;
            case "UPDATE_USER":
                boolean ok = new ProfileController().updateUser(request.getId(), request.getUsername(), request.getEmail());
                sendResponse(ok);
                break;
            case "CHECK_USERNAME_OR_EMAIL":
                boolean exists = new ProfileController().checkUsernameOrEmailExists(
                        request.getUsername(), request.getEmail(), request.getId());
                sendResponse(exists);
                break;
            default:
                sendResponse("UNKNOWN_COMMAND");
        }
    }

    private void handleBoThe(boThe req) throws IOException {
        boTheController dao = new boTheController();
        try {
            if ("GET_DECK".equals(req.getCommand())) {
                List<boThe> list = dao.getDecksByUser(req.getUserId());
                sendResponse(list);
            } else if ("ADD_DECK".equals(req.getCommand())) {
                int newId = dao.insertDeck(req);
                req.setDeckId(newId);
                sendResponse(req);
            } else if ("UPDATE_DECK".equals(req.getCommand())) {
                boolean upOK = dao.updateDeckName(req.getDeckId(), req.getName());
                sendResponse(upOK ? "SUCCESS" : "FAIL");
            } else if ("DELETE_DECK".equals(req.getCommand())) {
                boolean delOK = dao.deleteDeck(req.getDeckId());
                sendResponse(delOK ? "SUCCESS" : "FAIL");
            } else if ("COUNT_CARD".equals(req.getCommand())) {
                int total = new CardController().countCardsInDeck(req.getDeckId());
                sendResponse(total);
            } else {
                sendResponse("UNKNOWN_COMMAND");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            sendResponse(null);
        }
    }

    private void handleCard(Card card) throws IOException {
        CardController dao = new CardController();
        try {
            if ("GET_CARD".equals(card.getCommand())) {
                List<Card> list = dao.getCards(card.getBoThe_id());
                sendResponse(list);
            } else if ("ADD_CARD".equals(card.getCommand())) {
                int newId = dao.insertCard(card);
                card.setId(newId);
                sendResponse(card);
            } else if ("UPDATE_CARD".equals(card.getCommand())) {
                boolean ok = dao.updateCard(card.getId(), card.getEnglish_text(), card.getVietnamese_text());
                sendResponse(ok ? "SUCCESS" : "FAIL");
            } else if ("DELETE_CARD".equals(card.getCommand())) {
                boolean delOK = dao.deleteCard(card.getId());
                sendResponse(delOK ? "SUCCESS" : "FAIL");
            } else if ("UPDATE_CARD_AFTER_REVIEW".equals(card.getCommand())) {
                dao.updateCardAfterReview(card);
                sendResponse("SUCCESS");
            } else if ("BULK_UPDATE_CARDS".equals(card.getCommand())) {
                List<Card> cards = card.getCardList();
                if (cards != null && !cards.isEmpty()) {
                    for (Card c : cards) {
                        dao.updateCardAfterReview(c);
                    }
                    sendResponse("SUCCESS");
                    System.out.println("Bulk updated " + cards.size() + " cards");
                } else {
                    sendResponse("ERROR: Empty or null card list");
                    System.out.println("Received empty or null card list for BULK_UPDATE_CARDS");
                }
            } else {
                sendResponse("UNKNOWN_COMMAND");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse("ERROR: " + e.getMessage());
        }
    }

    private void handleStat(Stat req) throws IOException {
        statController dao = new statController();
        try {
            if ("GET_STATS".equals(req.getCommand())) {
                List<Stat> stats = dao.getStatsByUser(req.getUserId());
                sendResponse(stats);
            } else if ("GET_STAT_BY_DATE".equals(req.getCommand())) {
                Stat stat = dao.getStatByDate(req.getUserId(), req.getDate());
                sendResponse(stat);
            } else if ("SYNC_STATS".equals(req.getCommand())) {
                dao.syncStatsFromCards();
                sendResponse("SYNC_SUCCESS");
            } else {
                sendResponse("UNKNOWN_COMMAND");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            sendResponse(null);
        }
    }

    private void sendResponse(Object response) throws IOException {
        synchronized (out) {
            out.writeObject(response);
            out.flush();
        }
    }

    private void cleanup() {
        if (currentUserId != null) {
            clientOutputs.remove(currentUserId);
            try {
                broadcastMessage(new Message("BROADCAST_MESSAGE", 0, "System",
                        "Một người dùng đã rời phòng chat."));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
