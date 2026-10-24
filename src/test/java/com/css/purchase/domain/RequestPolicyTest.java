package com.css.purchase.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RequestPolicyTest {

  private final RequestPolicy policy =
      new RequestPolicy(new UnapprovedCannotOrder(new TransitionRule()));

  // The same four rows as in README "Status table".
  @ParameterizedTest(name = "{0} -> {1} = {2}")
  @CsvSource({
      "DRAFT,    APPROVED, APPROVED",
      "APPROVED, ORDERED,  ORDERED",
      "DRAFT,    ORDERED,  FORBIDDEN",
      "ORDERED,  DRAFT,    FORBIDDEN"
  })
  void statusTable(RequestStatus from, RequestStatus to, String expected) {
    if (expected.equals("FORBIDDEN")) {
      assertThrows(IllegalStateException.class, () -> policy.move(from, to));
    } else {
      assertEquals(RequestStatus.valueOf(expected), policy.move(from, to));
    }
  }
}
