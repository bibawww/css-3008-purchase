package com.css.purchase.domain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class RequestIdTest {

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void nullOrBlankIdThrows(String value) {
    assertThrows(IllegalArgumentException.class, () -> new RequestId(value));
  }

  @Test
  void newIdsAreDifferent() {
    assertNotEquals(RequestId.newId(), RequestId.newId());
  }
}
