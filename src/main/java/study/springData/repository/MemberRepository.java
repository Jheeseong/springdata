package study.springData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.springData.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
