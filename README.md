# springData
# v1.5 1/26
# 스프링 데이터 JPA 분석
## 스프링 데이터 JPA 구현체 분석
- JPA 구현체는 기본적으로 @Repository를 적용 : JPA 예외를 스프링이 추상화한 예외로 변환
- @Transactional 트랜잭션 적용
  - JPA의 모든 변경은 트랜잭션 안에서 동작
  - JPA 의 기본은 **readOnly = true**를 통해 조회만 가능하지만 등록,수정, 삭제 메소드 실행 시 트랜잭션 **readOnly = false** 가 동작
  - 서비스 계층에서 트랙잭션 실행하지 않을 시 리파지토리에서 트랙잭션이 시작, 서비스 계층에서 트랜잭션 실행 시 리파지토리는 해당 트랜잭션을 전파 받아 사용
  - 스프링 데이터 JPA 사용 시 트랜잭션 없이도 데이터 등록,변경이 가능했던 이유

## 새로운 엔티티 구별 방법
- save 메서드
  - 새로운 엔티티면 저장(persist)
  - 새로운 엔티티가 아니면 병합(merge)

- 새로운 엔티티 판단 기본 전략
  - 식별자가 객체일 때 **null**로 반환
  - 식별자가 자바 기본 타입일 때 0으로 판단  
  -> persistable 인터페이스를 구현하여 판단 로직 변경 가능
 
 
        @Entity
        @Getter
        @EntityListeners(AuditingEntityListener.class)
        public class Item implements Persistable<String> {

           @Id
           private String id;

           @CreatedDate
           private LocalDateTime createDate;

           public Item(String id) {
               this.id = id;
           }

           @Override
           public boolean isNew() {
               return createDate == null;
           }
        }
        
- @GenerateValue가 있을 땐 save() 호출 시점에 식별자가 없으므로 새로운 엔티티로 인식해서 정삭 동작.
- 하지만 @Id만 사용해 직접 할당하게 된다면 이미 값이 있는 상태로 save를 호출.
- 따라서 merge가 호출되어 동작
- Persistable를 사용해서 새로운 엔티티 확인 여부를 직접 구현
- 여기 코드에서는 @CreateDate를 조합하여 새로운 엔티티 여부를 판단하도록 설계

# v1.4 1/25
# SpringData 확장 기능
## WEB - 도메인 클래스 컨버터
    
    // 도메인 클래스 컨버터
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getName();
    }
    
- HTTP 파라미터로 넘어온 엔티티 id를 도베인 클래스 컨버터가 동작해서 회원 엔티티 객체 반환
- 도메인 클래스 컨버터도 리파지토리를 사용하여 엔티티 발견
- 도메인 클래스 컨버터로 엔티티 받을 시 단순 조회용으로만 사용 가능

## WEB - 페이징과 정렬

    // 페이징과 정렬
    @GetMapping("/members/page")
    public Page<Member> list(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }
    
- 파라미터로 Pageable을 받을 수 잇다
- 요청 파라미터 설정
  - ex) /members?page=0&size=3&sort=id,desc&sort=username,desc
  - page : 현재 페이지, 0부터 시작(기본 페이지 사이즈 = 20)
  - size : 한 페이지에 노출할 데이터 건수
  - sort : 정렬 조건을 정의
- @PageableDefault 어노테이션을 사용하여서도 page, size, sort 설정 가능
- 접두사 : 페이지 정보가 둘 이상일 경우 접두사로 구분, @Qualifier에 접두사명 추가

### DTO 페이징

    // 페이징과 정렬
    @GetMapping("/members/page")
    public Page<MemberDto> list(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getName(), null));
        return map;
    }
    
### 페이징 1부터 시작
- 스프링 데이터는 page를 0부터 시작
- 해결 방법
  - Pageable,Page를 파라미터와 응답 값으로 사용하지 않고, 직접 클래스를 만들어서 처리. 그 후 직접 PageRequest(Pageable 구현체)를 생성해서 리포지토리로 전송. 응답 값도 Page 대신 직접 만들어 제공
  - spring.data.web.pageable.one-indexed-parameters 를 True로 설정. 하지만 이 방법은 web에서 page 파라미터를 -1로 처리 할 뿐 응답 값 page를 변경시키지는 않는 한계가 존재

# v1.3 1/24
# SpringData 확장 기능
## 사용자 정의 리포지토리
**MemberRepositoryCustomImpl Class 생성**
    
    @RequiredArgsConstructor
    public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
        }
    }
    
**MemberRepositoryCustom Interface 생성**

    public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
    }
    
**MemberRepository에 CustomInterface 상속**

    public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    
    ....
    
    //CustomImpl
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByName(String name);
    
    ....
    
    }

- 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성
- 그러나 스프링 데이터가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많음
- 인터페이스의 메서드를 Impl 상속을 통해 구현 가능
 
## Auditing
- 엔티티를 생성, 변경할 때 등의 경우에 등록일, 수정일, 등록자, 수정자 추적 가능
- 데이터가 중복 저장되는 것처럼 보이지만, 이렇게 설계 시 변경 컬럼만 확인해도 마지막 업데이트 유저를 알 수 있어 유지관리가 편리

**BaseEntity 생성**
    
    @EntityListeners(AuditingEntityListener.class)
    @MappedSuperclass
    @Getter
    public class BaseEntity {

        @CreatedDate
        @Column(updatable = false)
        private LocalDateTime createDate;

        @LastModifiedDate
        private LocalDateTime lastModifiedDate;

        @CreatedBy
        @Column(updatable = false)
        private String createBy;

        @LastModifiedBy
        private String lastModifiedBy;
    }
    
**Member, Team 엔티티에 상속**

    public class Member extends BaseEntity {
    ....
    }
    
    public class Team extends BaseEntity {
    ....
    }
    
**Test code**

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
# v1.2 1/23
## 쿼리 메소드 기능
### JPA 페이징과 정렬
**리포지토리**

    //JPA 페이징과 정렬//
    @Query(value = "select m from Member m",
            countQuery = "select count(m.name) from Member m")
    Page<Member> findPagingByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);
    List<Member> findListByAge(int age, Pageable pageable);
    List<Member> findSortByAge(int age, Sort sort);

    
**테스트코드**    

    @Test
    public void paging() throws Exception{

        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));

        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "name"));
        Page<Member> page = memberRepository.findPagingByAge(10, pageRequest);
        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest);

        List<Member> content = slice.getContent();
        for (Member member : content) {
            System.out.println("member = " + member);
        }
        assertThat(slice.getTotalElements()).isEqualTo(6);
        assertThat(slice.getTotalPages()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(1);
        assertThat(slice.hasNext()).isTrue();

    }


- Page : count 쿼리 사용, CountQuery를 통해 분리 가능(count 쿼리는 무겁기 떄문, 분리하지 않을 시 join한 데이터 모두 count 쿼리를 사용)
- slice : count 쿼리 사용 X, 추가로 limit +1을 조회하여 다음 페이지 여부를 확인(최근 모바일 리스트)
- List : count 쿼리 사용 X

#### 페이지를 유지하며 엔티티를 DTO로 변환

    Page<Member> page = memberRepository.findByAge(10, pageRequest);
    Page<MemberDto> dtoPage = page.map(m -> new MemberDto());

### 벌크성 수정 쿼리
**리포지토리**

    @Modifying
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
    
**테스트코드**

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

- 벌크성 수정 쿼리는 기존의 더티체킹을 통한 수정이 아닌 업데이트 쿼리를 통해 해당 조건의 데이터를 수정하는 방법
- 벌크성 수정 쿼리는 @Modifying 어노테이션을 필수적으로 사용
- 벌스성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화가 필수, 하지 않을 경우 영속성 컨텍스트에 과거 값이 남아있어 문제 발성 가능성이 높음
- Modifing 영속성 컨텍스트 초기화 : @Modifying(clearAutomatically = true)

### @EntityGraph

    //EntityGraph
    @EntityGraph(attributePaths = "team")
    List<Member> findEntityGraphByName(@Param("name") String name);

- 연관된 엔티티들을 SQL 한번에 조회하는 방법
- **지연로딩으로 인해 데이터 조회 시 쿼리가 1+N 문제**가 발생
- join fetch 혹은 @EntityGraph를 사용하여 해결

### JPA Hint & Lock
#### JPA Hint
    
    //JPA Hint & Lock
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findJpaHintByName(String name);

- JPA 쿼리 힌트(SQL 힌트가 아닌 JPA구현체에게 제공하는 힌트)
- update Query 실행 X

#### Lock

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByName(String name);

- 쿼리 마지막에 update가 붙음  
![image](https://user-images.githubusercontent.com/96407257/150670306-9d158565-938c-421d-80fa-a60e390922e8.png)

# v1.1 1/22
## 쿼리 메소드 기능
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

    
