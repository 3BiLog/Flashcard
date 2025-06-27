package Mail;

import database.CardController;
import OOP.Card;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(4);

    private static final ConcurrentMap<LocalDateTime, List<Card>> pendingCards =
            new ConcurrentHashMap<>();

    private static final ConcurrentMap<LocalDateTime, ScheduledFuture<?>> pendingTasks =
            new ConcurrentHashMap<>();

    private static final EmailService EMAIL_SERVICE = new EmailService();

    private static LocalDateTime roundToNearestMinute(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    public static void scheduleEmailGrouped(int userId, LocalDateTime nextReview, Card card) {
        LocalDateTime roundedNextReview = roundToNearestMinute(nextReview);

        List<Card> cardList = pendingCards.computeIfAbsent(roundedNextReview, dt -> new ArrayList<>());
        synchronized (cardList) {
            cardList.add(card);
        }

        pendingTasks.computeIfAbsent(roundedNextReview, dt -> {
            long delayMs = Duration.between(LocalDateTime.now(), dt).toMillis();
            if (delayMs < 0) delayMs = 0;
            return SCHEDULER.schedule(() -> {
                List<Card> cards = pendingCards.remove(dt);
                pendingTasks.remove(dt);

                try {
                    String email = CardController.fetchUserEmail(userId);
                    if (email == null || email.isBlank()) {
                        System.err.println("[SchedulerService] No email for userId=" + userId);
                        return;
                    }
                    StringBuilder content = new StringBuilder();
                    content.append("Bạn có ").append(cards.size()).append(" từ đã đến lúc ôn tập:\n\n");
                    for (Card c : cards) {
                        content.append("- ").append(c.getEnglish_text())
                                .append(": ").append(c.getVietnamese_text()).append("\n");
                    }
                    EMAIL_SERVICE.sendCustomEmail(email.trim(),
                            "🔔 Nhắc nhở ôn tập từ vựng",
                            content.toString());
                    CardController.markNotifiedSingleUser(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        });
    }
}