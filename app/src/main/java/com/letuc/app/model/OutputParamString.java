package com.letuc.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutputParamString extends OutputParam {
    List<Boolean> warning;
    List<String> values;

    public OutputParamString(String type, String name, List<OutputParam> subParams, Set<InitMethodInfo> methods, Set<String> methodsFilter, List<Boolean> warning, List<String> values) {
        super(type, name, subParams, methods, methodsFilter);
        this.warning = warning;
        this.values = values;
    }
}
