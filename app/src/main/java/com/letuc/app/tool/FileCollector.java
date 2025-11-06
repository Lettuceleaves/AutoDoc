package com.letuc.app.tool;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileCollector {

    public static List<Path> collectJavaFiles(Path projectRootPath) throws RuntimeException {

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

        List<Path> javaFiles = new ArrayList<>();

        for (Path includeDir : includePaths) {
            Path absoluteIncludePath = projectRootPath.resolve(includeDir).normalize();

            if (Files.isDirectory(absoluteIncludePath)) {
                System.out.println("-> 开始扫描目录: " + absoluteIncludePath);
                try (Stream<Path> stream = Files.walk(absoluteIncludePath)) {
                    List<Path> filesInDir = stream
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                            .collect(Collectors.toList());
                    javaFiles.addAll(filesInDir);
                } catch (IOException e) {
                    System.err.println("错误: 遍历目录失败 " + absoluteIncludePath + ": " + e.getMessage());
                }
            } else if (Files.isRegularFile(absoluteIncludePath) && absoluteIncludePath.toString().toLowerCase().endsWith(".java")) {
                javaFiles.add(absoluteIncludePath);
            } else {
                System.err.println("警告: 扫描路径不存在或不是有效目录/文件: " + absoluteIncludePath);
            }
        }

        printCollectedFiles(javaFiles);

        return javaFiles;
    }

    @SuppressWarnings("unchecked")
    private static List<Path> extractIncludes(Map<String, Object> topLevelMap) {
        if (topLevelMap == null || !topLevelMap.containsKey("scan")) {
            System.err.println("错误: YAML 缺少顶层键 'scan'");
            return Collections.emptyList();
        }

        try {
            Map<String, Object> scanMap = (Map<String, Object>) topLevelMap.get("scan");
            Map<String, Object> pathMap = (Map<String, Object>) scanMap.get("path");
            List<String> includesList = (List<String>) pathMap.get("includes");

            return includesList.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> Path.of(s.trim()))
                    .collect(Collectors.toList());
        } catch (ClassCastException | NullPointerException e) {
            System.err.println("错误: YAML 结构不正确或缺少必要的键 (scan.path.includes)");
            return Collections.emptyList();
        }
    }

    private static void printCollectedFiles(List<Path> paths) {
        System.out.println("""
                
                ---------- 收集到的 Java 文件列表 ----------
                """);
        if (paths.isEmpty()) {
            System.out.println("(未找到任何 Java 文件)");
        }
        for (Path path : paths) {
            System.out.println(path);
        }
        System.out.println("\n-------------------------------------------");
        System.out.println("总共找到文件: " + paths.size() + " 个");
        System.out.println("-------------------------------------------");
    }
}