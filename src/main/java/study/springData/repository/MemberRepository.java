package study.springData.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.springData.dto.MemberDto;
import study.springData.entity.Member;
import study.springData.entity.Team;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

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

    //JPA 페이징과 정렬
    @Query(value = "select m from Member m",
            countQuery = "select count(m.name) from Member m")
    Page<Member> findPagingByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);
    List<Member> findListByAge(int age, Pageable pageable);
    List<Member> findSortByAge(int age, Sort sort);

    //벌크성 업데이터 쿼리
    @Modifying
    @Query("update Member m set m.age = m.age + 1" +
            " where m.age >= :age")
    int bulkAgeUpdate(@Param("age") int age);

    //EntityGraph
    @EntityGraph(attributePaths = "team")
    List<Member> findEntityGraphByName(@Param("name") String name);

    //JPA Hint & Lock
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findJpaHintByName(String name);

    //CustomImpl
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByName(String name);

}
