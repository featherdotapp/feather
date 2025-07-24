package com.feather.api.security.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import com.feather.api.security.exception_handling.exception.ApiKeyAuthenticationException;
import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
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
            final Field field = ApiKeyAuthenticationProvider.class.getDeclaredField("validApiKey");
            field.setAccessible(true);
            field.set(classUnderTest, validApiKey);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAuthenticate_validApiKey_returnsAuthenticatedToken() {
        final ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(validApiKey);
        final Authentication result = classUnderTest.authenticate(token);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo("apiKey");
    }

    @Test
    void testAuthenticate_invalidApiKey_throwsBadCredentialsException() {
        final ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("wrong-key");
        assertThrows(ApiKeyAuthenticationException.class, () -> classUnderTest.authenticate(token));
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
