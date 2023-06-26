package springbeam.susukgwan.fcm;

import lombok.AllArgsConstructor;
import lombok.Getter;


public enum NotificationParameter {
    SOUND("default"),
    COLOR("#B9E0FD");

    private String value;
    NotificationParameter(String value) {this.value = value;}
    public String getValue() {return this.value;}
}
