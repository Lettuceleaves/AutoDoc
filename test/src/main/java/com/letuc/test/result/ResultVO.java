package com.letuc.test.result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ResultVO<T> {
    public static final String SUCCESS_CODE = "0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String code;
    private T data;
    private String message;

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }

    public boolean isFail() {
        return !isSuccess();
    }

    public String toJsonString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public ResultVO(String code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

}
