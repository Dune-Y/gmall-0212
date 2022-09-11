package com.atguigu.gmall.common.exception;

/**
 * @ ClassName CartException
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/10 20:39
 * @ Version 1.0
 */
public class CartException extends RuntimeException{

    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }
}
