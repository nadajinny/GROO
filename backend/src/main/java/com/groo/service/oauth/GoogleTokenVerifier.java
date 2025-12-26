package com.groo.service.oauth;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleTokenVerifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientId;

    public GoogleTokenVerifier(@Value("${app.oauth2.google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleProfile verify(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || body.containsKey("error")) {
                throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
            }
            String audience = (String) body.get("aud");
            if (StringUtils.hasText(clientId) && !clientId.equals(audience)) {
                throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
            }
            String email = (String) body.get("email");
            String name = (String) body.getOrDefault("name", email);
            String sub = (String) body.get("sub");
            if (!StringUtils.hasText(email)) {
                throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
            }
            return new GoogleProfile(email, name, sub);
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILURE);
        }
    }

    public record GoogleProfile(String email, String name, String id) {
    }
}
