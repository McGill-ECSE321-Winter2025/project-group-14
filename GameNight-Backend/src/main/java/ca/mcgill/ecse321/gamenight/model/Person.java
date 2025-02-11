package ca.mcgill.ecse321.gamenight.model;

import java.util.Set;

import jakarta.persistence.Entity;

@Entity
public class Person {
    
    private String emailAddress;
    private String password;
    private String name;

   
    private Set<AccountRole> roles;

    public Set<AccountRole> getRoles(){
        return roles;
        
    }

    public void setRoles(Set<AccountRole> roles){
        this.roles = roles;
    }

    public Person() {
        
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
