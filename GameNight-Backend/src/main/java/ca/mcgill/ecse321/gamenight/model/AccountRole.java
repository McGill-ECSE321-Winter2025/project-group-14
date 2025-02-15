package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AccountRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    public AccountRole() {

    }

    public AccountRole(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public int getId() {
        return id;
    }

}
