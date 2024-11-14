package notai.sttTask.application;

import lombok.RequiredArgsConstructor;
import notai.document.domain.Document;
import notai.document.domain.DocumentRepository;
import notai.llm.domain.TaskStatus;
import static notai.llm.domain.TaskStatus.*;
import notai.member.domain.Member;
import notai.member.domain.MemberRepository;
import notai.stt.domain.Stt;
import notai.stt.domain.SttRepository;
import notai.sttTask.application.result.SttTaskOverallStatusResult;
import notai.sttTask.domain.SttTask;
import notai.sttTask.domain.SttTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SttTaskQueryService {

    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;
    private final SttTaskRepository sttTaskRepository;
    private final SttRepository sttRepository;

    public SttTaskOverallStatusResult fetchOverallStatus(Long memberId, Long documentId) {
        Document foundDocument = documentRepository.getById(documentId);
        Member member = memberRepository.getById(memberId);
        foundDocument.validateOwner(member);

        List<Stt> sttResults = sttRepository.findAllByDocumentId(documentId);

        if (sttResults.isEmpty()) {
            return SttTaskOverallStatusResult.of(documentId, NOT_REQUESTED, 0, 0);
        }
        List<TaskStatus> taskStatuses =
                sttTaskRepository.findAllBySttIn(sttResults).stream().map(SttTask::getStatus).toList();


        int totalPages = taskStatuses.size();
        int completedPages = Collections.frequency(taskStatuses, COMPLETED);

        if (totalPages == completedPages) {
            return SttTaskOverallStatusResult.of(documentId, COMPLETED, totalPages, completedPages);
        }
        return SttTaskOverallStatusResult.of(documentId, IN_PROGRESS, totalPages, completedPages);
    }
}
