package com.toplms.master.tenant;

import com.toplms.domain.base.Tenant;

public record TenantSummaryDto(Tenant tenant, long userCount, long courseCount) {
}
