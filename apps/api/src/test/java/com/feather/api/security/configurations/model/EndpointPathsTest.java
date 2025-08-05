package com.feather.api.security.configurations.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EndpointPathsTest {

    @Test
    void testEqualsAndHashCode() {
        final String[] full = { "/full1", "/full2" };
        final String[] api = { "/api1" };
        final String[] unauth = { "/unauth1", "/unauth2" };

        final EndpointPaths ep1 = new EndpointPaths(full, api, unauth);
        final EndpointPaths ep2 = new EndpointPaths(full.clone(), api.clone(), unauth.clone());
        final EndpointPaths ep3 = new EndpointPaths(new String[] { "/other" }, api, unauth);

        assertEquals(ep1, ep2);
        assertEquals(ep1.hashCode(), ep2.hashCode());
        assertNotEquals(ep1, ep3);
        assertNotEquals(ep1.hashCode(), ep3.hashCode());
        assertNotEquals(null, ep1);
        assertNotEquals("not an EndpointPaths", ep1);
    }

    @Test
    void testDifferentOrderArraysNotEqual() {
        final String[] full1 = { "/full1", "/full2" };
        final String[] full2 = { "/full2", "/full1" };
        final String[] api = { "/api1" };
        final String[] unauth = { "/unauth1", "/unauth2" };
        final EndpointPaths ep1 = new EndpointPaths(full1, api, unauth);
        final EndpointPaths ep2 = new EndpointPaths(full2, api, unauth);
        assertNotEquals(ep1, ep2);
    }

    @Test
    void testAllFieldsDifferent() {
        final EndpointPaths ep1 = new EndpointPaths(new String[] { "/a" }, new String[] { "/b" }, new String[] { "/c" });
        final EndpointPaths ep2 = new EndpointPaths(new String[] { "/x" }, new String[] { "/y" }, new String[] { "/z" });
        assertNotEquals(ep1, ep2);
    }

    @Test
    void testToString() {
        final String[] full = { "/full1" };
        final String[] api = { "/api1" };
        final String[] unauth = { "/unauth1" };
        final EndpointPaths ep = new EndpointPaths(full, api, unauth);
        final String str = ep.toString();
        assertTrue(str.contains("fullyAuthenticatedPaths=[/full1]"));
        assertTrue(str.contains("apiKeyAuthenticatedPaths=[/api1]"));
        assertTrue(str.contains("unauthenticatedPaths=[/unauth1]"));
    }

    @Test
    void testToStringWithEmptyArrays() {
        final EndpointPaths ep = new EndpointPaths(new String[] {}, new String[] {}, new String[] {});
        final String str = ep.toString();
        assertTrue(str.contains("fullyAuthenticatedPaths=[]"));
        assertTrue(str.contains("apiKeyAuthenticatedPaths=[]"));
        assertTrue(str.contains("unauthenticatedPaths=[]"));
    }

    @Test
    void testEmptyArrays() {
        final EndpointPaths ep = new EndpointPaths(new String[] {}, new String[] {}, new String[] {});
        assertArrayEquals(new String[] {}, ep.fullyAuthenticatedPaths());
        assertArrayEquals(new String[] {}, ep.apiKeyAuthenticatedPaths());
        assertArrayEquals(new String[] {}, ep.unauthenticatedPaths());
        assertEquals(ep.hashCode(), ep.hashCode());
    }

    @Test
    void testNullSafety() {
        final EndpointPaths ep = new EndpointPaths(new String[] { "/a" }, new String[] { "/b" }, new String[] { "/c" });
        final boolean result = ep.equals(null);
        assertThat(result).isFalse();
    }

    @Test
    void testEqualsNullAndDifferentClass() {
        final EndpointPaths ep = new EndpointPaths(new String[] { "/a" }, new String[] { "/b" }, new String[] { "/c" });
        assertNotEquals(null, ep);
        assertNotEquals("not an EndpointPaths", ep); // getClass() != o.getClass()
    }

    @Test
    void testEqualsWithDifferentClass() {
        // Arrange
        final EndpointPaths ep = new EndpointPaths(new String[] { "/a" }, new String[] { "/b" }, new String[] { "/c" });
        final Object other = new Object();
        // Act & Assert
        assertThat(ep.equals(other)).isFalse();
    }

    @Test
    void testEqualsWithLastFieldDifferent() {
        // Arrange
        final String[] full = { "/full1" };
        final String[] api = { "/api1" };
        final String[] unauth1 = { "/unauth1" };
        final String[] unauth2 = { "/unauth2" };
        final EndpointPaths ep1 = new EndpointPaths(full, api, unauth1);
        final EndpointPaths ep2 = new EndpointPaths(full, api, unauth2);
        // Act & Assert
        assertThat(ep1.equals(ep2)).isFalse();
    }
}
