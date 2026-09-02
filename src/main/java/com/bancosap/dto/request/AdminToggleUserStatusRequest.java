package com.bancosap.dto.request;

import com.bancosap.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public class AdminToggleUserStatusRequest {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "O novo status é obrigatório.")
    private UserStatus status;

    private String reason;

    public AdminToggleUserStatusRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
