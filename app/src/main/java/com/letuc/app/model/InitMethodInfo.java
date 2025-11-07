package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
public class InitMethodInfo {

    private final String descriptor;
    private final String className;
    private final String methodName;
    private final List<String> paramTypes;
    private final boolean isConstructor;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitMethodInfo that = (InitMethodInfo) o;
        return Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor);
    }
}