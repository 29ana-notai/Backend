package notai.llm.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import notai.auth.Auth;
import notai.llm.application.LlmTaskQueryService;
import notai.llm.application.LlmTaskService;
import notai.llm.application.command.LlmTaskPageResultCommand;
import notai.llm.application.command.LlmTaskPageStatusCommand;
import notai.llm.application.command.LlmTaskSubmitCommand;
import notai.llm.application.command.SummaryAndProblemUpdateCommand;
import notai.llm.application.result.*;
import notai.llm.presentation.request.LlmTaskSubmitRequest;
import notai.llm.presentation.request.SummaryAndProblemUpdateRequest;
import notai.llm.presentation.response.*;
import notai.member.domain.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/llm")
@RequiredArgsConstructor
public class LlmTaskController {

    private final LlmTaskService llmTaskService;
    private final LlmTaskQueryService llmTaskQueryService;

    @PostMapping
    public ResponseEntity<LlmTaskSubmitResponse> submitTask(@RequestBody @Valid LlmTaskSubmitRequest request) {
        LlmTaskSubmitCommand command = request.toCommand();
        LlmTaskSubmitResult result = llmTaskService.submitTasks(command);
        return ResponseEntity.accepted().body(LlmTaskSubmitResponse.from(result));
    }

    @GetMapping("/status/{documentId}")
    public ResponseEntity<LlmTaskOverallStatusResponse> fetchOverallStatus(
            @Auth Member member, @PathVariable("documentId") Long documentId
    ) {
        LlmTaskOverallStatusResult result = llmTaskQueryService.fetchOverallStatus(member, documentId);
        return ResponseEntity.ok(LlmTaskOverallStatusResponse.from(result));
    }

    @GetMapping("/status/{documentId}/{pageNumber}")
    public ResponseEntity<LlmTaskPageStatusResponse> fetchPageStatus(
            @Auth Member member,
            @PathVariable("documentId") Long documentId,
            @PathVariable("pageNumber") Integer pageNumber
    ) {
        LlmTaskPageStatusCommand command = LlmTaskPageStatusCommand.of(documentId, pageNumber);
        LlmTaskPageStatusResult result = llmTaskQueryService.fetchPageStatus(member, command);
        return ResponseEntity.ok(LlmTaskPageStatusResponse.from(result));
    }

    @GetMapping("/results/{documentId}")
    public ResponseEntity<LlmTaskAllPagesResultResponse> findAllPagesResult(
            @Auth Member member, @PathVariable("documentId") Long documentId
    ) {
        LlmTaskAllPagesResult result = llmTaskQueryService.findAllPagesResult(member, documentId);
        return ResponseEntity.ok(LlmTaskAllPagesResultResponse.from(result));
    }

    @GetMapping("/results/{documentId}/{pageNumber}")
    public ResponseEntity<LlmTaskPageResultResponse> findPageResult(
            @Auth Member member,
            @PathVariable("documentId") Long documentId,
            @PathVariable("pageNumber") Integer pageNumber
    ) {
        LlmTaskPageResultCommand command = LlmTaskPageResultCommand.of(documentId, pageNumber);
        LlmTaskPageResult result = llmTaskQueryService.findPageResult(member, command);
        return ResponseEntity.ok(LlmTaskPageResultResponse.from(result));
    }

    @PostMapping("/callback")
    public ResponseEntity<SummaryAndProblemUpdateResponse> handleTaskCallback(
            @RequestBody @Valid SummaryAndProblemUpdateRequest request
    ) {
        SummaryAndProblemUpdateCommand command = request.toCommand();
        Integer receivedPage = llmTaskService.updateSummaryAndProblem(command);
        return ResponseEntity.ok(SummaryAndProblemUpdateResponse.from(receivedPage));
    }
}
