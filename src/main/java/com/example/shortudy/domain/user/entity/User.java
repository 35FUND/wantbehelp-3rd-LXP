package com.example.shortudy.domain.user.entity;

import com.example.shortudy.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * - email: 로그인용 이메일 (유니크)
 * - password: 암호화된 비밀번호
 * - nickname: 닉네임 (nullable)
 * - name: 사용자 이름
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String nickname;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private final List<String> roles = new ArrayList<>();

    // == 생성자 ==
    protected User() {
    }

    private User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.roles.add("ROLE_USER");
    }

    // == 정적 팩토리 메서드 ==
    public static User createUser(String email, String password, String name) {
        return new User(email, password, name);
    }

    // == Getter ==
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getRoles() {
        return roles;
    }
}

