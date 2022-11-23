package org.example.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.api.bean.BaseError;
import org.example.api.bean.DefaultError;

@Data
@NoArgsConstructor
public class BaseBusinessException extends RuntimeException {

    private BaseError error = DefaultError.SYSTEM_INTERNAL_ERROR;
    private String extMessage = null;

    public BaseBusinessException(String message) {
        super(message);
        this.extMessage = message;
    }

    public BaseBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.extMessage = message;
    }

    public BaseBusinessException(Throwable cause) {
        super(cause);
    }

    public BaseBusinessException(BaseError error) {
        this.error = error;
    }

    public BaseBusinessException(String message, BaseError error) {
        super(message);
        this.extMessage = message;
        this.error = error;
    }

    public BaseBusinessException(String message, Throwable cause, BaseError error) {
        super(message, cause);
        this.extMessage = message;
        this.error = error;
    }

    public BaseBusinessException(Throwable cause, BaseError error) {
        super(cause);
        this.error = error;
    }
}
