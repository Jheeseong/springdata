package study.springData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.springData.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
