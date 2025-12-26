package com.groo.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    FORBIDDEN_OPERATION(HttpStatus.FORBIDDEN, "FORBIDDEN_OPERATION", "이 작업을 수행할 권한이 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_SOCIAL_TOKEN", "소셜 토큰을 검증할 수 없습니다."),
    SOCIAL_LOGIN_FAILURE(HttpStatus.BAD_REQUEST, "SOCIAL_LOGIN_FAILURE", "소셜 로그인 처리 중 오류가 발생했습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "요청 필드 유효성 검증에 실패했습니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", "그룹을 찾을 수 없습니다."),
    GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "GROUP_ACCESS_DENIED", "그룹에 접근할 권한이 없습니다."),
    GROUP_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "GROUP_MEMBER_ALREADY_EXISTS", "이미 그룹에 속한 사용자입니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_MEMBER_NOT_FOUND", "그룹 멤버를 찾을 수 없습니다."),
    INVITATION_INVALID(HttpStatus.BAD_REQUEST, "INVITATION_INVALID", "유효하지 않은 초대 코드입니다."),
    GROUP_ARCHIVED(HttpStatus.BAD_REQUEST, "GROUP_ARCHIVED", "이미 비활성화된 그룹입니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_ACCESS_DENIED", "프로젝트에 접근할 권한이 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "작업을 찾을 수 없습니다."),
    SUBTASK_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBTASK_NOT_FOUND", "하위 작업을 찾을 수 없습니다."),
    TASK_UPDATE_INVALID(HttpStatus.BAD_REQUEST, "TASK_UPDATE_INVALID", "변경할 작업 정보가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
