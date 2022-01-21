package study.springData.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.springData.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;

    public Member save(Member member){
        em.persist(member);
        return member;
    }

    public Optional<Member> find(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m join fetch m.team t",Member.class)
                .getResultList();
    }
}
