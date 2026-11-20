package com.css.purchase.domain;

import java.util.UUID;

/** No purchase request with this id. Unchecked, like DuplicateRequest. */
public class RequestNotFound extends RuntimeException {

  public RequestNotFound(UUID id) {
    super("purchase request not found: " + id);
  }
}
