package com.feather.api.jpa.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {

    public static final String DEFAULT_OAUTH_PROVIDER = "LinkedIn";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String email;

    private String accessToken;
    private String refreshToken;

    @Setter(AccessLevel.NONE)
    private String password = "<PASSWORD>";

    private List<Role> userRoles = List.of(Role.DEFAULT_USER);
    private Set<String> oAuthProviders = Set.of(DEFAULT_OAUTH_PROVIDER);

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    /**
     * Required by UserDetails but not used in this application.
     *
     * @return null
     */
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

}
