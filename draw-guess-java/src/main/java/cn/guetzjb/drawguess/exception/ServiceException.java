package cn.guetzjb.drawguess.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceException extends RuntimeException {

    private Integer code;

    private String message;

    public ServiceException(String message){
        this.message = message;
    }
}
