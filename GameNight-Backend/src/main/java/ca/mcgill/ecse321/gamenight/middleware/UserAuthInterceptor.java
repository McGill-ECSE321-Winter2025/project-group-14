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
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private final PersonRepository personRepository;

    private final UserContext userContext;

    @Autowired
    public UserAuthInterceptor(
            PersonRepository personRepository,
            UserContext userContext) {
        this.personRepository = personRepository;
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

        // Access is granted as long as the user-id is in the request header (less secure)
        if (requireUser != null) {
            String userIdHeader = request.getHeader("User-Id");

            if (userIdHeader == null) {
                throw new UnauthorizedException("No User-Id header provided");
            }

            try {
                int userId = Integer.parseInt(userIdHeader);

                Person user = personRepository
                        .findById(userId)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));

                userContext.setCurrentUser(user);
            } catch (IllegalArgumentException e) {
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
