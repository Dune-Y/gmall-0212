package com.atguigu.gmall.common.exception;

/**
 * @ ClassName AuthException
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/8 14:57
 * @ Version 1.0
 */

public class AuthException extends RuntimeException{
    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }
}
