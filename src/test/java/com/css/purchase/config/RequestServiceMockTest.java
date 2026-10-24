package com.css.purchase.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.css.purchase.domain.ApprovalNotifier;
import com.css.purchase.domain.RequestStatus;
import com.css.purchase.domain.TransitionRule;
import com.css.purchase.domain.UnapprovedCannotOrder;
import com.css.purchase.persistence.RequestJdbc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The outbound port is mocked: no real HTTP, no real database. */
@ExtendWith(MockitoExtension.class)
class RequestServiceMockTest {

  @Mock ApprovalNotifier notifier;
  @Mock RequestJdbc requests;

  private RequestService service() {
    return new RequestService(
        new UnapprovedCannotOrder(new TransitionRule()), requests, notifier);
  }

  @Test
  void approvalCallsThePort() {
    assertEquals(RequestStatus.APPROVED,
        service().move("PR-19", RequestStatus.DRAFT, RequestStatus.APPROVED));
    verify(notifier).approved("PR-19");
  }

  @Test
  void forbiddenMoveNeverCallsThePort() {
    assertThrows(IllegalStateException.class,
        () -> service().move("PR-19", RequestStatus.DRAFT, RequestStatus.ORDERED));
    verifyNoInteractions(notifier);
  }
}
