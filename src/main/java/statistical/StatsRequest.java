package statistical;

import OOP.Stat;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class StatsRequest {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public List<Stat> getStats(int userId) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Stat request = new Stat("GET_STATS", null, 0, userId);
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            System.out.println("GetStats response for userId " + userId + ": " + response);
            if (response instanceof List) {
                return (List<Stat>) response;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in getStats: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public Stat getStatByDate(int userId, LocalDate date) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Stat request = new Stat("GET_STAT_BY_DATE", date, 0, userId);
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            System.out.println("GetStatByDate response for userId " + userId + ", date " + date + ": " + response);
            if (response instanceof Stat) {
                return (Stat) response;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in getStatByDate: " + e.getMessage());
            e.printStackTrace();
        }
        return new Stat("GET_STAT_BY_DATE", date, 0, userId);
    }

    public boolean syncStats() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Stat request = new Stat("SYNC_STATS", null, 0, 0);
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            System.out.println("SyncStats response: " + response);
            if (response instanceof String) {
                return "SYNC_SUCCESS".equals(response);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in syncStats: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}