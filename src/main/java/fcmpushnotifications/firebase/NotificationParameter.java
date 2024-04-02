package fcmpushnotifications.firebase;

public enum NotificationParameter {
    SOUND("default"),
    COLOR("#162A48");

    private String value;

    NotificationParameter(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
