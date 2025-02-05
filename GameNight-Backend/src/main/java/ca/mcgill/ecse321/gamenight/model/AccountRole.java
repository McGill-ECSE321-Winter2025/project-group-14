package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AccountRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
