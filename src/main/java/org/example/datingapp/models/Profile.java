package org.example.datingapp.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String openInformation;
    private String closedInformation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String name) {
        this.email = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String name) {
        this.password = name;
    }

    public String getOpenInformation() {
        return openInformation;
    }

    public void setOpenInformation(String openInformation) {
        this.openInformation = openInformation;
    }

    public String getClosedInformation() {
        return closedInformation;
    }

    public void setClosedInformation(String closedInformation) {
        this.closedInformation = closedInformation;
    }
}
