package FlashCard;

import OOP.Card;
import statistical.StatsRequest;
import statistical.statController;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CardRequest {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT    = 1234;

    private static Object send(Object obj) throws Exception{
        try (Socket socket = new Socket(SERVER_HOST,SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){
            out.writeObject(obj);
            out.flush();
            return in.readObject();
        }
    }

    public static List<Card> getCard(int boThe_id){
        try {
            Card req = new Card("GET_CARD",0,boThe_id,null,null);
            Object resp = send(req);
            if (resp instanceof List){
                return (List<Card>) resp;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    public static Card addCard(int boThe_id, String english_text,String vietnamese_text){
        try {
            Card req = new Card("ADD_CARD",0,boThe_id,english_text,vietnamese_text);
            Object resp = send(req);
            if (resp instanceof Card){
                return (Card)resp;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean updateCard(int id,String english_text,String vietnamese_text){
        try {
            Card req = new Card("UPDATE_CARD",id,0,english_text,vietnamese_text);
            Object resp = send(req);
            return (resp instanceof String) && resp.equals("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCard(int id, statController statCtrl) {
        try {
            Card req = new Card("DELETE_CARD", id, 0, null, null);
            Object resp = send(req);
            boolean success = (resp instanceof String) && resp.equals("SUCCESS");
            if (success) {
                // Gọi đồng bộ hóa thống kê
                StatsRequest statsRequest = new StatsRequest();
                boolean syncSuccess = statsRequest.syncStats();
                System.out.println("Sync stats after deleting card " + id + ": " + (syncSuccess ? "Success" : "Failed"));
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

//    public static boolean updateCardAfterReview(Card card) {
//        try {
//            Card req = new Card("UPDATE_CARD_AFTER_REVIEW", card.getId(), card.getBoThe_id(), card.getEnglish_text(), card.getVietnamese_text());
//            req.setRepetitionCount(card.getRepetitionCount());
//            req.setIntervalDays(card.getIntervalDays());
//            req.setEaseFactor(card.getEaseFactor());
//            req.setLastReviewDate(card.getLastReviewDate());
//            req.setNextReviewDate(card.getNextReviewDate());
//            req.setMemoryLevel(card.getMemoryLevel());
//            Object resp = send(req);
//            return (resp instanceof String) && resp.equals("SUCCESS");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public static void updateCardAfterReview(Card card) {
        try {
            System.out.println("Sending update for card: ID=" + card.getId() +
                    ", MemoryLevel=" + card.getMemoryLevel() +
                    ", NextReview=" + card.getNextReviewDate());

            Object resp = send(card);
            System.out.println("Server response for card ID=" + card.getId() + ": " + (resp != null ? resp.toString() : "null"));

            if (resp == null) {
                throw new RuntimeException("Server returned null response for card ID=" + card.getId());
            }
            if (!(resp instanceof String)) {
                throw new RuntimeException("Unexpected response type for card ID=" + card.getId() + ": " + resp.getClass().getName());
            }
            if (!resp.equals("SUCCESS")) {
                throw new RuntimeException("Failed to update card after review: ID=" + card.getId() + ", response: " + resp);
            }
        } catch (Exception e) {
            System.err.println("Error updating card after review: ID=" + card.getId() + ", Error=" + e.getMessage());
            throw new RuntimeException("Error updating card after review: ID=" + card.getId() + ", " + e.getMessage(), e);
        }
    }

    public static boolean bulkUpdateCards(List<Card> cards) {
        try {
            Card req = new Card("BULK_UPDATE_CARDS", 0, 0, null, null);
            req.setCardList(cards);
            Object resp = send(req);
            return (resp instanceof String) && resp.equals("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
