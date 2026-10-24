package com.css.purchase.domain;

/** Statuses of a purchase request. */
public enum RequestStatus {
  DRAFT,
  APPROVED,
  ORDERED;

  /** Reads a status from text (for example, from a database column). Unknown text throws. */
  public static RequestStatus fromText(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("status must not be null or blank");
    }
    for (RequestStatus s : values()) {
      if (s.name().equals(text)) {
        return s;
      }
    }
    throw new IllegalArgumentException("unknown status: " + text);
  }
}
