package ca.mcgill.ecse321.gamenight.middleware;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import ca.mcgill.ecse321.gamenight.model.Person;

@Component
@RequestScope
@Getter
@Setter
public class UserContext {

    private Person currentUser;

    public void clear() {
        this.currentUser = null;
    }
}
