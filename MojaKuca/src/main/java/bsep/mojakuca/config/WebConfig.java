package bsep.mojakuca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    // Za svrhe razvoja konfigurisemo dozvolu za CORS kako ne bismo morali @CrossOrigin anotaciju da koristimo nad svakim kontrolerom
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4201", "http://localhost:8082")
                .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH")
                .allowedHeaders("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization", "Cookie", "Set-Cookie")
                //.exposedHeaders("Set-Cookie")
                .allowCredentials(true);
    }
}
