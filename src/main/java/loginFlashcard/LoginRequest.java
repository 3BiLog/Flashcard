package loginFlashcard;

import OOP.User;

import java.io.*;
import java.net.Socket;

public class LoginRequest {

    public static User sendRequest(String command, String uandE, String password) {
        try (Socket socket = new Socket("localhost", 1234)) {
            socket.setSoTimeout(30000); // Đặt timeout 30 giây
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            User req = new User(command, uandE, uandE, password);
            out.writeObject(req);
            out.flush();

            Object resp = in.readObject();
            if (resp instanceof User) {
                return (User) resp;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean sendRegisterRequest(String command, String username, String email, String password) {
        try (Socket socket = new Socket("localhost", 1234)) {
            socket.setSoTimeout(30000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            User req = new User(command, username, email, password);
            out.writeObject(req);
            out.flush();

            Object resp = in.readObject();
            if (resp instanceof String) {
                return "SUCCESS".equals(resp);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkEmailExists(String email) {
        try (Socket socket = new Socket("localhost", 1234)) {
            socket.setSoTimeout(30000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            User req = new User("CHECK_EMAIL", null, email, null);
            out.writeObject(req);
            out.flush();

            Object resp = in.readObject();
            if (resp instanceof Boolean) {
                return (Boolean) resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean resetPassword(String email, String hashedPassword) {
        try (Socket socket = new Socket("localhost", 1234)) {
            socket.setSoTimeout(30000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            User req = new User("RESET_PASSWORD", null, email, hashedPassword);
            out.writeObject(req);
            out.flush();

            Object resp = in.readObject();
            if (resp instanceof Boolean) {
                return (Boolean) resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

//package loginFlashcard;
//
//import OOP.User;
//
//import java.io.*;
//import java.net.Socket;
//
//public class LoginRequest {
//
//    public static User sendRequest(String command,String uandE, String password) {
//        try (Socket socket = new Socket("localhost", 1234);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())) {
//
//            User req = new User(command,uandE, uandE, password);
//            out.writeObject(req);
//            out.flush();
//
//            Object resp = in.readObject();
//            //return resp instanceof String && ((String)resp).equals("SUCCESS");
//            if (resp instanceof User) {
//                return (User) resp;
//            }
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static boolean sendRegisterRequest(String command, String username, String email, String password) {
//        try (Socket socket = new Socket("localhost", 1234);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())) {
//
//            User req = new User(command, username, email, password);
//            out.writeObject(req);
//            out.flush();
//
//            Object resp = in.readObject();
//            if (resp instanceof String) {
//                return ((String) resp).equals("SUCCESS");
//            }
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean checkEmailExists(String email) {
//        try (Socket socket = new Socket("localhost", 1234);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())) {
//
//            User req = new User("CHECK_EMAIL", null, email, null);
//            out.writeObject(req);
//            out.flush();
//
//            Object resp = in.readObject();
//            if (resp instanceof Boolean) {
//                return (Boolean) resp;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean resetPassword(String email, String hashedPassword) {
//        try (Socket socket = new Socket("localhost", 1234);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())) {
//
//            User req = new User("RESET_PASSWORD", null, email, hashedPassword);
//            out.writeObject(req);
//            out.flush();
//
//            Object resp = in.readObject();
//            if (resp instanceof Boolean) {
//                return (Boolean) resp;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//}
