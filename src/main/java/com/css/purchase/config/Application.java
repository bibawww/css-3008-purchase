package com.css.purchase.config;

import com.css.purchase.domain.RequestStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.css.purchase")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  CommandLineRunner demo(RequestService service) {
    return args -> {
      RequestStatus now = service.move("PR-1", RequestStatus.DRAFT, RequestStatus.APPROVED);
      System.out.println("PR-1: DRAFT -> " + now);
    };
  }
}
