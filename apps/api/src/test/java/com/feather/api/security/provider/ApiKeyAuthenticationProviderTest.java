package com.feather.api.security.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationProviderTest {

    @InjectMocks
    private ApiKeyAuthenticationProvider classUnderTest;

    private String validApiKey;

    @BeforeEach
    void setUp() {
        validApiKey = "test-key";
        // Set the environment value
        try {
            java.lang.reflect.Field field = ApiKeyAuthenticationProvider.class.getDeclaredField("validApiKey");
            field.setAccessible(true);
            field.set(classUnderTest, validApiKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAuthenticate_validApiKey_returnsAuthenticatedToken() {
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(validApiKey);
        Authentication result = classUnderTest.authenticate(token);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo("apiKey");
    }

    @Test
    void testAuthenticate_invalidApiKey_throwsBadCredentialsException() {
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("wrong-key");
        assertThrows(BadCredentialsException.class, () -> classUnderTest.authenticate(token));
    }

    @Test
    void testSupports_returnsTrueForApiKeyAuthenticationToken() {
        assertThat(classUnderTest.supports(ApiKeyAuthenticationToken.class)).isTrue();
    }

    @Test
    void testSupports_returnsFalseForOtherToken() {
        assertThat(classUnderTest.supports(String.class)).isFalse();
    }
}
