package com.example.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A registered user. The username is the user's email address.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    /** Creates an empty user (required by JPA). */
    public User() {
    }

    /**
     * Creates a user.
     *
     * @param username    email address used to log in
     * @param password    BCrypt-hashed password
     * @param fullName    full name
     * @param phoneNumber phone number
     */
    public User(String username, String password, String fullName, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    /** @return the id */
    public Long getId() {
        return id;
    }

    /** @param id the id */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the username (email) */
    public String getUsername() {
        return username;
    }

    /** @return the hashed password */
    public String getPassword() {
        return password;
    }

    /** @return the full name */
    public String getFullName() {
        return fullName;
    }

    /** @return the phone number */
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
