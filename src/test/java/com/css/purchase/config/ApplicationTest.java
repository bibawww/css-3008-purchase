package com.css.purchase.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.css.purchase.domain.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTest {

  @Autowired
  RequestService service;

  @Test
  void springInjectsRuleIntoService() {
    assertEquals(RequestStatus.APPROVED,
        service.move("PR-1", RequestStatus.DRAFT, RequestStatus.APPROVED));
    assertThrows(IllegalStateException.class,
        () -> service.move("PR-1", RequestStatus.DRAFT, RequestStatus.ORDERED));
  }
}
