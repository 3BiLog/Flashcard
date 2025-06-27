package FlashCard;

import OOP.Card;
import OOP.boThe;
import statistical.StatsRequest;
import statistical.statController;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class botheRequest  {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT    = 1234;
    /**
     * Gửi object bất kỳ lên server và nhận về Object.
     */
    private static Object send(Object obj) throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(obj);
            out.flush();
            return in.readObject();
        }
    }
    /**
     * Lấy danh sách bộ thẻ của user.
     */
    @SuppressWarnings("unchecked")
    public static List<boThe> getDecks(int userId) {
        try {
            boThe req = new boThe("GET_DECK", 0, userId, null,0);
            Object resp = send(req);
            if (resp instanceof List) {
                return (List<boThe>) resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Thêm mới một bộ thẻ và trả về đối tượng với deckId sinh mới.
     */
    public static boThe addDeck(int userId, String name, int cardCount) {
        try {
            boThe req = new boThe("ADD_DECK", 0, userId, name,cardCount);
            Object resp = send(req);
            if (resp instanceof boThe) {
                return (boThe) resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateDeck(int deckId, String newName){
        try {
            boThe req = new boThe("UPDATE_DECK",deckId,0,newName,0);
            Object resp = send(req);
            return (resp instanceof String) && resp.equals("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDeck(int deckId, statController statCtrl) {
        try {
            boThe req = new boThe("DELETE_DECK", deckId, 0, null, 0);
            Object resp = send(req);
            boolean success = (resp instanceof String) && resp.equals("SUCCESS");
            if (success) {
                // Gọi đồng bộ hóa thống kê
                StatsRequest statsRequest = new StatsRequest();
                boolean syncSuccess = statsRequest.syncStats();
                System.out.println("Sync stats after deleting deck " + deckId + ": " + (syncSuccess ? "Success" : "Failed"));
                // Làm mới giao diện nếu statCtrl không null
                if (statCtrl != null) {
                    statCtrl.refreshStats();
                }
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int countCard(int boThe_id){
        try {
            boThe req = new boThe("COUNT_CARD",boThe_id,0,null,0);
            Object resp = send(req);
            if(resp instanceof Integer){
                return (Integer) resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }





}