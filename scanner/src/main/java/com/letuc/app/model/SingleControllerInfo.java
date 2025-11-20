package com.letuc.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleControllerInfo {

    private String className;
    private String url;

    @JsonIgnore
    private Map<String, SingleMethodInfo> controllerMap;

    @JsonProperty("controllerMap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<MethodEntry> getFormattedControllerMap() {
        if (this.controllerMap == null || this.controllerMap.isEmpty()) {
            return null;
        }

        List<MethodEntry> list = new ArrayList<>();
        for (Map.Entry<String, SingleMethodInfo> entry : this.controllerMap.entrySet()) {
            list.add(new MethodEntry(entry.getKey(), entry.getValue()));
        }
        return list;
    }
    @Data
    @AllArgsConstructor
    static class MethodEntry {
        private String methodName;
        private SingleMethodInfo methodInfo;
    }
}