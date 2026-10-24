package com.css.purchase.dto;

import com.css.purchase.domain.RequestStatus;

/** Shape of the ERP payload (vendor JSON, later). Maps the vendor state onto our status. */
public record ErpRequestDto(String number, String erpState) {

  public RequestStatus toStatus() {
    return switch (erpState) {
      case "NEW" -> RequestStatus.DRAFT;
      case "SIGNED" -> RequestStatus.APPROVED;
      case "PO_SENT" -> RequestStatus.ORDERED;
      default -> throw new IllegalArgumentException("unknown ERP state: " + erpState);
    };
  }
}
