package com.library.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * CO1: Entity — 3NF-compliant User (no transitive dependencies).
 * CO4: Spring Boot JPA entity with validation annotations.
 * Maps to PostgreSQL `users` table.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_status", columnList = "status")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be 3–100 characters")
    private String username;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Full name is required")
    private String fullname;

    @Column(unique = true, length = 255)
    @Email(message = "Must be a valid email address")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    @Pattern(regexp = "admin|librarian|teacher|student", message = "Role must be admin, librarian, teacher, or student")
    private String role = "student";

    @Column(nullable = false, length = 50)
    private String status = "active";

    @Column(name = "joined_date")
    private String joinedDate;

    @Column(nullable = false)
    @Min(value = 0, message = "Loan count cannot be negative")
    private int loans = 0;

    // ── Bidirectional relationship (CO1: FK / relational link) ─────────────
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties("user")
    private List<Loan> loanHistory;

    public User() {
    }

    public User(Long id, String username, String fullname, String email, String password,
            String role, String status, String joinedDate, int loans) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.joinedDate = joinedDate;
        this.loans = loans;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }

    public int getLoans() {
        return loans;
    }

    public void setLoans(int loans) {
        this.loans = loans;
    }

    public List<Loan> getLoanHistory() {
        return loanHistory;
    }

    public void setLoanHistory(List<Loan> h) {
        this.loanHistory = h;
    }

    // ── UserDetails interface (CO3: Security / Auth) ───────────────────────
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !"suspended".equals(status);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean isEnabled() {
        return "active".equals(status);
    }
}
