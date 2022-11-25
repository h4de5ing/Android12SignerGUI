package org.example.api.bean;

public enum DefaultError implements BaseError {
    SYSTEM_INTERNAL_ERROR("0000", "系统内部错误"),
    PARAMETER_ERROR("0001", "参数错误"),
    PARAMETER_EMPTY("0002", "参数为空");

    String errorCode;
    String errorMessage;

    DefaultError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
