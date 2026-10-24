package com.css.purchase.domain;

import java.util.Objects;

/** Business rules for moving a purchase request between statuses. Plain Java. */
public final class RequestPolicy {

  public RequestStatus move(RequestStatus from, RequestStatus to) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");

    if (from == RequestStatus.DRAFT && to == RequestStatus.APPROVED) {
      return to;
    }
    if (from == RequestStatus.APPROVED && to == RequestStatus.ORDERED) {
      return to;
    }
    if (from == RequestStatus.DRAFT && to == RequestStatus.ORDERED) {
      throw new IllegalStateException("skipped approval: DRAFT cannot become ORDERED");
    }
    if (from == RequestStatus.ORDERED && to == RequestStatus.DRAFT) {
      throw new IllegalStateException("reopen after pay: ORDERED cannot go back to DRAFT");
    }
    throw new IllegalStateException("move not allowed: " + from + " -> " + to);
  }
}
