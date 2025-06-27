package OOP;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private int userId;
    private String username;
    private String content;
    private byte[] fileData;
    private String fileName;
    private String fileType;
    private LocalDateTime timestamp;
    private String messageId;


    // Constructor cho tin nhắn văn bản/icon
    public Message(String command, int userId, String username, String content) {
        this.command = command;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.messageId = generateMessageId();
    }

    // Constructor cho tin nhắn file
    public Message(String command, int userId, String username, byte[] fileData, String fileName, String fileType) {
        this.command = command;
        this.userId = userId;
        this.username = username;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
        this.timestamp = LocalDateTime.now();
        this.messageId = generateMessageId();
    }

    // Constructor đầy đủ (không có messageId)
    public Message(String command, int userId, String username, String content, byte[] fileData,
                   String fileName, String fileType, LocalDateTime timestamp) {
        this.command = command;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
        this.timestamp = timestamp;
        this.messageId = generateMessageId();
    }

    // Constructor với messageId
    public Message(String command, int userId, String username, String content, byte[] fileData,
                   String fileName, String fileType, LocalDateTime timestamp, String messageId) {
        this.command = command;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
        this.timestamp = timestamp;
        this.messageId = messageId != null ? messageId : generateMessageId();
    }

    // Constructor cho DELETE_MESSAGE
    public Message(String command, String messageId, int userId, String username) {
        this.command = command;
        this.messageId = messageId;
        this.userId = userId;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor cũ cho DELETE_MESSAGE
    public Message(String command, String messageId, int userId) {
        this.command = command;
        this.messageId = messageId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Tạo messageId duy nhất
    private String generateMessageId() {
        return java.util.UUID.randomUUID().toString();
    }

    // Getters and Setters
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}