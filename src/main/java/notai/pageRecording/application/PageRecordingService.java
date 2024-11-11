package notai.pageRecording.application;

import lombok.RequiredArgsConstructor;
import notai.document.domain.Document;
import notai.document.domain.DocumentRepository;
import notai.member.domain.Member;
import notai.member.domain.MemberRepository;
import notai.pageRecording.application.command.PageRecordingSaveCommand;
import notai.pageRecording.domain.PageRecording;
import notai.pageRecording.domain.PageRecordingRepository;
import notai.recording.domain.Recording;
import notai.recording.domain.RecordingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PageRecordingService {

    private final PageRecordingRepository pageRecordingRepository;
    private final RecordingRepository recordingRepository;
    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;

    public void savePageRecording(Long memberId, PageRecordingSaveCommand command) {
        Recording foundRecording = recordingRepository.getById(command.recordingId());
        Document foundDocument = documentRepository.getById(command.documentId());
        Member member = memberRepository.getById(memberId);

        foundDocument.validateOwner(member);
        foundRecording.validateDocumentOwnership(foundDocument);

        command.sessions().forEach(session -> {
            PageRecording pageRecording = new PageRecording(
                    foundRecording,
                    session.pageNumber(),
                    session.startTime(),
                    session.endTime()
            );
            pageRecordingRepository.save(pageRecording);
        });
    }
}
