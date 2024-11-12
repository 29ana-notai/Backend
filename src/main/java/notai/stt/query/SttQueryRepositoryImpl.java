package notai.stt.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import static notai.stt.domain.QStt.stt;
import notai.stt.domain.Stt;

import java.util.List;

@RequiredArgsConstructor
public class SttQueryRepositoryImpl implements SttQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<Stt> findAllByDocumentIdAndPageNumber(Long documentId, Integer pageNumber) {
        return queryFactory.selectFrom(stt)
                           .where(stt.recording.document.id.eq(documentId).and(stt.pageNumber.eq(pageNumber)))
                           .fetch();
    }
}
