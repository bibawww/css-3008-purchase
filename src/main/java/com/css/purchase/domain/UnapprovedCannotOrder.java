package com.css.purchase.domain;

import java.util.Objects;

/** Stop-factor: only an APPROVED request can be ORDERED. Then passes to the next rule. */
public final class UnapprovedCannotOrder implements Rule {

  private final Rule next;

  public UnapprovedCannotOrder(Rule next) {
    this.next = Objects.requireNonNull(next, "next");
  }

  @Override
  public void check(RequestStatus from, RequestStatus to) {
    if (to == RequestStatus.ORDERED && from != RequestStatus.APPROVED) {
      throw new IllegalStateException(
          "skipped approval: " + from + " cannot become ORDERED");
    }
    next.check(from, to);
  }
}
