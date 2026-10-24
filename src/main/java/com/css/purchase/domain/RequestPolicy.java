package com.css.purchase.domain;

import java.util.Objects;

/** Business rules for moving a purchase request between statuses. Plain Java, no Spring. */
public final class RequestPolicy {

  private final Rule rules;

  public RequestPolicy(Rule rules) {
    this.rules = Objects.requireNonNull(rules, "rules");
  }

  public RequestStatus move(RequestStatus from, RequestStatus to) {
    rules.check(from, to);
    return to;
  }
}
