package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.Person;

public class PersonResponseDto {
    private int personId;
    private String name;
    private String email;

    public PersonResponseDto(Person person) {
        this.personId = person.getId();
        this.name = person.getName();
        this.email = person.getEmailAddress();
    }
    public PersonResponseDto() {
        // Default constructor for Jackson
    }
    
    public int getPersonId() {
        return personId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
