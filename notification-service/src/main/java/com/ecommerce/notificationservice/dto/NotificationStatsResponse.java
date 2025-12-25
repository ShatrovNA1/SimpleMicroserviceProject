package com.ecommerce.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsResponse {
    private long totalNotifications;
    private long pendingCount;
    private long sendingCount;
    private long sentCount;
    private long failedCount;
    private long cancelledCount;
}

