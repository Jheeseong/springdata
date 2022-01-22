package study.springData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.springData.dto.MemberDto;
import study.springData.entity.Member;
import study.springData.entity.Team;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //NamedQuery 사용
    @Query(name = "Member.findByName")
    List<Member> findByName(@Param("name") String name);

    //메소드에 JPQL 쿼리 작성, 파라미터 바인딩
    @Query("select m from Member m join fetch m.team t where m.team.name = :name ")
    List<Member> findTeam(@Param("name") String team);

    //값 하나를 조회
    @Query("select m.name from Member m")
    List<String> findUserNameList();

    //DTO 직접 조회
    @Query("select new study.springData.dto.MemberDto(m.id, m.name, t.name)" +
            " from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //컬렉션 파라미터 바인딩
    @Query("select m from Member m where m.name in :names")
    List<Member> findByNames(@Param("names") List<String> names);
}
