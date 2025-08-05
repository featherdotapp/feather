package com.feather.api.adapter.linkedin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LinkedinUserInfoResponseDTOTest {

    @Test
    void testRecordFieldsAndEquality() {
        // Arrange
        final String subjectIdentifier = "sub123";
        final String fullName = "John Doe";
        final String firstName = "John";
        final String lastName = "Doe";
        final String picture = "http://pic.url";
        final Object locale = "en_US";
        final String email = "john@doe.com";
        final boolean isEmailVerified = true;

        // Act
        final LinkedinUserInfoResponseDTO someDto =
                new LinkedinUserInfoResponseDTO(subjectIdentifier, fullName, firstName, lastName, picture, locale, email, isEmailVerified);
        final LinkedinUserInfoResponseDTO dto3 =
                new LinkedinUserInfoResponseDTO("other", fullName, firstName, lastName, picture, locale, email, isEmailVerified);

        // Assert
        assertThat(someDto.subjectIdentifier()).isEqualTo(subjectIdentifier);
        assertThat(someDto.fullName()).isEqualTo(fullName);
        assertThat(someDto.firstName()).isEqualTo(firstName);
        assertThat(someDto.lastName()).isEqualTo(lastName);
        assertThat(someDto.picture()).isEqualTo(picture);
        assertThat(someDto.locale()).isEqualTo(locale);
        assertThat(someDto.email()).isEqualTo(email);
        assertThat(someDto.isEmailVerified()).isEqualTo(isEmailVerified);
        assertThat(someDto).isNotEqualTo(dto3);
        assertThat(someDto.toString()).contains(subjectIdentifier, fullName, firstName, lastName, picture, email);
    }

    @Test
    void testEqualsWithDifferentClass() {
        // Arrange
        final LinkedinUserInfoResponseDTO dto = new LinkedinUserInfoResponseDTO(
                "sub", "name", "first", "last", "pic", "locale", "email", true);
        final Object other = new Object();
        // Act & Assert
        assertThat(dto.equals(other)).isFalse();
    }
}

