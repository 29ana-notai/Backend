package notai.stt.application;

import lombok.RequiredArgsConstructor;
import notai.stt.domain.Stt;
import notai.stt.domain.SttRepository;
import notai.stt.presentation.response.SttPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SttQueryService {
    private final SttRepository sttRepository;

    public SttPageResponse getSttByPage(Long documentId, Integer pageNumber) {
        List<Stt> sttResults = sttRepository.findAllByDocumentIdAndPageNumber(documentId, pageNumber);
        return SttPageResponse.of(pageNumber, sttResults);
    }
} 