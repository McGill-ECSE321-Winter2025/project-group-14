package ca.mcgill.ecse321.gamenight.config;

import ca.mcgill.ecse321.gamenight.security.FirebaseAuthFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FirebaseAuthFilter()).addPathPatterns("/**") // Secures all routes
                .excludePathPatterns("/auth/login", "/auth/register"); // except these two
    }
}
