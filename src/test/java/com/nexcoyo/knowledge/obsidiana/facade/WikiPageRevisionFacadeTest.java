package com.nexcoyo.knowledge.obsidiana.facade;

import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.facade.support.AccessContext;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageRevisionService;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageService;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikiPageRevisionFacadeTest {

    @Mock
    private WikiPageRevisionService revisionService;
    @Mock
    private WikiPageService wikiPageService;
    @Mock
    private EntityReferenceResolver refs;

    @InjectMocks
    private WikiPageRevisionFacade facade;

    @Test
    void saveRejectsInvalidBase64CiphertextWithTypedError() {
        UUID actorId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();

        WikiPage pageRef = new WikiPage();
        pageRef.setId(pageId);
        AppUser userRef = new AppUser();
        userRef.setId(actorId);

        when(refs.page(pageId)).thenReturn(pageRef);
        when(refs.user(actorId)).thenReturn(userRef);

        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            pageId,
            null,
            "Encrypted",
            EditorType.CKEDITOR,
            null,
            null,
            "%%%invalid-base64%%%",
            "summary",
            true,
            "YWJjZGVmZ2hpams=",
            "YWJjZGVmZ2hpams=",
            "PBKDF2WithHmacSHA256:65536:256:YWJjZGVmZ2hpams=",
            false,
            actorId,
            true
        );

        assertThatThrownBy(() -> facade.save(request, AccessContext.admin(actorId)))
            .isInstanceOf(ApiException.class)
            .satisfies(ex -> {
                ApiException api = (ApiException) ex;
                assertThat(api.code()).isEqualTo(ErrorCode.REVISION_INVALID_CIPHERTEXT);
            });
    }
}

