package study.springData.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@Rollback(value = false)
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

    @Test
    public void paging() throws Exception{

        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));

        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "name"));
        //Page<Member> page = memberRepository.findPagingByAge(10, pageRequest);
        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest);

        List<Member> content = slice.getContent();
        for (Member member : content) {
            System.out.println("member = " + member);
        }
        //assertThat(slice.getTotalElements()).isEqualTo(6);
        //assertThat(slice.getTotalPages()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(1);
        assertThat(slice.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdateQuery() throws Exception {

        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 15));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 24));
        memberRepository.save(new Member("member5", 35));
        memberRepository.save(new Member("member6", 41));

        int ageUpdate = memberRepository.bulkAgeUpdate(20);

        assertThat(ageUpdate).isEqualTo(4);
      }

      @Test
      public void entityGrapeMethod() {
          Team teamA = teamRepository.save(new Team("teamA"));
          Team teamB = teamRepository.save(new Team("teamB"));

          memberRepository.save(new Member("memberA",10,teamA));
        memberRepository.save(new Member("memberB",10,teamA));
        memberRepository.save(new Member("memberC",10,teamB));

        em.flush();
        em.clear();

          List<Member> result = memberRepository.findEntityGraphByName("memberA");

          for (Member member : result) {
              System.out.println("member.getName() = " + member.getName());
              System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
          }
      }

    @Test
    public void JpaHintMethod() {
        Team teamA = teamRepository.save(new Team("teamA"));
        Team teamB = teamRepository.save(new Team("teamB"));

        memberRepository.save(new Member("memberA",10,teamA));
        memberRepository.save(new Member("memberB",10,teamA));
        memberRepository.save(new Member("memberC",10,teamB));

        em.flush();
        em.clear();

        //Member memberA = memberRepository.findJpaHintByName("memberA");
        Member memberA = memberRepository.findLockByName("memberA");
        memberA.setName("memberB");
    }

    @Test
    public void CustomInterface() {
        Team teamA = teamRepository.save(new Team("teamA"));
        Team teamB = teamRepository.save(new Team("teamB"));

        memberRepository.save(new Member("memberA",10,teamA));
        memberRepository.save(new Member("memberB",10,teamA));
        memberRepository.save(new Member("memberC",10,teamB));

        em.flush();
        em.clear();

        List<Member> result = memberRepository.findMemberCustom();
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void BaseEntityTime() throws Exception{

        Member member = new Member("member1");
        memberRepository.save(member);

        Thread.sleep(100);
        member.setName("member2");

        em.flush();
        em.clear();

        Member result = memberRepository.findById(member.getId()).get();

        System.out.println("result.getCreateDate() = " + result.getCreateDate());
        System.out.println("result.getLastModifiedDate() = " + result.getLastModifiedDate());
        System.out.println("result.getCreateBy() = " + result.getCreateBy());
        System.out.println("result.getLastModifiedBy() = " + result.getLastModifiedBy());



    }
}