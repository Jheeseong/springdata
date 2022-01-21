package study.springData.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.springData.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamJpaRepository {

    private final EntityManager em;

    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    public void delete(Team team) {
        em.remove(team);
    }

    public Optional<Team> find(Long id) {
        Team team = em.find(Team.class, id);
        return Optional.ofNullable(team);
    }

    public List<Team> findAll() {
        return em.createQuery("select t from Team t",Team.class)
                .getResultList();
    }

    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
    }
}
