package com.groo.service.oauth;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenVerifier {

    public FirebaseProfile verify(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return new FirebaseProfile(token.getUid(), token.getEmail(), token.getName());
        } catch (IllegalStateException | FirebaseAuthException ex) {
            throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    public record FirebaseProfile(String uid, String email, String name) {
    }
}
