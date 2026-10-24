package com.css.purchase.config;

import com.css.purchase.domain.ApprovalNotifier;
import com.css.purchase.domain.DuplicateRequest;
import com.css.purchase.domain.RequestId;
import com.css.purchase.domain.RequestStatus;
import com.css.purchase.domain.Rule;
import com.css.purchase.persistence.RequestJdbc;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestService {

  private static final String INSERT_STATUS = RequestStatus.DRAFT.name();

  private final Rule rules;
  private final RequestJdbc requests;
  private final ApprovalNotifier notifier;

  public RequestService(Rule rules, RequestJdbc requests, ApprovalNotifier notifier) {
    this.rules = rules;
    this.requests = requests;
    this.notifier = notifier;
  }

  /** Checks the rules, then tells the outside world when the request is approved. */
  public RequestStatus move(String businessKey, RequestStatus from, RequestStatus to) {
    rules.check(from, to);
    if (to == RequestStatus.APPROVED) {
      notifier.approved(businessKey);
    }
    return to;
  }

  /** One transaction per call. Duplicate business key -> DuplicateRequest, first row stays. */
  @Transactional
  public void register(RequestId id, String businessKey, String title) {
    requireKey(businessKey);
    try {
      requests.insert(UUID.fromString(id.value()), businessKey, INSERT_STATUS, title);
    } catch (DuplicateKeyException ex) { // SQLState 23505
      throw new DuplicateRequest(businessKey); // unchecked -> rollback
    }
  }

  /** ONE transaction, two INSERTs. The second fails, so the first is rolled back too. */
  @Transactional
  public void insertTwice(String businessKey) {
    requireKey(businessKey);
    requests.insert(UUID.randomUUID(), businessKey, INSERT_STATUS, "Laptops for the lab");
    try {
      requests.insert(UUID.randomUUID(), businessKey, INSERT_STATUS, "Laptops again");
    } catch (DuplicateKeyException ex) {
      throw new DuplicateRequest(businessKey); // unchecked -> rollback
    }
  }

  public int count(String businessKey) {
    return requests.count(businessKey);
  }

  private static void requireKey(String businessKey) {
    if (businessKey == null || businessKey.isBlank()) {
      throw new IllegalArgumentException("business key must not be null or blank");
    }
  }
}
