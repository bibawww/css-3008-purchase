package com.css.purchase.domain;

/** Outbound port: tell someone that a request was approved. Implemented in `client`. */
public interface ApprovalNotifier {
  void approved(String businessKey);
}
