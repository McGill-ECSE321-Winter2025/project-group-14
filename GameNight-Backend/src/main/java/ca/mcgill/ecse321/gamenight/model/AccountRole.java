package ca.mcgill.ecse321.gamenight.model;


import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AccountRole {

    private int id;

    public Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public AccountRole(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
