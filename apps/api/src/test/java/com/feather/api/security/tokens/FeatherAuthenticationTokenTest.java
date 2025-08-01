package com.feather.api.security.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;

import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class FeatherAuthenticationTokenTest {

    @Test
    void testEqualsAndHashCode_SameValues() {
        final UserDetails principal = Mockito.mock(UserDetails.class);
        final FeatherCredentials credentials = Mockito.mock(FeatherCredentials.class);
        final GrantedAuthority authority = Mockito.mock(GrantedAuthority.class);
        final FeatherAuthenticationToken token1 = new FeatherAuthenticationToken(principal, credentials, Collections.singletonList(authority));
        final FeatherAuthenticationToken token2 = new FeatherAuthenticationToken(principal, credentials, Collections.singletonList(authority));
        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void testEquals_DifferentTypeOrNull() {
        final UserDetails principal = Mockito.mock(UserDetails.class);
        final FeatherCredentials credentials = Mockito.mock(FeatherCredentials.class);
        final GrantedAuthority authority = Mockito.mock(GrantedAuthority.class);
        final FeatherAuthenticationToken token = new FeatherAuthenticationToken(principal, credentials, Collections.singletonList(authority));
        assertNotEquals(null, token);
        assertNotEquals("string", token);
    }

}

