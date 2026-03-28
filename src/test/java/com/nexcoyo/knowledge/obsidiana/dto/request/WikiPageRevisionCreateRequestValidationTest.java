package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WikiPageRevisionCreateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void encryptedPayloadRequiresCipherMetadata() {
        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            null,
            "Encrypted",
            EditorType.CKEDITOR,
            null,
            null,
            null,
            "summary",
            true,
            null,
            null,
            null,
            false,
            UUID.randomUUID(),
            true
        );

        Set<jakarta.validation.ConstraintViolation<WikiPageRevisionCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(jakarta.validation.ConstraintViolation::getMessage)
            .contains("Encrypted revisions require contentCiphertext, contentIv, contentAuthTag and encryptionKdf");
    }

    @Test
    void rejectsInvalidCryptoFormatFields() {
        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            null,
            "Encrypted",
            EditorType.CKEDITOR,
            null,
            null,
            "dGVzdA==",
            "summary",
            true,
            "not-base64***",
            "###",
            "bad-kdf",
            false,
            UUID.randomUUID(),
            true
        );

        Set<jakarta.validation.ConstraintViolation<WikiPageRevisionCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(jakarta.validation.ConstraintViolation::getMessage)
            .contains("contentIv must be valid Base64", "contentAuthTag must be valid Base64", "encryptionKdf has invalid format");
    }
}

