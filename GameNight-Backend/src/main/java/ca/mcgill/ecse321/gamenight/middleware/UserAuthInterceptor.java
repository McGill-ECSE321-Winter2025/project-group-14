package ca.mcgill.ecse321.gamenight.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import ca.mcgill.ecse321.gamenight.exception.UnauthorizedException;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private final PersonRepository personRepository;
    private final PlayerRepository playerRepository;
    private final UserContext userContext;

    @Autowired
    public UserAuthInterceptor(
            PersonRepository personRepository,
            PlayerRepository playerRepository,
            UserContext userContext) {
        this.personRepository = personRepository;
        this.playerRepository = playerRepository;
        this.userContext = userContext;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws UnauthorizedException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        RequireUser requireUser = handlerMethod.getMethodAnnotation(
                RequireUser.class);

        if (requireUser == null) {
            requireUser = handlerMethod
                    .getBeanType()
                    .getAnnotation(RequireUser.class);
        }

        // Log the received User-Id header
        String userIdHeader = request.getHeader("User-Id");
        System.out.println("Received User-Id from header: " + userIdHeader);

        if (requireUser != null) {
            if (userIdHeader == null) {
                throw new UnauthorizedException("No User-Id header provided");
            }

            try {
                int playerId = Integer.parseInt(userIdHeader);

                // First, check if the player exists
                Player player = playerRepository.findById(playerId)
                        .orElseThrow(() -> {
                            System.out.println("Player not found for ID: " + playerId);
                            return new UnauthorizedException("Player not found");
                        });

                // Get the associated person
                Person person = player.getPerson();
                if (person == null) {
                    System.out.println("No person associated with player ID: " + playerId);
                    throw new UnauthorizedException("No person associated with this player");
                }

                System.out.println("Authenticated player: " + player.getId() + " - " + person.getEmailAddress());
                userContext.setCurrentUser(person);

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid User-Id format: " + userIdHeader);
                throw new UnauthorizedException("Invalid User-Id format");
            }
        }
        return true;
    }




    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) {
        userContext.clear();
    }

}
