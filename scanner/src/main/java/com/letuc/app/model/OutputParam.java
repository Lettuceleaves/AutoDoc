package com.letuc.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputParam { // TODO 分离出参根和子出参

    private String className;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String origin;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<OutputParam> subParams;
}