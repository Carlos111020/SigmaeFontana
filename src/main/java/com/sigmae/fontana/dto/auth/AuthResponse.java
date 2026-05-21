package com.sigmae.fontana.dto.auth;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        String tipo,
        LocalDateTime expiraEn,
        UserSummaryResponse usuario
) {
}
