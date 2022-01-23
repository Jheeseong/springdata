package study.springData.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.springData.entity.Member;
import study.springData.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;
    @Autowired TeamJpaRepository teamJpaRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");

        Member saveMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(saveMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getName()).isEqualTo(member.getName());

        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void testTeam() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamJpaRepository.save(team1);
        teamJpaRepository.save(team2);

        Member member1 = new Member("memberA",10,team1);
        Member member2 = new Member("memberB",20,team2);
        Member member3 = new Member("memberC",30,team1);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);

        em.flush();
        em.clear();

        List<Member> all = memberJpaRepository.findAll();
        for (Member member : all) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }
    }

    @Test
    public void nameQuery() {
        Member member1 = new Member("memberA");
        Member member2 = new Member("memberB");
        Member member3 = new Member("memberC");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);

        List<Member> memberName = memberJpaRepository.findByName(member1.getName());

        System.out.println("memberName = " + memberName);
    }

    @Test
    public void paging() throws Exception{
        
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",10));
        memberJpaRepository.save(new Member("member3",10));
        memberJpaRepository.save(new Member("member4",10));
        memberJpaRepository.save(new Member("member5",10));
        memberJpaRepository.save(new Member("member6",10));

        List<Member> members = memberJpaRepository.findByPage(10, 0, 2);
        long totalCount = memberJpaRepository.totalCount(10);
        
        assertThat(members.size()).isEqualTo(2);
        assertThat(totalCount).isEqualTo(6);
        System.out.println("members = " + members);
    }

    @Test
    public void bulkUpdate() {
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",20));
        memberJpaRepository.save(new Member("member3",21));
        memberJpaRepository.save(new Member("member4",30));
        memberJpaRepository.save(new Member("member5",10));
        memberJpaRepository.save(new Member("member6",41));

        int ageUpdate = memberJpaRepository.bulkAgeUpdate(20);

        assertThat(ageUpdate).isEqualTo(4);
    }


}