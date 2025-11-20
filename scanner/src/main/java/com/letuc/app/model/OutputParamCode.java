package com.letuc.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.letuc.app.scanner.ScanEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutputParamCode extends OutputParam {

    // 【核心修改】使用 Set<RawEntry> 替代原来的两个 List
    // 使用 LinkedHashSet 保证插入顺序 (先扫到的先显示)，同时利用 Set 特性去重
    @JsonIgnore
    private Set<RawEntry> uniqueEntries = new LinkedHashSet<>();

    public OutputParamCode(String className, String origin, String name, List<OutputParam> subParams,
                           List<Integer> types, List<String> values) {
        super(className, origin, name, subParams);
        // 构造函数保留兼容性，将传入的 List 转入 Set
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                int type = (types != null && i < types.size() && types.get(i) != null) ? types.get(i) : 0;
                this.addEntry(type, values.get(i));
            }
        }
    }

    /**
     * 【新增方法】供扫描器调用，添加数据并自动去重
     */
    public void addEntry(int type, String value) {
        if (value != null) {
            this.uniqueEntries.add(new RawEntry(type, value));
        }
    }

    @JsonProperty("values")
    public List<ValueDetail> getFormattedValues() {
        if (this.uniqueEntries.isEmpty()) {
            return Collections.emptyList();
        }

        List<ValueDetail> result = new ArrayList<>();
        int index = 0;

        // 遍历去重后的集合
        for (RawEntry entry : this.uniqueEntries) {
            String rawVal = entry.getRawValue();
            int typeVal = entry.getType();

            Object finalValue = rawVal;
            String description = null;

            // 处理枚举类型 (Type = 1)
            if (typeVal == 1 && rawVal != null && rawVal.contains(".")) {
                try {
                    int lastDotIndex = rawVal.lastIndexOf('.');
                    String className = rawVal.substring(0, lastDotIndex);
                    String constantName = rawVal.substring(lastDotIndex + 1);

                    // 安全获取 Enum Map
                    if (ScanEnums.enumMap.containsKey(className)) {
                        Map<String, List<String>> constMap = ScanEnums.enumMap.get(className);
                        if (constMap != null && constMap.containsKey(constantName)) {
                            List<String> enumInfo = constMap.get(constantName);

                            // 获取 Value (Index 0)
                            if (enumInfo != null && !enumInfo.isEmpty()) {
                                finalValue = enumInfo.get(0);

                                // 获取 Description (Index 1)
                                if (enumInfo.size() > 1) {
                                    description = enumInfo.get(1);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error resolving enum description for: " + rawVal);
                }
            }

            result.add(new ValueDetail(index++, typeVal, finalValue, description));
        }
        return result;
    }

    /**
     * 内部类：用于 Set 去重的键值对
     * 必须重写 Equals 和 HashCode (Lombok @Data 已包含)
     */
    @Data
    @AllArgsConstructor
    private static class RawEntry {
        private int type;
        private String rawValue;
    }

    /**
     * 内部类：最终输出给 JSON 的格式
     */
    @Data
    @AllArgsConstructor
    static class ValueDetail {
        private int index;
        private int type;
        private Object value;
        private String description;
    }
}