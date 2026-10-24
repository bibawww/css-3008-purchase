package com.css.purchase.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.css.purchase.config.RequestService;
import com.css.purchase.domain.DuplicateRequest;
import com.css.purchase.domain.RequestId;
import com.css.purchase.domain.RequestStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

// Needs PostgreSQL on localhost:5432 (db css, user css, password css).
// No @Transactional here: the count must see what was really committed.
@SpringBootTest(classes = com.css.purchase.config.Application.class)
class RequestPersistenceTest {

  @Autowired RequestService service;
  @Autowired RequestJdbc requests;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void cleanDatabase() throws IOException {
    jdbc.execute("DROP TABLE IF EXISTS purchase_request");
    jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
  }

  @Test
  void secondStatementRollsBack() {
    assertThrows(DuplicateRequest.class, () -> service.insertTwice("PR-19"));
    assertEquals(0, requests.count("PR-19"));
  }

  @Test
  void secondRequestKeepsTheFirst() {
    service.register(RequestId.newId(), "PR-19", "Laptops for the lab");
    assertThrows(DuplicateRequest.class,
        () -> service.register(RequestId.newId(), "PR-19", "Laptops again"));
    assertEquals(1, requests.count("PR-19"));
  }

  @Test
  void statusIsStoredAndReadBack() {
    service.register(RequestId.newId(), "PR-20", "Monitors");
    assertEquals(RequestStatus.DRAFT, requests.findStatus("PR-20").orElseThrow());
  }

  @Test
  void unknownStatusTextIsRejectedByTheCheckConstraint() {
    assertThrows(DataIntegrityViolationException.class,
        () -> requests.insert(UUID.randomUUID(), "PR-21", "SHIPPED", "Chairs"));
    assertEquals(0, requests.count("PR-21"));
  }

  @Test
  void blankBusinessKeyIsRejectedInJava() {
    assertThrows(IllegalArgumentException.class,
        () -> service.register(RequestId.newId(), "  ", "No key"));
  }
}
