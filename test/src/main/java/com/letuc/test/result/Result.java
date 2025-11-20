
package com.letuc.test.result;

import com.letuc.test.result.errorcode.BaseErrorCode;
import com.letuc.test.result.errorcode.ErrorCode;

public final class Result {

    public static <T> ResultVO<T> success() {
        return new ResultVO<T>()
                .setCode(ResultVO.SUCCESS_CODE);
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<T>().setCode(ResultVO.SUCCESS_CODE).setData(data);
    }

    public static <T> ResultVO<T> failure() {
        return new ResultVO<T>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    public static <T> ResultVO<T> failure(ErrorCode errorCode) {
        return new ResultVO<T>()
                .setCode(errorCode.code())
                .setMessage(errorCode.message());
    }

    public static <T> ResultVO<T> failure(ErrorCode errorCode, T data) {
        return new ResultVO<T>()
                .setCode(errorCode.code())
                .setData(data)
                .setMessage(errorCode.message());
    }
}
