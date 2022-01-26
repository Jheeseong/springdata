package study.springData.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.springData.dto.MemberDto;
import study.springData.entity.Member;
import study.springData.entity.Team;
import study.springData.repository.MemberRepository;
import study.springData.repository.TeamRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    @GetMapping("")
    public String hello() {
        return "hello";
    }

    // 도메인 클래스 컨버터
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getName();
    }

    // 페이징과 정렬
    @GetMapping("/members/page")
    public Page<MemberDto> list(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getName(), null));
        return map;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i <100; i++) {
            memberRepository.save(new Member("user"+i, i));
        }
    }
}
