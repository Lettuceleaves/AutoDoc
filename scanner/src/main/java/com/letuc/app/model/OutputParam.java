package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputParam { // TODO 分离出参根和子出参
    String className;
    String origin;
    String name;
    List<OutputParam> subParams;

    public String toJsonContent() {
        StringBuilder sb = new StringBuilder();

        sb.append("\"className\":\"").append(className).append("\",");
        if (origin != null) {
            sb.append("\"origin\":\"").append(origin).append("\",");
        }
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
        return sb.toString();
    }

    public String toJson() {
        return "{" + toJsonContent() + "}";
    }
}
