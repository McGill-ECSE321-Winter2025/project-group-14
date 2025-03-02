package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.*;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "email_address", unique = true, nullable = false)
    private String emailAddress;

    private String password;
    private String name;

    @Column(unique = true, nullable = true) // Firebase UID
    private String firebaseUid;

    public Person() {
    }

    public Person(String emailAddress, String password, String name) {
        this.emailAddress = emailAddress;
        this.name = name;
        this.password = password;
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

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}
