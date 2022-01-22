package study.springData.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.springData.dto.MemberDto;
import study.springData.entity.Member;
import study.springData.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void jpaMemberTest() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamRepository.save(team1);
        teamRepository.save(team2);

        Member member1 = new Member("memberA",10,team1);
        Member member2 = new Member("memberB",20,team2);
        Member member3 = new Member("memberC",30,team1);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        for (Member member : all) {
            System.out.println("member.getTeam() = " + member.getTeam());
        }
        assertThat(all.size()).isEqualTo(3);

        long count = memberRepository.count();
        System.out.println("count = " + count);
    }

    @Test
    public void nameQuery() {
        Member member1 = new Member("memberA");
        Member member2 = new Member("memberB");
        Member member3 = new Member("memberC");

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findByName("memberA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
        System.out.println("memberA = " + result);
    }

    @Test
    public void findMember() {

        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamRepository.save(team1);
        teamRepository.save(team2);

        Member member1 = new Member("memberA",10,team1);
        Member member2 = new Member("memberB",20,team2);
        Member member3 = new Member("memberC",30,team1);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findTeam("teamA");
        assertThat(result).isEqualTo(Arrays.asList(member1,member3));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void findDto() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamRepository.save(team1);
        teamRepository.save(team2);

        Member member1 = new Member("memberA",10,team1);
        Member member2 = new Member("memberB",20,team2);
        Member member3 = new Member("memberC",30,team1);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void collectionParam() {
        Member member1 = new Member("memberA");
        Member member2 = new Member("memberA");
        Member member3 = new Member("memberB");

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findByNames(Arrays.asList("memberA","memberB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
}