package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleControllerInfo {
    String className;
    String url;
    Map<String, SingleMethodInfo> controllerMap;

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"className\":\"").append(className).append("\",");
        sb.append("\"url\":\"").append(url).append("\"");
        if (this.controllerMap != null && !this.controllerMap.isEmpty()) {
            sb.append(",\"controllerMap\":[");
            for (Map.Entry<String, SingleMethodInfo> entry : controllerMap.entrySet()) {
                sb.append("{");
                sb.append("\"methodName\":\"").append(entry.getKey()).append("\",");
                sb.append("\"methodInfo\":");
                sb.append(entry.getValue().toJson());
                sb.append("},");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
