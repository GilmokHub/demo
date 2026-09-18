package kr.gilmok.demo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C000", "서버 내부 오류입니다."),
    NOT_ENOUGH_KEY_LENGTH(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "JWT 시크릿은 최소 256비트(32바이트)여야 합니다."),
    INVALID_USER(HttpStatus.UNAUTHORIZED, "U001", "유효하지 않은 회원 정보입니다."),
    INACTIVATED_USER(HttpStatus.FORBIDDEN, "U002", "비활성화된 회원 입니다."),
    
    // 토큰 관련 공통 에러
    ACCESS_TOKEN_BLOCKED(HttpStatus.UNAUTHORIZED, "AT004", "로그아웃된 토큰입니다. 다시 로그인해주세요."),
    SECURITY_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "보안 시스템 장애로 인해 인증을 처리할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
