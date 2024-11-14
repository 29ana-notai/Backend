package notai.sttTask.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import notai.llm.domain.TaskStatus;
import static notai.stt.domain.QStt.stt;
import static notai.sttTask.domain.QSttTask.sttTask;
import notai.sttTask.domain.SttTask;

import java.util.List;

@RequiredArgsConstructor
public class SttTaskQueryRepositoryImpl implements SttTaskQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public TaskStatus getTaskStatusByDocumentIdAndPageNumber(Long documentId, Integer pageNumber) {
        return queryFactory
                .select(stt.sttTask.status)
                .from(stt)
                .where(stt.sttTask.recording.document.id.eq(documentId)
                        .and(stt.pageNumber.eq(pageNumber)))
                .fetchFirst();
    }
} 
