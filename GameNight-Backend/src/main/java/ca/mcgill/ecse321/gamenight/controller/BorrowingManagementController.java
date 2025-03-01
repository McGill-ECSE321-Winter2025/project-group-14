package ca.mcgill.ecse321.gamenight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
public class BorrowingManagementController {
    @Autowired
    BorrowingManagementService borrowingManagementService;
    
}
