package com.feather.api.security.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import com.feather.api.jpa.model.User;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthenticationTokenFactoryTest {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final String CURRENT_CREDENTIALS = "apiKey";
    @InjectMocks
    private AuthenticationTokenFactory classUnderTest;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication currentAuthentication;
    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
        when(currentAuthentication.getCredentials()).thenReturn(CURRENT_CREDENTIALS);
    }

    @Test
    void buildAuthenticationToken_ShouldCreateTokenWithCombinedAuthorities() {
        // Arrange
        final SimpleGrantedAuthority existingAuthority = new SimpleGrantedAuthority("ROLE_USER");
        doReturn(Collections.singleton(existingAuthority)).when(currentAuthentication).getAuthorities();

        // Act
        final FeatherAuthenticationToken result = classUnderTest.buildAuthenticationToken(
                mockUser
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(mockUser);

        final FeatherCredentials credentials = (FeatherCredentials) result.getCredentials();
        assertThat(credentials.apiKey()).isEqualTo(CURRENT_CREDENTIALS);
        assertThat(credentials.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(credentials.refreshToken()).isEqualTo(REFRESH_TOKEN);

        final Collection<GrantedAuthority> authorities = result.getAuthorities();
        assertThat(authorities)
                .hasSize(2);
    }

    @Test
    void buildAuthenticationToken_ShouldCreateTokenWithOnlyJwtRoleWhenNoExistingAuthorities() {
        // Arrange
        when(currentAuthentication.getAuthorities())
                .thenReturn(Collections.emptySet());

        // Act
        final FeatherAuthenticationToken result = classUnderTest.buildAuthenticationToken(
                mockUser
        );

        // Assert
        assertThat(result).isNotNull();
        final Collection<GrantedAuthority> authorities = result.getAuthorities();
        assertThat(authorities)
                .hasSize(1);
    }
}