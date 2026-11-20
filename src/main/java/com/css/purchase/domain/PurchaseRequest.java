package com.css.purchase.domain;

import java.util.UUID;

/** One stored purchase request as the domain sees it. Plain Java, no Spring. */
public record PurchaseRequest(UUID id, String businessKey, RequestStatus status, String title) {
}
