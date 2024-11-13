package notai.stt.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import notai.stt.domain.Stt;
import static notai.stt.domain.QStt.stt;

import java.util.List;

@RequiredArgsConstructor
public class SttQueryRepositoryImpl implements SttQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Stt> findAllByDocumentIdAndPageNumber(Long documentId, Integer pageNumber) {
        return queryFactory
                .selectFrom(stt)
                .join(stt.recording).fetchJoin()
                .where(stt.recording.document.id.eq(documentId)
                        .and(stt.pageNumber.eq(pageNumber)))
                .fetch();
    }

    @Override
    public List<Stt> findAllByDocumentId(Long documentId) {
        return queryFactory
                .selectFrom(stt)
                .join(stt.recording).fetchJoin()
                .where(stt.recording.document.id.eq(documentId))
                .fetch();
    }
}
