package fcmpushnotifications.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;

import fcmpushnotifications.model.TokenRequest;
import fcmpushnotifications.service.PushNotificationService;

@RestController
public class PushNotificationController {
    private Firestore firestore;

    public PushNotificationController(PushNotificationService pushNotificationService, Firestore firestore) {
        this.firestore = firestore;
    }

    @PostMapping("/register-token")
    public ResponseEntity<String> saveToken(@RequestBody TokenRequest tokenRequest) {
        String userId = tokenRequest.getUserId();
        String token = tokenRequest.getToken();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);

        firestore.collection("Users")
                .document(userId)
                .set(tokenData, SetOptions.merge());

        return ResponseEntity.ok("Token saved successfully");
    }
}
