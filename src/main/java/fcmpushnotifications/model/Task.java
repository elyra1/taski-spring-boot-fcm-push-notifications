package fcmpushnotifications.model;

import com.google.cloud.Timestamp;

import fcmpushnotifications.firebase.TimestampDeserializer;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private Long color;
    private String repeatString;
    private List<String> contributors;

    public Task(String authorId, String id, String title, String description, String category, Timestamp startTime,
            Timestamp endTime, boolean notificationSended, int remindTimeInSeconds, Long color, String repeatString,
            List<String> contributors) {
        this.authorId = authorId;
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notificationSended = notificationSended;
        this.remindTimeInSeconds = remindTimeInSeconds;
        this.color = color;
        this.repeatString = repeatString;
        this.contributors = contributors;
    }

    public Task() {
    }

    public Map<String, Object> withShiftedDatesByOneDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startTime.toDate());
        cal.add(Calendar.DATE, 1);
        Timestamp newStartTime = Timestamp.of(cal.getTime());

        cal.setTime(this.endTime.toDate());
        cal.add(Calendar.DATE, 1);
        Timestamp newEndTime = Timestamp.of(cal.getTime());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper
                .convertValue(this, new TypeReference<Map<String, Object>>() {
                });
        data.put("startTime", newStartTime);
        data.put("endTime", newEndTime);
        data.put("color", color);
        data.put("notificationSended", false);
        return data;
    }

    public Map<String, Object> withShiftedDatesByOneWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startTime.toDate());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        Timestamp newStartTime = Timestamp.of(cal.getTime());

        cal.setTime(this.endTime.toDate());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        Timestamp newEndTime = Timestamp.of(cal.getTime());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper
                .convertValue(this, new TypeReference<Map<String, Object>>() {
                });
        data.put("startTime", newStartTime);
        data.put("endTime", newEndTime);
        data.put("color", color);
        data.put("notificationSended", false);
        return data;
    }

    public Map<String, Object> withShiftedDatesByOneMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startTime.toDate());
        cal.add(Calendar.MONTH, 1);
        Timestamp newStartTime = Timestamp.of(cal.getTime());

        cal.setTime(this.endTime.toDate());
        cal.add(Calendar.MONTH, 1);
        Timestamp newEndTime = Timestamp.of(cal.getTime());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper
                .convertValue(this, new TypeReference<Map<String, Object>>() {
                });
        data.put("startTime", newStartTime);
        data.put("endTime", newEndTime);
        data.put("color", color);
        data.put("notificationSended", false);
        return data;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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

    public Long getColor() {
        return color;
    }

    public void setColor(Long color) {
        this.color = color;
    }

    public String getRepeatString() {
        return repeatString;
    }

    public void setRepeatString(String repeatString) {
        this.repeatString = repeatString;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }
}
