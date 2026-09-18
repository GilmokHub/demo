package kr.gilmok.demo.auth.dto;

public record LoginResponse(
        long accessTokenExpiresIn,
        String username,
        String role
) {
}
