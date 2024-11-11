package notai.member.presentation.response;

import notai.member.domain.Member;

public record MemberFindResponse(
        Long id,
        String nickname
) {
    public static MemberFindResponse from(Member member) {
        return new MemberFindResponse(member.getId(), member.getNickname());
    }
}
