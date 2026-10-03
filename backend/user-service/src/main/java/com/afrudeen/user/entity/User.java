package com.afrudeen.user.entity;
import com.afrudeen.user.common.BaseEntity;
import com.afrudeen.user.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))

public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash, never plain text

    @Enumerated(EnumType.STRING) // store "ADMIN", not 1
    @Column(nullable = false, length = 20)
    private Role role;

    protected User() { }
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public String getDisplayName() { return email; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}