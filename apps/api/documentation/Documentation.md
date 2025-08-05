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

The security architecture is based on Spring Security with custom filters and authentication providers:

### Security Filter Chains

1. **Public Chain** (external services related endpoints like LinkedIn):
    - Allows unauthenticated access to specific endpoints like `/auth/linkedin/callback`

2. **API Key Chain** (no authenticated user-related endpoints):
    - Secures endpoints that require API key authentication (e.g., `/auth/linkedin/loginUrl`)
    - Uses `ApiKeyFilter` and `ApiKeyAuthenticationProvider`

3. **Fully Authenticated Chain** (authenticated user-related endpoints):
    - Requires both API key and JWT token authentication
    - Uses both `ApiKeyFilter` and `JwtTokenFilter`
    - Example endpoint: `/auth/linkedin/isAuthenticated`

### Custom Authentication Components

- **Filters**:
    - `ApiKeyFilter`: Extracts and validates API key from requests
    - `JwtTokenFilter`: Handles JWT token validation and refresh flow
        - Extracts access token from Authorization header
        - Extracts refresh token from HTTP-only cookie
        - Manages token refresh when access token expires

- **Authentication Providers**:
    - `ApiKeyAuthenticationProvider`: Validates API keys and generates appropriate authentication token
    - `JwtTokenAuthenticationProvider`: Handles JWT token validation and refresh
        - Validates both access and refresh tokens
        - Generates new access token if current one is expired
        - Maintains user's authentication state

- **Authentication Tokens**:
    - `ApiKeyAuthenticationToken`: Represents API key authentication
    - `JwtAuthenticationToken`: Represents JWT token authentication
    - `FeatherAuthenticationToken`: Base token class for session persistence
    - `FeatherCredentials`: Record storing authentication credentials
        - currentCredentials: Current authentication state (API key)
        - accessToken: JWT access token
        - refreshToken: JWT refresh token

## API Endpoints

### Endpoints

All endpoints are defined in the openapi.json

## Adding New Endpoints

To add new endpoints to the Feather application

1. **Create Controller Method**:

2. **Configure Security**:
    - Determine required authentication level for your endpoint
    - Add appropriate security level in your endpoint using annotations:
        - `@ApiKeyAuthenticate`: api key required
        - `@FullyAuthenticated`: api key + jwt access and refresh token required
        - `@Unauthenticated`: no auth required endpoint
    ```java
    @FullyAuthenticated
    @GetMapping("/isAuthenticated")
    public ResponseEntity<Boolean> isAuthenticated() {
         return ResponseEntity.ok(Boolean.TRUE);
     }
     ```

    ```
    When creating a new endpointt, do not add the request parameter in the Mapping annotation, it could lead to problems with the Custom Authentication 
   Annotation. Instead use @RequestParameter in the parameters of the method. The path variable is resolved by the annotations adding to the security 
   matchers the path with a /** matcher at the end, but this could lead to problems when adding similar paths.
    ```

## JWT Token Management

The application uses JWT tokens for authentication with two types:

1. **Access Token**:
    - Short-lived token used for API authentication
    - Contains user roles and permissions
    - Sent in Authorization header as "Bearer {token}"
    - Automatically refreshed when expired if refresh token is valid

2. **Refresh Token**:
    - Long-lived token used to get new access tokens
    - Stored as HTTP-only cookie for security
    - Used when access token expires
    - Validated against user's stored refresh token

### Token Refresh Flow

1. Client sends request with expired access token
2. JwtTokenFilter detects expired token
3. If refresh token is valid:
    - New access token is generated
    - Token is returned in Authorization header
    - Request continues with new token
4. If refresh token is invalid:
    - Authentication error is returned
    - User must re-authenticate
5. Refresh token regeneration:
    - If refresh token is valid, and it's expiry time is closer than 7 days, a new refresh token is generated and sent back to the client

## OAuth2 Integration

The application integrates LinkedIn OAuth2 for user authentication. The LinkedIn login is then used to collect user information to simplify the user
registration flow.