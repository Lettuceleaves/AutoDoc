package com.letuc.app.tool;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
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
    public static List<String> methodFQNs;
    public static Set<String> targets =  new HashSet<>();
    public static Path rootPath;
    public static Path expDir = Path.of(System.getProperty("user.dir") + "\\autodoc\\");
    public static String expFileName = "AutoDoc";

    public static void init(String configPath) {
        if (configPath.equals("test")) {
            srcPaths = collectConfigInfo(Path.of(System.getProperty("user.dir")).resolve("autodoc").resolve("autodoc_config.yaml"));
        } else {
            srcPaths = collectConfigInfo(Path.of(configPath));
        }
    }

    public static Path getExpJSONFilePath() {
        return expDir.resolve(expFileName + ".json");
    }

    public static Path getExpMarkdownFilePath() {
        return expDir.resolve(expFileName + ".md");
    }

    public static List<Path> collectConfigInfo(Path configFilePath) throws RuntimeException {
        if (!configFilePath.toFile().exists()) {
            System.err.println("错误: 配置文件未找到，路径: " + configFilePath.toAbsolutePath());
            throw new RuntimeException("config file not found: " + configFilePath.toAbsolutePath());
        }
        List<Path> includePaths;
        rootPath = configFilePath.getParent();
        methodFQNs = new LinkedList<>();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
            Map<String, Object> topLevelMap = yaml.load(inputStream);
            includePaths = extractIncludes(topLevelMap);
            List<String> rootPathConfig = extractRoot(topLevelMap);
            methodFQNs = extractMethodFiles(topLevelMap);
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

    private static List<String> extractMethodFiles(Map<String, Object> topLevelMap) {
        if (!topLevelMap.containsKey("method")) {
            return null;
        }
        Object rootValue = topLevelMap.get("method");
        if (!(rootValue instanceof List<?> rawList)) {
            System.err.println("错误: YAML 'method' 键的值不是一个列表 (List)");
            return null;
        }
        List<String> resultList = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String) {
                resultList.add((String) item);
            } else if (item != null) {
                System.err.println("警告: YAML 'method' 列表包含非字符串元素, 已忽略: " + item);
            }
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public static void collectTargetMethods() {
        if (methodFQNs == null || methodFQNs.isEmpty()) {
            System.err.println("警告: methodFQNs 列表为空，请检查 YAML 配置文件中是否有 'method' 项");
            return;
        }

        // 【调试】检查 ASTMap 是否有数据
        if (ASTMap.AST.isEmpty()) {
            System.err.println("严重错误: ASTMap.AST 为空！尚未加载任何源码，请确保在调用 collectTargetMethods 之前已执行源码扫描逻辑。");
            return;
        }

        for (var ClassFQN : methodFQNs) {
            // 再次 trim 确保安全
            String searchKey = ClassFQN.trim();

            CompilationUnit cu = ASTMap.AST.get(searchKey);

            if (cu == null) {
                System.err.println("\n--- 匹配失败调试 ---");
                System.err.println("配置尝试查找 Key: [" + searchKey + "]");
                System.err.println("ASTMap 中存在的 Key (前5个示例): " + ASTMap.AST.keySet().stream().limit(5).collect(Collectors.toList()));
                System.err.println("--------------------\n");
                continue;
            }

            // 成功获取到 CU，开始遍历方法
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                method.findAncestor(TypeDeclaration.class).ifPresent(parent -> {
                    TypeDeclaration<?> typeDecl = (TypeDeclaration<?>) parent;
                    typeDecl.getFullyQualifiedName().ifPresent(typeFQN -> {
                        String methodName = method.getNameAsString();
                        String fullMethodName = typeFQN + "." + methodName;
                        targets.add(fullMethodName);
                        // System.out.println("已添加目标方法: " + fullMethodName); // 可选：打印成功添加的方法
                    });
                });
            });
        }

        System.out.println("目标方法收集完毕，共收集到 " + targets.size() + " 个方法。");
    }
}