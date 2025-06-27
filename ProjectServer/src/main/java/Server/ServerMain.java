
package Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) {
        int port = 1234;

        // Khởi chạy Email Scheduler ngay khi server start
//        ReviewEmailScheduler emailScheduler = new ReviewEmailScheduler();
//        emailScheduler.start();
//        System.out.println("Email Scheduler đã được khởi động.");
//
//        // Đăng ký shutdown hook để dừng scheduler khi server tắt
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("Shutdown hook: Dừng Email Scheduler...");
//            emailScheduler.stop();
//        }));

        // Khởi tạo ServerSocket và lắng nghe kết nối
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đang chạy tại cổng " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối từ: " + clientSocket.getInetAddress());
                // Xử lý client trên luồng mới
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}