package fcmpushnotifications.model;

import com.google.cloud.Timestamp;

import fcmpushnotifications.firebase.TimestampDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private String authorId;
    private String id;
    private String title;
    private String description;
    private String category;
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp startTime;
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp endTime;
    private boolean notificationSended;
    private int remindTimeInSeconds;

    public Task(String authorId, String id, String title, String description, String category,
            Timestamp startTime, Timestamp endTime, boolean notificationSended, int remindTimeInSeconds) {
        this.authorId = authorId;
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notificationSended = notificationSended;
        this.remindTimeInSeconds = remindTimeInSeconds;
    }

    public Task() {
        super();
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public boolean isNotificationSended() {
        return notificationSended;
    }

    public void setNotificationSended(boolean notificationSended) {
        this.notificationSended = notificationSended;
    }

    public int getRemindTimeInSeconds() {
        return remindTimeInSeconds;
    }

    public void setRemindTimeInSeconds(int remindTimeInSeconds) {
        this.remindTimeInSeconds = remindTimeInSeconds;
    }
}
