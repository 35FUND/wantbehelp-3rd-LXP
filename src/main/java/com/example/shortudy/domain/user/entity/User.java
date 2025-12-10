package com.example.shortudy.domain.user.entity;

import com.example.shortudy.global.common.BaseEntity;
import jakarta.persistence.*;

/**
 * 사용자 엔티티
 * - email: 로그인용 이메일 (유니크)
 * - password: 암호화된 비밀번호
 * - nickname: 닉네임 (nullable)
 * - active: 계정 활성화 상태
 */
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    protected User() {
    }

    protected User(String email, String password, String name, String nickname) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.roles.add("ROLE_USER");
    }

    public static User createUser(String email, String password, String name, String nickname) {
        return new User(email, password, name, nickname);
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}



