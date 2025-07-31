package com.feather.api.security.configurations;

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

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatherSecurityEndpointPathConfigurationTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private PathResolver pathResolver;

    @InjectMocks
    private FeatherSecurityEndpointPathConfiguration classUnderTest;

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

    @Test
    void applicationPaths_ShouldCategorizeEndpointsCorrectly() throws NoSuchMethodException {
        // Arrange
        TestController controller = new TestController();
        Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        Method secureMethod = TestController.class.getMethod("secureEndpoint");
        Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");
        Method publicMethod = TestController.class.getMethod("publicEndpoint");

        when(pathResolver.resolvePath(secureMethod, TestController.class)).thenReturn("/secure");
        when(pathResolver.resolvePath(apiKeyMethod, TestController.class)).thenReturn("/api-key");
        when(pathResolver.resolvePath(publicMethod, TestController.class)).thenReturn("/public");

        // Act
        EndpointPaths result = classUnderTest.applicationPaths();

        // Assert
        assertThat(result.fullyAuthenticatedPaths()).containsExactly("/secure");
        assertThat(result.apiKeyAuthenticatedPaths()).containsExactly("/api-key");
        assertThat(result.unauthenticatedPaths()).containsExactly("/public");
    }

    @Test
    void applicationPaths_ShouldThrowException_WhenDuplicatePathsExist() throws NoSuchMethodException {
        // Arrange
        TestController controller = new TestController();
        Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        Method secureMethod = TestController.class.getMethod("secureEndpoint");
        Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");

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
        EndpointPaths result = classUnderTest.applicationPaths();

        // Assert
        assertThat(result.fullyAuthenticatedPaths()).isEmpty();
        assertThat(result.apiKeyAuthenticatedPaths()).isEmpty();
        assertThat(result.unauthenticatedPaths()).isEmpty();
    }

    @Test
    void applicationPaths_ShouldHandlePathResolutionException() throws NoSuchMethodException {
        // Arrange
        TestController controller = new TestController();
        Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        Method secureMethod = TestController.class.getMethod("secureEndpoint");
        when(pathResolver.resolvePath(secureMethod, TestController.class))
                .thenThrow(new PathResolutionException(PathResolutionException.ErrorType.DUPLICATE_PATH, "Test error"));

        // Act & Assert
        assertThrows(PathResolutionException.class, () -> classUnderTest.applicationPaths());
    }

    @Test
    void applicationPaths_ShouldThrowException_WhenDuplicatePathExistsInThirdSet() throws NoSuchMethodException {
        // Arrange
        TestController controller = new TestController();
        Map<String, Object> controllers = Map.of("testController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        Method apiKeyMethod = TestController.class.getMethod("apiKeyEndpoint");
        Method fullyAuthenticatedMethod = TestController.class.getMethod("secureEndpoint");
        Method unauthenticatedMethod = TestController.class.getMethod("publicEndpoint");

        // Set up paths so that unauthenticatedPaths (set2) contains the duplicate path
        when(pathResolver.resolvePath(apiKeyMethod, TestController.class)).thenReturn("/duplicate");
        when(pathResolver.resolvePath(fullyAuthenticatedMethod, TestController.class)).thenReturn("/secure");
        when(pathResolver.resolvePath(unauthenticatedMethod, TestController.class)).thenReturn("/duplicate");

        // Act & Assert
        assertThrows(
                PathResolutionException.class,
                () -> classUnderTest.applicationPaths()
        );
    }
}
