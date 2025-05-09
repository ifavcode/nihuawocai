package cn.guetzjb.drawguess.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotLoginException extends RuntimeException {

    private Integer code;

    private String message;

    public NotLoginException(String message) {
        this.message = message;
    }

}
