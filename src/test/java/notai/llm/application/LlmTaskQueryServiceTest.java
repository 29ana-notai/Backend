package notai.llm.application;

import notai.common.exception.type.InternalServerErrorException;
import notai.common.exception.type.NotFoundException;
import notai.document.domain.Document;
import notai.document.domain.DocumentRepository;
import notai.llm.application.command.LlmTaskPageResultCommand;
import notai.llm.application.command.LlmTaskPageStatusCommand;
import notai.llm.application.result.LlmTaskAllPagesResult;
import notai.llm.application.result.LlmTaskOverallStatusResult;
import notai.llm.application.result.LlmTaskPageResult;
import notai.llm.application.result.LlmTaskPageStatusResult;
import notai.llm.query.LlmTaskQueryRepository;
import notai.member.domain.Member;
import notai.problem.domain.ProblemRepository;
import notai.problem.query.result.ProblemPageContentResult;
import notai.summary.domain.SummaryRepository;
import notai.summary.query.result.SummaryPageContentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static notai.llm.domain.TaskStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LlmTaskQueryServiceTest {

    @InjectMocks
    private LlmTaskQueryService llmTaskQueryService;

    @Mock
    private LlmTaskQueryRepository llmTaskQueryRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SummaryRepository summaryRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private Member member;

    @Mock
    private Document document;

    @Test
    void 작업_상태_확인시_존재하지_않는_문서ID로_요청한_경우_예외_발생() {
        // given
        given(documentRepository.getById(eq(2L))).willThrow(NotFoundException.class);

        // when & then
        assertAll(() -> assertThrows(
                NotFoundException.class,
                () -> llmTaskQueryService.fetchOverallStatus(member, 2L)
        ));
    }

    @Test
    void 작업_상태_확인시_모든_페이지의_작업이_완료된_경우_COMPLETED() {
        // given
        Long documentId = 1L;
        List<Long> summaryIds = List.of(1L, 2L, 3L);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryIdsByDocumentId(documentId)).willReturn(summaryIds);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(1L)).willReturn(COMPLETED);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(2L)).willReturn(COMPLETED);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(3L)).willReturn(COMPLETED);

        // when
        LlmTaskOverallStatusResult result = llmTaskQueryService.fetchOverallStatus(member, documentId);

        // then
        assertAll(
                () -> assertThat(result.overallStatus()).isEqualTo(COMPLETED),
                () -> assertThat(result.totalPages()).isEqualTo(3),
                () -> assertThat(result.completedPages()).isEqualTo(3),
                () -> verify(summaryRepository).getSummaryIdsByDocumentId(documentId),
                () -> verify(llmTaskQueryRepository).getTaskStatusBySummaryId(documentId)
        );
    }

    @Test
    void 작업_상태_확인시_모든_페이지의_작업이_완료되지_않은_경우_IN_PROGRESS() {
        // given
        Long documentId = 1L;
        List<Long> summaryIds = List.of(1L, 2L, 3L);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryIdsByDocumentId(documentId)).willReturn(summaryIds);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(1L)).willReturn(COMPLETED);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(2L)).willReturn(IN_PROGRESS);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(3L)).willReturn(PENDING);

        // when
        LlmTaskOverallStatusResult result = llmTaskQueryService.fetchOverallStatus(member, documentId);

        // then
        assertAll(
                () -> assertThat(result.overallStatus()).isEqualTo(IN_PROGRESS),
                () -> assertThat(result.totalPages()).isEqualTo(3),
                () -> assertThat(result.completedPages()).isEqualTo(1),
                () -> verify(summaryRepository).getSummaryIdsByDocumentId(documentId),
                () -> verify(llmTaskQueryRepository).getTaskStatusBySummaryId(documentId)
        );
    }

    @Test
    void 페이지별_작업_상태_확인() {
        // given
        Long documentId = 1L;
        Long summaryId = 1L;
        Integer pageNumber = 20;
        LlmTaskPageStatusCommand command = new LlmTaskPageStatusCommand(documentId, pageNumber);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryIdByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(summaryId);
        given(llmTaskQueryRepository.getTaskStatusBySummaryId(summaryId)).willReturn(IN_PROGRESS);

        // when
        LlmTaskPageStatusResult result = llmTaskQueryService.fetchPageStatus(member, command);

        // then
        assertThat(result.status()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void 페이지별_작업_상태_확인시_요청_기록이_없는_경우_NOT_REQUESTED() {
        // given
        Long documentId = 1L;
        Integer pageNumber = 20;
        LlmTaskPageStatusCommand command = new LlmTaskPageStatusCommand(documentId, pageNumber);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryIdByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(null);

        // when
        LlmTaskPageStatusResult result = llmTaskQueryService.fetchPageStatus(member, command);

        // then
        assertThat(result.status()).isEqualTo(NOT_REQUESTED);
    }

    @Test
    void 작업_결과_확인시_존재하지_않는_문서ID로_요청한_경우_예외_발생() {
        // given
        given(documentRepository.getById(eq(2L))).willThrow(NotFoundException.class);

        // when & then
        assertAll(() -> assertThrows(
                NotFoundException.class,
                () -> llmTaskQueryService.findAllPagesResult(member, 2L)
        ));
    }

    @Test
    void 작업_결과_확인시_생성된_요약과_문제의_수가_일치하지_않는_경우_예외_발생() {
        // given
        Long documentId = 1L;
        List<SummaryPageContentResult> summaryResults = List.of(new SummaryPageContentResult(1, "요약 내용"));
        List<ProblemPageContentResult> problemResults = List.of(
                new ProblemPageContentResult(1, "문제 내용"),
                new ProblemPageContentResult(2, "문제 내용")
        );

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getPageNumbersAndContentByDocumentId(documentId)).willReturn(summaryResults);
        given(problemRepository.getPageNumbersAndContentByDocumentId(documentId)).willReturn(problemResults);

        // when & then
        assertAll(
                () -> assertThrows(
                        InternalServerErrorException.class,
                        () -> llmTaskQueryService.findAllPagesResult(member, 1L)
                ),
                () -> verify(summaryRepository).getPageNumbersAndContentByDocumentId(documentId),
                () -> verify(problemRepository).getPageNumbersAndContentByDocumentId(documentId)
        );
    }

    @Test
    void 작업_결과_확인() {
        // given
        Long documentId = 1L;
        List<SummaryPageContentResult> summaryResults = List.of(
                new SummaryPageContentResult(1, "요약 내용"),
                new SummaryPageContentResult(2, "요약 내용")
        );
        List<ProblemPageContentResult> problemResults = List.of(
                new ProblemPageContentResult(1, "문제 내용"),
                new ProblemPageContentResult(2, "문제 내용")
        );

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getPageNumbersAndContentByDocumentId(documentId)).willReturn(summaryResults);
        given(problemRepository.getPageNumbersAndContentByDocumentId(documentId)).willReturn(problemResults);

        // when
        LlmTaskAllPagesResult response = llmTaskQueryService.findAllPagesResult(member, documentId);

        // then
        assertAll(
                () -> assertEquals(documentId, response.documentId()),
                () -> assertEquals(2, response.results().size()),
                () -> verify(summaryRepository).getPageNumbersAndContentByDocumentId(documentId),
                () -> verify(problemRepository).getPageNumbersAndContentByDocumentId(documentId)
        );
    }

    @Test
    void 페이지별_작업_결과_확인() {
        // given
        Long documentId = 1L;
        Integer pageNumber = 20;
        String summaryResult = "요약 내용";
        String problemResult = "문제 내용";
        LlmTaskPageResultCommand command = new LlmTaskPageResultCommand(documentId, pageNumber);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(
                summaryResult);
        given(problemRepository.getProblemContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(
                problemResult);

        // when
        LlmTaskPageResult result = llmTaskQueryService.findPageResult(member, command);

        // then
        assertAll(
                () -> assertThat(result.summary()).isEqualTo(summaryResult),
                () -> assertThat(result.problem()).isEqualTo(problemResult)
        );
    }

    @Test
    void 페이지별_작업_결과가_존재하지_않는_경우_예외가_아니라_null_응답() {
        // given
        Long documentId = 1L;
        Integer pageNumber = 20;
        LlmTaskPageResultCommand command = new LlmTaskPageResultCommand(documentId, pageNumber);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(null);
        given(problemRepository.getProblemContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(null);

        // when
        LlmTaskPageResult result = llmTaskQueryService.findPageResult(member, command);

        // then
        assertAll(
                () -> assertThat(result.summary()).isEqualTo(null),
                () -> assertThat(result.problem()).isEqualTo(null)
        );
    }

    @Test
    void 페이지별_작업_결과_확인시_요약과_문제_중_하나만_null인_경우_예외_발생() {
        // given
        Long documentId = 1L;
        Integer pageNumber = 20;
        String summaryResult = "요약 내용";
        LlmTaskPageResultCommand command = new LlmTaskPageResultCommand(documentId, pageNumber);

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        given(summaryRepository.getSummaryContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(
                summaryResult);
        given(problemRepository.getProblemContentByDocumentIdAndPageNumber(documentId, pageNumber)).willReturn(null);

        // when & then
        assertThrows(InternalServerErrorException.class, () -> llmTaskQueryService.findPageResult(member, command));
    }

    @Test
    void 요청_기록이_없는_문서의_작업_상태_확인시_NOT_REQUESTED() {
        // given
        Long documentId = 1L;

        given(summaryRepository.getSummaryIdsByDocumentId(documentId)).willReturn(Collections.emptyList());

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        // when
        LlmTaskOverallStatusResult result = llmTaskQueryService.fetchOverallStatus(member, documentId);

        // then
        assertAll(
                () -> assertThat(result.overallStatus()).isEqualTo(NOT_REQUESTED),
                () -> assertThat(result.totalPages()).isEqualTo(0),
                () -> assertThat(result.completedPages()).isEqualTo(0)
        );
    }

    @Test
    void 요청_기록이_없는_문서의_결과_확인시_empty_list_응답() {
        // given
        Long documentId = 1L;

        given(summaryRepository.getPageNumbersAndContentByDocumentId(documentId)).willReturn(Collections.emptyList());

        given(documentRepository.getById(eq(1L))).willReturn(document);
        willDoNothing().given(document).validateOwner(member);

        // when
        LlmTaskAllPagesResult result = llmTaskQueryService.findAllPagesResult(member, documentId);

        // then
        assertAll(
                () -> assertThat(result.totalPages()).isEqualTo(0),
                () -> assertThat(result.results()).isEmpty()
        );
    }
}
