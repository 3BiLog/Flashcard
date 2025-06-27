package FlashCard;

import OOP.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProfileRequest {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1234;

    private static Object send(Object obj) throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(30000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(obj);
            out.flush();
            return in.readObject();
        }
    }

    public User getUserById(int userId) {
        try {
            User req = new User("GET_USER", userId, null, null, null);
            Object resp = send(req);
            if (resp instanceof User) {
                return (User) resp;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi yêu cầu GET_USER_BY_ID", e);
        }
        return null;
    }

    public boolean updateUser(int id, String ten, String email) {
        try {
            User u = new User("UPDATE_USER", id, ten, email, null);
            Object resp = send(u);
            if (resp instanceof Boolean) {
                return (Boolean) resp;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi yêu cầu UPDATE_USER", e);
        }
        return false;
    }

    public boolean checkUsernameOrEmailExists(String username, String email, int excludeUserId) {
        try {
            User req = new User("CHECK_USERNAME_OR_EMAIL", excludeUserId, username, email, null);
            Object resp = send(req);
            if (resp instanceof Boolean) {
                return (Boolean) resp;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra username hoặc email", e);
        }
        return false;
    }
}

//package FlashCard;
//
//import OOP.User;
//
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//
//public class ProfileRequest {
//    private static final String SERVER_HOST = "localhost";
//    private static final int SERVER_PORT = 1234;
//
//    private static Object send(Object obj) throws Exception {
//        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
//
//            out.writeObject(obj);
//            out.flush();
//            return in.readObject();
//        }
//    }
//
//    public User getUserById(int userId) {
//        try {
//            User req = new User("GET_USER", userId, null, null, null);
//            Object resp = send(req);
//            if (resp instanceof User) {
//                return (User) resp;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi gửi yêu cầu GET_USER_BY_ID", e);
//        }
//        return null;
//    }
//
//    // Gửi yêu cầu cập nhật user
//    public boolean updateUser(int id, String ten, String email) {
//        try {
//            User u = new User("UPDATE_USER", id, ten, email, null);
//            Object resp = send(u);
//            if (resp instanceof Boolean) {
//                return (Boolean) resp;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi gửi yêu cầu UPDATE_USER", e);
//        }
//        return false;
//    }
//
//    public boolean checkUsernameOrEmailExists(String username, String email, int excludeUserId) {
//        try {
//            User req = new User("CHECK_USERNAME_OR_EMAIL", excludeUserId, username, email, null);
//            Object resp = send(req);
//            if (resp instanceof Boolean) {
//                return (Boolean) resp;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi kiểm tra username hoặc email", e);
//        }
//        return false;
//    }
//}
