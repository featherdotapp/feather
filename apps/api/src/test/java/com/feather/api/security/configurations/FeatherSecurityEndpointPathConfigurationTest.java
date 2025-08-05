package com.feather.api.security.configurations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;

import com.feather.api.security.annotations.ApiKeyAuthenticated;
import com.feather.api.security.annotations.FullyAuthenticated;
import com.feather.api.security.annotations.Unauthenticated;
import com.feather.api.security.configurations.model.EndpointPaths;
import com.feather.api.security.exception_handling.exception.PathResolutionException;
import com.feather.api.security.helpers.PathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class FeatherSecurityEndpointPathConfigurationTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private PathResolver pathResolver;

    @InjectMocks
    private FeatherSecurityEndpointPathConfiguration classUnderTest;

    @Test
    void applicationPaths_ShouldCategorizeEndpointsCorrectly() throws NoSuchMethodException {
        // Arrange
        final TestController controller = new TestController();
        final Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        final Method secureMethod = TestController.class.getMethod("secureEndpoint");
        final Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");
        final Method publicMethod = TestController.class.getMethod("publicEndpoint");

        when(pathResolver.resolvePath(secureMethod, TestController.class)).thenReturn("/secure");
        when(pathResolver.resolvePath(apiKeyMethod, TestController.class)).thenReturn("/api-key");
        when(pathResolver.resolvePath(publicMethod, TestController.class)).thenReturn("/public");

        // Act
        final EndpointPaths result = classUnderTest.applicationPaths();

        // Assert
        assertThat(result.fullyAuthenticatedPaths()).containsExactly("/secure");
        assertThat(result.apiKeyAuthenticatedPaths()).containsExactly("/api-key");
        assertThat(result.unauthenticatedPaths()).containsExactly("/public");
    }

    @Test
    void applicationPaths_ShouldThrowException_WhenDuplicatePathsExist() throws NoSuchMethodException {
        // Arrange
        final TestController controller = new TestController();
        final Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        final Method secureMethod = TestController.class.getMethod("secureEndpoint");
        final Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");

        when(pathResolver.resolvePath(secureMethod, TestController.class)).thenReturn("/duplicate");
        when(pathResolver.resolvePath(apiKeyMethod, TestController.class)).thenReturn("/duplicate");

        // Act & Assert
        assertThrows(PathResolutionException.class, () -> classUnderTest.applicationPaths());
    }

    @Test
    void applicationPaths_ShouldHandleEmptyControllerList() {
        // Arrange
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(Map.of());

        // Act
        final EndpointPaths result = classUnderTest.applicationPaths();

        // Assert
        assertThat(result.fullyAuthenticatedPaths()).isEmpty();
        assertThat(result.apiKeyAuthenticatedPaths()).isEmpty();
        assertThat(result.unauthenticatedPaths()).isEmpty();
    }

    @Test
    void applicationPaths_ShouldHandlePathResolutionException() throws NoSuchMethodException {
        // Arrange
        final TestController controller = new TestController();
        final Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        final Method secureMethod = TestController.class.getMethod("secureEndpoint");

        when(pathResolver.resolvePath(secureMethod, TestController.class))
                .thenThrow(new PathResolutionException(PathResolutionException.ErrorType.DUPLICATE_PATH, "Test error"));

        // Act & Assert
        assertThrows(
                PathResolutionException.class,
                () -> classUnderTest.applicationPaths()
        );
    }

    @Test
    void applicationPaths_ShouldThrowException_WhenDuplicatePathExistsInThirdSet() throws NoSuchMethodException {
        // Arrange
        final TestController controller = new TestController();
        final Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        final Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");
        final Method unauthenticatedMethod = TestController.class.getMethod("publicEndpoint");

        when(pathResolver.resolvePath(apiKeyMethod, TestController.class)).thenReturn("/duplicate");
        when(pathResolver.resolvePath(unauthenticatedMethod, TestController.class)).thenReturn("/duplicate");

        // Act & Assert
        assertThrows(
                Exception.class,
                () -> classUnderTest.applicationPaths()
        );
    }

    @Test
    void applicationPaths_ShouldThrowException_WhenPathExistsInSet2Only() throws NoSuchMethodException {
        // Arrange
        final TestController2 controller2 = new TestController2();
        final Map<String, Object> controllers = Map.of("testController2", controller2);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        final Method apiKeyMethod = TestController2.class.getMethod("apiKeyEndpoint");
        final Method publicMethod = TestController2.class.getMethod("publicEndpoint");

        when(pathResolver.resolvePath(apiKeyMethod, TestController2.class)).thenReturn("/unique");
        when(pathResolver.resolvePath(publicMethod, TestController2.class)).thenReturn("/unique");

        // Act & Assert
        assertThrows(PathResolutionException.class, () -> classUnderTest.applicationPaths());
    }

    @RestController
    static class TestController {

        @FullyAuthenticated
        @GetMapping("/secure")
        public void secureEndpoint() {
            // test
        }

        @ApiKeyAuthenticated
        @GetMapping("/api-key")
        public void apiKeyEndpoint() {
            // test
        }

        @Unauthenticated
        @GetMapping("/public")
        public void publicEndpoint() {
            // test
        }

        @GetMapping("/no-annotation")
        public void noAnnotationEndpoint() {
            // test
        }
    }

    @RestController
    static class TestController2 {

        @ApiKeyAuthenticated
        @GetMapping("/api-key")
        public void apiKeyEndpoint() {
            // test
        }

        @Unauthenticated
        @GetMapping("/public")
        public void publicEndpoint() {
            // test
        }
    }
}
