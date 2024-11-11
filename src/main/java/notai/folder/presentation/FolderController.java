package notai.folder.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import notai.auth.Auth;
import notai.document.application.DocumentQueryService;
import notai.document.application.result.DocumentFindResult;
import notai.document.presentation.response.DocumentFindResponse;
import notai.folder.application.FolderQueryService;
import notai.folder.application.FolderService;
import notai.folder.application.result.FolderFindResult;
import notai.folder.application.result.FolderMoveResult;
import notai.folder.application.result.FolderSaveResult;
import notai.folder.application.result.FolderUpdateResult;
import notai.folder.presentation.request.FolderMoveRequest;
import notai.folder.presentation.request.FolderSaveRequest;
import notai.folder.presentation.request.FolderUpdateRequest;
import notai.folder.presentation.response.*;
import notai.member.domain.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final FolderQueryService folderQueryService;
    private final DocumentQueryService documentQueryService;

    private static final Long ROOT_ID = -1L;

    @PostMapping
    public ResponseEntity<FolderSaveResponse> saveFolder(
            @Auth Member member, @Valid @RequestBody FolderSaveRequest folderSaveRequest
    ) {
        FolderSaveResult folderResult = saveFolderResult(member, folderSaveRequest);
        FolderSaveResponse response = FolderSaveResponse.from(folderResult);
        return ResponseEntity.created(URI.create("/api/folders/" + response.id())).body(response);
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<FolderMoveResponse> moveFolder(
            @Auth Member member, @PathVariable Long id, @Valid @RequestBody FolderMoveRequest folderMoveRequest
    ) {
        FolderMoveResult folderResult = moveFolderWithRequest(member, id, folderMoveRequest);
        FolderMoveResponse response = FolderMoveResponse.from(folderResult);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderUpdateResponse> updateFolder(
            @Auth Member member, @PathVariable Long id, @Valid @RequestBody FolderUpdateRequest folderUpdateRequest
    ) {
        FolderUpdateResult folderResult = folderService.updateFolder(member, id, folderUpdateRequest);
        FolderUpdateResponse response = FolderUpdateResponse.from(folderResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<FindResponseWrapper>> getFolders(
            @Auth Member member, @PathVariable Long id
    ) {
        List<FindResponseWrapper> result = new ArrayList<>();

        insertFolderFindResponse(result, member, id);
        insertDocumentFindResponse(result, member, id);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(
            @Auth Member member, @PathVariable Long id
    ) {
        folderService.deleteFolder(member, id);
        return ResponseEntity.noContent().build();
    }

    private FolderSaveResult saveFolderResult(Member member, FolderSaveRequest folderSaveRequest) {
        if (folderSaveRequest.parentFolderId() != null) {
            return folderService.saveSubFolder(member, folderSaveRequest);
        }
        return folderService.saveRootFolder(member, folderSaveRequest);
    }

    private FolderMoveResult moveFolderWithRequest(Member member, Long id, FolderMoveRequest folderMoveRequest) {
        if (folderMoveRequest.targetFolderId() != null) {
            return folderService.moveNewParentFolder(member, id, folderMoveRequest);
        }
        return folderService.moveRootFolder(member, id);
    }

    private void insertFolderFindResponse(List<FindResponseWrapper> result, Member member, Long folderId) {
        List<FolderFindResult> folderResults = folderQueryService.getFolders(member, folderId);
        List<FolderFindResponse> folderResponses = folderResults.stream().map(FolderFindResponse::from).toList();

        for (FolderFindResponse response : folderResponses) {
            result.add(FindResponseWrapper.fromFolderFindResponse(response));
        }
    }

    private void insertDocumentFindResponse(List<FindResponseWrapper> result, Member member, Long folderId) {
        List<DocumentFindResult> documentResults;
        if (folderId == null || folderId.equals(ROOT_ID)) {
            documentResults = documentQueryService.findRootDocuments(member.getId());
        } else {
            documentResults = documentQueryService.findDocuments(folderId);
        }
        List<DocumentFindResponse> documentResponses =
                documentResults.stream().map(DocumentFindResponse::from).toList();

        for (DocumentFindResponse response : documentResponses) {
            result.add(FindResponseWrapper.fromDocumentFindResponse(response));
        }
    }
}
