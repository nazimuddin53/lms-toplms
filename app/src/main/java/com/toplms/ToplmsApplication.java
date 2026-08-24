package com.toplms;

import com.toplms.config.AppHostProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@code @ConfigurationPropertiesScan} tells Boot to look for
 * {@code @ConfigurationProperties} classes in this package (and below) and
 * register each as a bean — the alternative to listing every one explicitly
 * via {@code @EnableConfigurationProperties}. This is what makes
 * {@link com.toplms.config.AppHostProperties} available for injection.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ToplmsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ToplmsApplication.class, args);

        // The bean is now fully built and bound — pull it out of the context.
        // This is the simplest way to touch a bean from main(). For anything
        // beyond a one-line debug print, prefer a CommandLineRunner @Bean,
        // which gets the dependency injected the normal way.
        AppHostProperties props = ctx.getBean(AppHostProperties.class);
        System.out.println("Host: " + props.getHost());
        System.out.println("Port: " + props.getPort());
    }
}
