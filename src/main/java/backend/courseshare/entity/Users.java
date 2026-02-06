package backend.courseshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.Year;
import java.util.Random;

@Entity
@Table(name="Users", indexes = {
        @Index(name="idx_users_public_id" , columnList = "public_id")
})
@Data
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false, length = 32)
    private String publicId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="passwordHash",nullable = false)
    private String passwordHash;

    @Column(name="created_At",nullable = false,updatable = false)
    private Instant created_At;

    @Enumerated(EnumType.STRING)
    private Role role=Role.USER;


    @PrePersist
    protected void onCreate(){
        if(created_At == null) {
            created_At=Instant.now();
        }
    }
    //Constructors



    public Users(Long id, String publicId,
                 String username, String email,
                 String passwordHash, Instant created_At,
                 Role role) {
        this.id = id;
        this.publicId = publicId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.created_At = created_At;
        this.role = role;
    }
//Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreated_At() {
        return created_At;
    }

    public void setCreated_At(Instant created_At) {
        this.created_At = created_At;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}