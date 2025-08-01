package com.feather.api.security.tokens;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationTokenTest {

    @Test
    void testEqualsAndHashCode_SameApiKey() {
        final ApiKeyAuthenticationToken token1 = new ApiKeyAuthenticationToken("key123");
        final ApiKeyAuthenticationToken token2 = new ApiKeyAuthenticationToken("key123");
        assertThat(token1).isEqualTo(token2).hasSameHashCodeAs(token2);
    }

    @Test
    void testEqualsAndHashCode_DifferentApiKey() {
        final ApiKeyAuthenticationToken token1 = new ApiKeyAuthenticationToken("key123");
        final ApiKeyAuthenticationToken token2 = new ApiKeyAuthenticationToken("key456");
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1.hashCode()).isNotEqualTo(token2.hashCode());
    }

    @Test
    void testEqualsAndHashCode_WithAuthorities() {
        final ApiKeyAuthenticationToken token1 = new ApiKeyAuthenticationToken("key123", java.util.Collections.emptyList());
        final ApiKeyAuthenticationToken token2 = new ApiKeyAuthenticationToken("key123", java.util.Collections.emptyList());
        assertThat(token1).isEqualTo(token2).hasSameHashCodeAs(token2);
    }
}