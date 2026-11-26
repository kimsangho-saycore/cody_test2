package kr.innoaca.usr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"kr.innoaca.core", "kr.innoaca.usr"})
@MapperScan({"kr.innoaca.core", "kr.innoaca.usr"})
@EnableCaching
@EnableAsync
@EnableScheduling
public class InnoUsrApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(InnoUsrApplication.class);
    }

    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application,application-usr");
        SpringApplication.run(InnoUsrApplication.class, args);
    }

}
