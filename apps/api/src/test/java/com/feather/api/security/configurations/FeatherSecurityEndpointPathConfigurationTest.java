package com.feather.api.security.configurations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.feather.api.security.annotations.ApiKeyAuthenticated;
import com.feather.api.security.annotations.FullyAuthenticated;
import com.feather.api.security.annotations.Unauthenticated;
import com.feather.api.security.configurations.model.EndpointPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class FeatherSecurityEndpointPathConfigurationTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private FeatherSecurityEndpointPathConfiguration config;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
    }

    @Test
    void testApplicationPaths_AllBranches() {
        final Map<String, Object> beans = Map.of(
                "testController", new TestController(),
                "noRequestMappingController", new NoRequestMappingController(),
                "emptyMappingController", new EdgeCaseController()
        );
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(beans);
        final EndpointPaths paths = config.applicationPaths();
        assertThat(paths.unauthenticatedPaths()).containsExactlyInAnyOrder("/base/public", "/no-base", "/empty");
        assertThat(paths.fullyAuthenticatedPaths()).containsExactlyInAnyOrder("/base/private");
        assertThat(paths.apiKeyAuthenticatedPaths()).containsExactlyInAnyOrder("/base/api");
    }

    @RestController
    @RequestMapping("/base")
    static class TestController {

        @GetMapping("/public")
        @Unauthenticated
        public void publicEndpoint() {
            // test
        }

        @PostMapping("/private")
        @FullyAuthenticated
        public void privateEndpoint() {
            // test
        }

        @PutMapping("/api")
        @ApiKeyAuthenticated
        public void apiEndpoint() {
            // test
        }

        @DeleteMapping
        public void noAuthAnnotation() {
            // test
        }
    }

    @RestController
    static class NoRequestMappingController {

        @GetMapping("/no-base")
        @Unauthenticated
        public void endpoint() {
            // test
        }
    }

    @RequestMapping("/empty")
    @RestController
    static class EdgeCaseController {

        @GetMapping
        @Unauthenticated
        public void emptyMapping() {
            // test
        }

        @GetMapping("/test/{someVariable}")
        @Unauthenticated
        public String withRequestParameter(@PathVariable final String someVariable) {
            // test
            return someVariable;
        }
    }

}

