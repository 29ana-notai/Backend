package notai.sttTask.presentation;

import lombok.RequiredArgsConstructor;
import notai.auth.Auth;
import notai.sttTask.application.SttTaskQueryService;
import notai.sttTask.application.result.SttTaskOverallStatusResult;
import notai.sttTask.presentation.response.SttTaskOverallStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SttTaskController {

    private final SttTaskQueryService sttTaskQueryService;

    @GetMapping("/status/{documentId}")
    public ResponseEntity<SttTaskOverallStatusResponse> fetchOverallStatus(
            @Auth Long memberId, @PathVariable("documentId") Long documentId
    ) {
        SttTaskOverallStatusResult result = sttTaskQueryService.fetchOverallStatus(memberId, documentId);
        return ResponseEntity.ok(SttTaskOverallStatusResponse.from(result));
    }
}
