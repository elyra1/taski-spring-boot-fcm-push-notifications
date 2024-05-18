package fcmpushnotifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import fcmpushnotifications.firebase.FCMService;
import fcmpushnotifications.model.PushNotificationRequest;
import fcmpushnotifications.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private List<Task> upcomingTasks = new ArrayList<>();
    private List<String> userIds = new ArrayList<String>();

    public PushNotificationService(FCMService fcmService, Firestore firestore) throws Exception {
        this.fcmService = fcmService;
        this.firestore = firestore;
        firestore.collection("Tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot arg0, FirestoreException arg1) {
                System.out.print("TASKS: " + upcomingTasks.size() + ", ");
                upcomingTasks = getTasksFromQuerySnapshot(arg0);
                System.out.println(" CHANGED: " + upcomingTasks.size());
            }
        });
    }

    public List<Task> getTasksFromQuerySnapshot(QuerySnapshot arg) {
        final ObjectMapper mapper = new ObjectMapper();
        return arg.getDocuments().stream().map(e -> mapper.convertValue(e.getData(), Task.class))
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void addRepeatTask() throws Exception {

        List<Task> repeatedTasks = new ArrayList<>();
        final ObjectMapper mapper = new ObjectMapper();
        for (String userId : userIds) {
            repeatedTasks.addAll(getAllTasksForUser(userId).stream().map(e -> mapper.convertValue(e, Task.class))
                    .filter(task -> task.getRepeatString() != "never")
                    .collect(Collectors.toList()));
        }
        List<Task> dayRepeat = repeatedTasks.stream().filter(task -> task.getRepeatString().equals("day"))
                .collect(Collectors.toList());
        List<Task> weekRepeat = repeatedTasks.stream().filter(task -> task.getRepeatString().equals("week"))
                .collect(Collectors.toList());
        List<Task> monthRepeat = repeatedTasks.stream().filter(task -> task.getRepeatString().equals("month"))
                .collect(Collectors.toList());
        Calendar cal = Calendar.getInstance();
        Calendar nowCal = Calendar.getInstance();
        int nowDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR);

        for (Task task : dayRepeat) {
            cal.setTime(task.getStartTime().toDate());
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            if (nowDayOfYear - dayOfYear >= 1) {
                firestore.collection("Tasks").document(task.getId()).set(task.withShiftedDatesByOneDay());
            }
        }
        for (Task task : weekRepeat) {
            cal.setTime(task.getStartTime().toDate());
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            if (nowDayOfYear - dayOfYear >= 1) {
                firestore.collection("Tasks").document(task.getId()).set(task.withShiftedDatesByOneWeek());
            }
        }
        for (Task task : monthRepeat) {
            cal.setTime(task.getStartTime().toDate());
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            if (nowDayOfYear - dayOfYear >= 1) {
                firestore.collection("Tasks").document(task.getId()).set(task.withShiftedDatesByOneMonth());
            }
        }
    }

    @Scheduled(fixedDelay = 60000) // Запуск каждые 1 минут
    public void checkIsReadyForNotification() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        List<Task> needNotification = upcomingTasks.stream().filter(task -> {
            long currentTimeMillis = Timestamp.now().getSeconds();
            long startTimeMillis = task.getStartTime().getSeconds();
            long differenceInMillis = startTimeMillis - currentTimeMillis;
            return differenceInMillis <= task.getRemindTimeInSeconds() && !task.isNotificationSended();
        }).collect(Collectors.toList());
        for (Task task : needNotification) {
            String fcmToken = getFcmTokenForUser(task.getAuthorId());
            List<String> contributorsTokens = new ArrayList<>();
            if (task.getContributors().size() != 0) {
                for (String contributorId : task.getContributors()) {
                    contributorsTokens.add(getFcmTokenForUser(contributorId));
                }
            }

            if (fcmToken != null) {
                Map<String, Object> data = firestore.collection("Tasks").document(task.getId()).get().get().getData();
                data.put("notificationSended", true);
                firestore.collection("Tasks").document(task.getId()).set(data);
                PushNotificationRequest req = new PushNotificationRequest(task.getTitle(),
                        "У вас запланирована задача на " + sdf.format(task.getStartTime().toDate()),
                        "topic");
                req.setToken(fcmToken);
                sendPushNotificationToToken(req);
                for (String contributorToken : contributorsTokens) {
                    req.setToken(contributorToken);
                    sendPushNotificationToToken(req);
                }
                upcomingTasks.remove(task);
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

    public void sendPushNotificationToToken(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToToken(request);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }
}
