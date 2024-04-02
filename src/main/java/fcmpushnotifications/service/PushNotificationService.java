package fcmpushnotifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import fcmpushnotifications.firebase.FCMService;
import fcmpushnotifications.model.PushNotificationRequest;
import fcmpushnotifications.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PushNotificationService {

    @Value("#{${app.notifications.defaults}}")
    private Map<String, String> defaults;

    private Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private FCMService fcmService;
    private Firestore firestore;

    public PushNotificationService(FCMService fcmService, Firestore firestore) {
        this.fcmService = fcmService;
        this.firestore = firestore;
    }

    @Scheduled(fixedDelay = 60000) // Запуск каждые 1 минут
    public void checkUpcomingTasks() throws Exception {
        List<Task> upcomingTasks = getUpcomingTasks();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (Task task : upcomingTasks) {
            String fcmToken = getFcmTokenForUser(task.getAuthorId());

            if (fcmToken != null) {
                Map<String, Object> data = firestore.collection("Tasks").document(task.getId()).get().get().getData();
                data.put("notificationSended", true);
                firestore.collection("Tasks").document(task.getId()).set(data);
                PushNotificationRequest req = new PushNotificationRequest(task.getTitle(),
                        "У вас запланирована задача на " + sdf.format(task.getStartTime().toDate()),
                        "topic");
                req.setToken(fcmToken);
                sendPushNotificationToToken(req);
            }
        }
    }

    private String getFcmTokenForUser(String authorId) throws InterruptedException, ExecutionException {
        DocumentSnapshot doc = firestore.collection("Users").document(authorId).get().get();
        if (doc.getData().containsKey("fcmToken")) {
            return (String) doc.getData().get("fcmToken");
        }
        return null;
    }

    private List<String> getAllUserIds() throws Exception {
        List<String> userIds = new ArrayList<>();
        for (QueryDocumentSnapshot document : firestore.collection("Users").get().get().getDocuments()) {
            userIds.add(document.getString("id"));
        }
        return userIds;
    }

    private List<Map<String, Object>> getAllTasksForUser(String userId) throws Exception {
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (QueryDocumentSnapshot document : firestore.collection("Tasks")
                .whereEqualTo("authorId", userId)
                .get()
                .get()
                .getDocuments()) {
            tasks.add(document.getData());
        }
        return tasks;
    }

    private List<Task> getUpcomingTasks() throws Exception {
        List<Task> upcomingTasks = new ArrayList<>();
        List<String> userIds = getAllUserIds();
        final ObjectMapper mapper = new ObjectMapper();
        for (String userId : userIds) {
            List<Task> usersTask = getAllTasksForUser(userId).stream().map(e -> mapper.convertValue(e, Task.class))
                    .collect(Collectors.toList());
            List<Task> needNotificationTasks = usersTask.stream().filter(task -> {
                long currentTimeMillis = Timestamp.now().getSeconds();
                long startTimeMillis = task.getStartTime().getSeconds();
                long differenceInMillis = startTimeMillis - currentTimeMillis;
                return differenceInMillis <= task.getRemindTimeInSeconds() && !task.isNotificationSended();
            })
                    .collect(Collectors.toList());
            upcomingTasks.addAll(needNotificationTasks);
        }
        return upcomingTasks;
    }

    public void sendPushNotificationToToken(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToToken(request);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }
}
