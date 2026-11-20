package com.css.purchase.handler;

import com.css.purchase.domain.PurchaseRequest;

import java.util.UUID;

public record RequestResponse(UUID id, String businessKey, String status, String title) {

    static RequestResponse from(PurchaseRequest r) {
        return new RequestResponse(r.id(), r.businessKey(), r.status().name(), r.title());
    }
}
