package com.css.purchase.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RulesTest {

  private final Rule stopFactor = new UnapprovedCannotOrder(new TransitionRule());

  @Test
  void draftCannotBeOrdered() {
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> stopFactor.check(RequestStatus.DRAFT, RequestStatus.ORDERED));
    assertTrue(ex.getMessage().contains("skipped approval"));
  }

  @Test
  void approvedCanBeOrdered() {
    assertDoesNotThrow(() -> stopFactor.check(RequestStatus.APPROVED, RequestStatus.ORDERED));
  }

  @Test
  void transitionRuleRejectsGoingBack() {
    assertThrows(IllegalStateException.class,
        () -> new TransitionRule().check(RequestStatus.ORDERED, RequestStatus.DRAFT));
  }
}
