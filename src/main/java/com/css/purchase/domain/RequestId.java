package com.css.purchase.domain;

import java.util.UUID;

/** Surrogate id of a purchase request. Never null, never blank. */
public record RequestId(String value) {

  public RequestId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("request id must not be null or blank");
    }
  }

  public static RequestId newId() {
    return new RequestId(UUID.randomUUID().toString());
  }
}
