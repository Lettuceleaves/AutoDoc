package com.letuc.app.scanner;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScanConfig {

    public static List<Path> collectJavaSourcePaths(Path projectRootPath) throws RuntimeException {

        Path configFilePath = projectRootPath.resolve("autodoc.yaml");

        if (!configFilePath.toFile().exists()) {
            System.err.println("错误: 配置文件未找到，路径: " + configFilePath.toAbsolutePath());
            throw new RuntimeException("config file not found: " + configFilePath.toAbsolutePath());
        }

        List<Path> includePaths;
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
            Map<String, Object> topLevelMap = yaml.load(inputStream);
            includePaths = extractIncludes(topLevelMap);
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败: " + e.getMessage(), e);
        }

        List<Path> srcPaths = new ArrayList<>();
        includePaths.forEach(includePath -> {srcPaths.add(projectRootPath.resolve(includePath).normalize());});

        return srcPaths;
    }

    @SuppressWarnings("unchecked")
    private static List<Path> extractIncludes(Map<String, Object> topLevelMap) {
        if (topLevelMap == null || !topLevelMap.containsKey("scan")) {
            System.err.println("错误: YAML 缺少顶层键 'scan'");
            return Collections.emptyList();
        }

        try {
            Map<String, Object> scanMap = (Map<String, Object>) topLevelMap.get("scan");
            List<String> includesList = (List<String>) scanMap.get("src");

            return includesList.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> Path.of(s.trim()))
                    .collect(Collectors.toList());
        } catch (ClassCastException | NullPointerException e) {
            System.err.println("错误: YAML 结构不正确或缺少必要的键 (scan.src)");
            return Collections.emptyList();
        }
    }
}