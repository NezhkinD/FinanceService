package app.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String login;

    @Column(nullable = false, length = 255)
    private String password;

    public static UserEntity create(String login, String password) {
        UserEntity obj = new UserEntity();
        obj.login = login;
        obj.password = password;
        return obj;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
