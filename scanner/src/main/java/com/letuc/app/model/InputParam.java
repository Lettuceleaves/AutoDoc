package com.letuc.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputParam {
    private String type;
    private String name;
    private String field;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<InputParam> subParams;
}