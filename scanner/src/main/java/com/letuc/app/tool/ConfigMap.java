package com.letuc.app.tool;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ConfigMap {

    public static List<Path> srcPaths;
    public static Path rootPath;
    public static Path expDir = Path.of(System.getProperty("user.dir") + "\\autodoc\\");
    public static String expFileName = "AutoDoc";

    public static void init(String configPath) {
        if (configPath.equals("test")) {
            srcPaths = collectJavaSourcePaths(Path.of(System.getProperty("user.dir")).resolve("autodoc").resolve("autodoc_config.yaml"));
        } else {
            srcPaths = collectJavaSourcePaths(Path.of(configPath));
        }
    }

    public static Path getExpJSONFilePath() {
        return expDir.resolve(expFileName + ".json");
    }

    public static Path getExpMarkdownFilePath() {
        return expDir.resolve(expFileName + ".md");
    }

    private static List<Path> collectJavaSourcePaths(Path configFilePath) throws RuntimeException {
        if (!configFilePath.toFile().exists()) {
            System.err.println("错误: 配置文件未找到，路径: " + configFilePath.toAbsolutePath());
            throw new RuntimeException("config file not found: " + configFilePath.toAbsolutePath());
        }
        List<Path> includePaths;
        rootPath = configFilePath.getParent();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
            Map<String, Object> topLevelMap = yaml.load(inputStream);
            includePaths = extractIncludes(topLevelMap);
            List<String> rootPathConfig = extractRoot(topLevelMap);
            if (rootPathConfig != null) {
                rootPath = Path.of(rootPathConfig.get(0));
            }
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败: " + e.getMessage(), e);
        }
        List<Path> srcPaths = new ArrayList<>();
        includePaths.forEach(includePath -> {srcPaths.add(rootPath.resolve(includePath).normalize());});
        return srcPaths;
    }

    @SuppressWarnings("unchecked")
    private static List<Path> extractIncludes(Map<String, Object> topLevelMap) {
        if (!topLevelMap.containsKey("scan")) {
            System.err.println("错误: YAML 缺少顶层键 'scan'");
            return Collections.emptyList();
        }
        try {
            Map<String, Object> scanMap = (Map<String, Object>) topLevelMap.get("scan");
            List<String> includesList = (List<String>) scanMap.get("src");
            if (includesList == null) {
                System.err.println("错误: YAML 缺少 'scan.src' 键");
                return Collections.emptyList();
            }
            return includesList.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> Path.of(s.trim()))
                    .collect(Collectors.toList());
        } catch (ClassCastException | NullPointerException e) {
            System.err.println("错误: YAML 结构不正确或缺少必要的键 (scan.src)");
            return Collections.emptyList();
        }
    }

    private static List<String> extractRoot(Map<String, Object> topLevelMap) {
        if (!topLevelMap.containsKey("root")) {
            return null;
        }
        Object rootValue = topLevelMap.get("root");
        if (!(rootValue instanceof List<?> rawList)) {
            System.err.println("错误: YAML 'root' 键的值不是一个列表 (List)");
            return null;
        }
        List<String> resultList = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String) {
                resultList.add((String) item);
            } else if (item != null) {
                System.err.println("警告: YAML 'root' 列表包含非字符串元素, 已忽略: " + item);
            }
        }
        return resultList;
    }
}