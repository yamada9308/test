package hello;

import javax.servlet.MultipartConfigElement;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Bean
    DataSource dataSource() {
        return new SimpleDriverDataSource() {{
            setDriverClass(org.h2.Driver.class);
            setUsername("sa");
            setUrl("jdbc:h2:mem");
            setPassword("");
        }};
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}