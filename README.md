# springdata
# v1.1 1/22
## 쿼리 베소드 기능
- 메소드 이름으로 쿼리 생성
- 메소드 이름으로 JPA NamedQuery 호출
- @Query 어노테이션을 사용해서 리파지토리 인터페이스에 직접 쿼리

### 메소드 이름으로 쿼리 생성

    public interface MemberRepository extends JpaRepository<Member, Long> { 
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    }

- 조회 : find...By, read...By, query...by, get...By (exfindHelloByName 처럼...에 식별을 위한 내용 추가 가능)
- COUNT : count...By 반환타입 long
- EXISTS : exists...By 반환타입 boolean
- DELETE : delete...By, remove...By 반환타입 long
- DISTINCT : findDistinct, find MemberDistinctBy
- LIMIT : findFirst3, findFirst, findTop, findTop3
등등..
- 엔티티 필드명 변경 시 인터페이스에 정의한 메서드 이름 변경 필수, 그렇지 않으면 애플리케이션 시작 시점에 오류 발생 -> 애플리케이션 로딩 시점에 오류 인지할 수 있는 것이 장점

### JPA NamedQuery

- 엔티티

      @Entity
      @NamedQuery(name = "Member.findByName",
                  query = "select m from Member m where m.name = :name")
      public class Member {
      ....
      }
    
- 리포지토리
      
      public interface MemberRepository extends JpaRepository<Member, Long> {

      //NamedQuery 사용
      @Query(name = "Member.findByName")
      List<Member> findByName(@Param("name") String name);
      }
      
- 스프링 데이터 JPA는 선언한 "도메인 클래스 + .(점) + 메서드 이름"으로 Named 쿼리를 찾아서 실행
- 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략 사용(Named 쿼리가 우선권을 가짐)

### @Query 어노테이션을 사용해서 리파지토리 인터페이스에 직접 쿼리
- 메서드에 JPQL 쿼리 작성(파라미터 바인딩 포함)

      //메소드에 JPQL 쿼리 작성, 파라미터 바인딩
      @Query("select m from Member m join fetch m.team t where m.team.name = :name ")
      List<Member> findTeam(@Param("name") String team);

- 실행할 메서드에 정적 쿼리를 직접 작성하는 것으로 이름없는 Named 쿼리와 비슷
- JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류 발견 가능
- 코드 가독성과 유지보수를 위해 이름 기반 파라미터 바인딩 사용이 필수!!

### @Query 값, DTO 조회하기
- 단순히 값 하나를 조회

      //값 하나를 조회
      @Query("select m.name from Member m")
      List<String> findUserNameList();

- DTO 직접 조회 : DTO 직접 조회 시 JPA의 new 명령어 사용, 생성자가 맞는 DTO가 필요

      //DTO
      @Data
      public class MemberDto {

      private Long id;
      private String name;
      private String teamName;

      public MemberDto(Long id, String name, String teamName) {
          this.id = id;
          this.name = name;
          this.teamName = teamName;
          }
      }
      
      //리포지토리 - DTO 직접 조회
      @Query("select new study.springData.dto.MemberDto(m.id, m.name, t.name)" +
              " from Member m join m.team t")
      List<MemberDto> findMemberDto();

### 컬렉션 파라미터 바인딩
- Collection 타입으로 in절 지원

      //컬렉션 파라미터 바인딩
      @Query("select m from Member m where m.name in :names")
      List<Member> findByNames(@Param("names") List<String> names);
      }
      
### 반환 타입
- 스프링 데이터 JPA는 유연한 반환 타입 지원
- 컬렉션
  - 결과 없음 : 빈 컬렉션 반환
- 단건 조회
  - 결과 없음 : null 반환
  - 결과 2건 이상 : javax.persistence.NonUniqueResultException 예외 발생

# v1.0 1/21
## 공통 인터페이스 (JPA REPOSITORY)
- 스프링 데이터 JPA 기반 Repository  
- 순수 JPA로 구현한 MemberJpaRepository 대신 **스프링 데이터 JPA가 제공하는 공통 인터페이스** 사용

      public interface MemberRepository extends JpaRepository<Member, Long> {
      }
    
- 기존 순수 JPA 기반 테스트에서 사용한 코드를 그대로 사용 가능
- generic<T,ID>
  - T : 엔티티 타입
  - ID : 식별자 타입(PK)

### 공통 인터페이스 구성
![image](https://user-images.githubusercontent.com/96407257/150502994-74e03d08-d1e2-425d-a85c-178e18b89964.png)

- T findOne(ID) -> Optional<T> findById(ID)로 Optional이 붙은 상태로 변경

### 주요 메서드
- save(s) : 새로운 엔티티 저장, 이미 있는 엔티티는 병합(Merge)
- delete(T) : 엔티티 하나 삭제, 내부에서 EntityManager.remove() 호출
- find(명칭)By(ID,Name...등) : 엔티티 하나 조회, 내부에서 EntityManager.find() 호출
- getOne(Id) : 엔티티를 프록시로 조회, 내부에서 EntityManager.getReference() 호출
- findAll(~) : 모든 엔티티 조회. 정렬(sort)나 페이징(pageable)조건을 파라미터로 제공

    
