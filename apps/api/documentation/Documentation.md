# Feather Application Documentation

## Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [Security Implementation](#security-implementation)
3. [API Endpoints](#api-endpoints)
4. [Adding New Endpoints](#adding-new-endpoints)
5. [JWT Token Management](#jwt-token-management)
6. [OAuth2 Integration](#oauth2-integration)

## Authentication Flow

The authentication flow in Feather follows these steps:

1. **OAuth2 Initiation**:
    - Frontend calls `/auth/linkedin/loginUrl` (requires API key)
    - Backend returns LinkedIn OAuth2 authorization URL

2. **User Authorization**:
    - User is redirected to LinkedIn for authorization
    - LinkedIn redirects back to `/auth/linkedin/callback` with authorization code

3. **Token Exchange**:
    - Backend exchanges code for LinkedIn access token
    - User information is retrieved from LinkedIn
    - Backend creates/updates user and generates JWT tokens (access and refresh)

4. **Redirect to Frontend**:
    - Backend sets refresh token as an HTTP-only cookie
    - Access token is passed as a header to the frontend
    - User is redirected to frontend URL

5. **Subsequent Requests**:
    - Frontend sends access token in Authorization header
    - If access token expires, refresh token (stored in cookie) is used to get a new access token. SignUp is not required for later requests as long as the
      refresh token is valid.

## Security Implementation

For a detailed overview of the security implementation, check the `SpringSecurity.puml` file.

The security architecture is based on Spring Security with custom filters and authentication providers:

### Security Filter Chains

1. **Public Chain**:
    - Allows unauthenticated access to specific endpoints like `/auth/linkedin/callback`

2. **API Key Chain**:
    - Secures endpoints that require API key authentication (e.g., `/auth/linkedin/loginUrl`)
    - Uses `ApiKeyFilter` and `ApiKeyAuthenticationProvider`

3. **Fully Authenticated Chain**:
    - Requires both API key and JWT token authentication
    - Uses both `ApiKeyFilter` and `JwtTokenFilter`
    - Example endpoint: `/auth/linkedin/isAuthenticated`

### Custom Authentication Components

- **Filters**:
    - `ApiKeyFilter`: Extracts API key from requests
    - `JwtTokenFilter`: Extracts JWT tokens from requests

- **Authentication Providers**:
    - `ApiKeyAuthenticationProvider`: Validates API keys
    - `JwtTokenAuthenticationProvider`: Validates JWT tokens and generates a new access token if needed, and if refresh token is valid

- **Authentication Tokens**:
    - `ApiKeyAuthenticationToken`: Represents API key authentication
    - `JwtAuthenticationToken`: Represents JWT token authentication
    - `FeatherAuthenticationToken`: Base token class for authentication

## API Endpoints

### Endpoints

All endpoints are defined in the openapi.json

## Adding New Endpoints

To add new endpoints to the Feather application

1. **Create Controller Method**:

2. **Configure Security**:
    - Determine required authentication level for your endpoint
    - Add endpoint to appropriate security filter chain in `FeatherSecurityConfiguration`:
      ```java
      @Bean
      public SecurityFilterChain chain(final HttpSecurity http) throws Exception {
          http
              .securityMatcher("/your-path/**") // Add the path for the new endpoint
              .authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority());
          return http.build();
      }
      ```

## JWT Token Management

The application uses JWT tokens for authentication with two types:

1. **Access Token**:
    - Short-lived token used for API authentication
    - Contains user roles and permissions
    - Sent in Authorization header as "Bearer {token}"

2. **Refresh Token**:
    - Long-lived token used to get new access tokens
    - Stored as HTTP-only cookie
    - Used when access token expires

## OAuth2 Integration

The application integrates LinkedIn OAuth2 for user authentication. The LinkedIn login is then used to collect user information to simplify the user
registration flow.