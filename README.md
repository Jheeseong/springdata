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
