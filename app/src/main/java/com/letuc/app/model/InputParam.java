package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputParam {
    String type;
    String name;
    List<InputParam> subParams;

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(type).append("\",");
        sb.append("\"name\":\"").append(name).append("\"");
        if (this.subParams != null && !this.subParams.isEmpty()) {
            sb.append(",\"subParams\":[");
            for (int i = 0; i < subParams.size(); i++) {
                sb.append(subParams.get(i).toJson());
                if (i < subParams.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
