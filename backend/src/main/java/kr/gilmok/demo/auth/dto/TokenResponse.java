package kr.gilmok.demo.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}
