package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy =InheritanceType.TABLE_PER_CLASS)
public abstract class AccountRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "person_email", referencedColumnName = "email_address")
    private Person person;

    public Person getPerson() {
        return person;
    }

    public AccountRole(){

    }

    public int getId() {
        return id;
    }

}

