package com.css.purchase.domain;

/** One business rule for a status move. Throws IllegalStateException when the move is forbidden. */
public interface Rule {
  void check(RequestStatus from, RequestStatus to);
}
