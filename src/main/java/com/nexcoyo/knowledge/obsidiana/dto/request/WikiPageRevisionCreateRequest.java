package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WikiPageRevisionCreateRequest(
    UUID id,
    @NotNull UUID pageId,
    Integer revisionNumber,
    @NotBlank @Size(max = 255) String titleSnapshot,
    EditorType editorType,
    @Size(max = 2_000_000) String contentHtml,
    @Size(max = 2_000_000) String contentText,
    @Size(max = 4_000_000) String contentCiphertext,
    @Size(max = 500) String changeSummary,
    @NotNull Boolean isEncrypted,
    @Pattern(regexp = "^[A-Za-z0-9+/]+={0,2}$", message = "contentIv must be valid Base64")
    String contentIv,
    @Pattern(regexp = "^[A-Za-z0-9+/]+={0,2}$", message = "contentAuthTag must be valid Base64")
    String contentAuthTag,
    @Pattern(regexp = "^PBKDF2WithHmacSHA256:[0-9]+:[0-9]+:[A-Za-z0-9+/]+={0,2}$", message = "encryptionKdf has invalid format")
    String encryptionKdf,
    @NotNull Boolean isPinned,
    UUID createdBy,
    @NotNull Boolean updatePagePointer
) {

    @AssertTrue(message = "Encrypted revisions require contentCiphertext, contentIv, contentAuthTag and encryptionKdf")
    public boolean hasValidEncryptedPayload() {
        if (!Boolean.TRUE.equals(isEncrypted)) {
            return true;
        }
        return isNotBlank(contentCiphertext)
            && isNotBlank(contentIv)
            && isNotBlank(contentAuthTag)
            && isNotBlank(encryptionKdf)
            && isBlank(contentHtml)
            && isBlank(contentText);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
