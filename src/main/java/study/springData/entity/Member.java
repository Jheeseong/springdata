package study.springData.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NamedQuery(name = "Member.findByName",
            query = "select m from Member m where m.name = :name")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","name","age"})
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String name) {
        this.name = name;
    }

    public Member(String name, int age, Team team) {
        this.name = name;
        this.age = age;
        if(team != null) {
            ChangeTeam(team);
        }
    }

    private void ChangeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
