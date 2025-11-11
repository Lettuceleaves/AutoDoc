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

    public OutputParamString(String type, String origin, String name, List<OutputParam> subParams, Set<InitMethodInfo> methods, Set<String> methodsFilter, List<Boolean> warning, List<String> values) {
        super(type, origin, name, subParams, methods, methodsFilter);
        this.warning = warning;
        this.values = values;
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(super.toJsonContent());
        sb.append(",\"values\":[");
        if (this.values != null) {
            for (int i = 0; i < this.values.size(); i++) {
                sb.append("{");
                sb.append("\"index\":").append(i).append(",");
                String warningStatus = (this.warning == null || this.warning.isEmpty() || !this.warning.get(i)) ? "OK" : "Warning";
                sb.append("\"status\":\"").append(warningStatus).append("\",");
                String value = this.values.get(i);
                sb.append("\"value\":").append(value);
                sb.append("}");
                if (i < this.values.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
