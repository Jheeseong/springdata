# springdata
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

    
