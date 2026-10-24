package com.css.purchase.client;

import com.css.purchase.domain.ApprovalNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Stand-in for a real outbound call (email, ERP). Only logs for now. */
@Component
public class LogApprovalNotifier implements ApprovalNotifier {

  private static final Logger log = LoggerFactory.getLogger(LogApprovalNotifier.class);

  @Override
  public void approved(String businessKey) {
    log.info("purchase request {} approved", businessKey);
  }
}
