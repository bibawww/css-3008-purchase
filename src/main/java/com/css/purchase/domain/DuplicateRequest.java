package com.css.purchase.domain;

/** A purchase request with this business key already exists. Unchecked, so a transaction rolls back. */
public class DuplicateRequest extends RuntimeException {

  public DuplicateRequest(String businessKey) {
    super("duplicate purchase request: " + businessKey);
  }
}
