package ca.mcgill.ecse321.gamenight.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "email_address", unique = true, nullable = false)
    private String emailAddress;
    private String password;
    private String name;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private Set<AccountRole> roles;

    public Set<AccountRole> getRoles() {
        return roles;

    }

    public void setRoles(Set<AccountRole> roles) {
        this.roles = roles;
    }

    public Person() {

    }

    public int getId() {
        return id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
