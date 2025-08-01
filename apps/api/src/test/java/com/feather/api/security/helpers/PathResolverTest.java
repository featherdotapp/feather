package com.feather.api.security.helpers;

import com.feather.api.security.annotations.Unauthenticated;
import com.feather.api.security.exception_handling.exception.PathResolutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PathResolverTest {

    @InjectMocks
    private PathResolver classUnderTest;

    @Test
    void testResolvePath_getMapping() throws NoSuchMethodException {
        // Arrange
        @RequestMapping("/test")
        class TestClass {

            @Unauthenticated
            @GetMapping("/testMethod")
            public void testMethod() {
                // Test method
            }
        }

        // Act
        String result = classUnderTest.resolvePath(TestClass.class.getMethod("testMethod"), TestClass.class);

        // Assert
        assertThat(result).isEqualTo("/test/testMethod");
    }


    @Test
    void testResolvePath_multiplePaths() throws NoSuchMethodException {
        // Arrange
        @RequestMapping("/test")
        class TestClass {

            @Unauthenticated
            @GetMapping("/testMethod/get")
            public void testMethod() {
                // Test method
            }
        }

        // Act
        String result = classUnderTest.resolvePath(TestClass.class.getMethod("testMethod"), TestClass.class);

        // Assert
        assertThat(result).isEqualTo("/test/testMethod/get");
    }

    @Test
    void testResolvePath_hasNoMapping() throws NoSuchMethodException {
        // Arrange
        @RequestMapping("/test")
        class TestClass {

            @Unauthenticated
            public void testMethod() {
                // Test method
            }
        }
        Method method = TestClass.class.getMethod("testMethod");

        // Act & Assert
        assertThrows(
                PathResolutionException.class,
                () -> classUnderTest.resolvePath(method, TestClass.class)
        );
    }

    @Test
    void testResolvePath_withPathVariable() throws NoSuchMethodException {

        class TestClass {

            @Unauthenticated
            @GetMapping("/testMethod/{id}")
            public int testMethod(@PathVariable String id) {
                return Integer.parseInt(id);
            }
        }

        // Act
        String result = classUnderTest.resolvePath(TestClass.class.getMethod("testMethod", String.class), TestClass.class);

        // Assert
        assertThat(result).isEqualTo("/testMethod/**");
    }

    @Test
    void testResolvePath_NoSuchMethodException() throws NoSuchMethodException {
        // Arrange
        class TestClass {

            @Unauthenticated
            @GetMapping()
            public void testMethod() {
                // test
            }
        }
        Method method = TestClass.class.getMethod("testMethod");

        // Act & Assert
        assertThrows(
                PathResolutionException.class,
                () -> classUnderTest.resolvePath(method, TestClass.class)
        );

    }

    @Test
    void testResolvePath_MultiPaths() throws NoSuchMethodException {
        // Arrange
        class TestClass {

            @Unauthenticated
            @GetMapping(value = {"/someTest", "/anotherTest"})
            public void testMethod() {
                // test
            }
        }
        Method method = TestClass.class.getMethod("testMethod");

        // Act & Assert
        assertThrows(
                PathResolutionException.class,
                () -> classUnderTest.resolvePath(method, TestClass.class)
        );

    }
}
