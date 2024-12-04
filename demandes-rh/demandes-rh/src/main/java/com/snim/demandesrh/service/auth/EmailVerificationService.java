package com.snim.demandesrh.service.auth;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
@Service
public class EmailVerificationService {
    @Value("${email.verification.api.key}")
    private String apiKey;

    public boolean verifyEmail(String email) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.hunter.io/v2/email-verifier?email=" + email + "&api_key=" + apiKey;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(response.getBody());
                String result = json.getJSONObject("data").getString("status");
                return "valid".equals(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
