package com.letuc.app.entry;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.ScanControllers;
import com.letuc.app.scanner.ScanDI;
import com.letuc.app.scanner.ScanUsagePoints;
import com.letuc.app.tool.FileCollector;
import com.letuc.app.tool.InterfaceToBean;
import com.letuc.app.tool.SymbolSolver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoDocStarter {
    public static String controllerTail = "Controller.java";

    public static void run() {
        try {
            // 1. 定义你的“源文件根目录”(Source Roots)
            //    这是 SymbolSolver 能“理解”项目的关键
            //    你必须把 *所有* 模块的 src/main/java 都加进来
            List<Path> sourceRoots = List.of(
                    Paths.get("test/src/main/java") // 示例路径，根据你的项目修改
                    // e.g., Paths.get("module-service/src/main/java"),
                    // e.g., Paths.get("module-common/src/main/java")
            );

            // 2. 用“源文件根目录”初始化 SymbolSolver
            SymbolSolver.init(sourceRoots);

            // 3. 从项目根目录收集 *所有* .java 文件
            //    (FileCollector 应该会遍历所有子目录)
            List<Path> allJavaFiles = FileCollector.collectJavaFiles(Paths.get(System.getProperty("user.dir")));

            // 4. 【阶段一】: 构建“全局方法索引” (FQN -> AST)
            //    这一步 *必须* 在所有扫描之前，因为它依赖“活跃”的 AST
            System.out.println("开始构建全局方法索引...");
            Map<String, MethodDeclaration> globalMethodIndex = buildMap(allJavaFiles);
            System.out.println("索引构建完毕，共 " + globalMethodIndex.size() + " 个方法。");

            // 5. 【阶段二】: 运行你的扫描器
            Map<String, SingleControllerInfo> controllerInfo = ScanControllers.scan(allJavaFiles, controllerTail);
            ScanDI.scan(allJavaFiles); // 假设这个方法会填充 InterfaceToBean
            InterfaceToBean.print(); // 打印 DI 映射

            // 6. 【阶段三】: 运行“索引式 BFS”
            //    把“索引”和“DI映射”都传进去
            System.out.println("开始扫描调用链 (BFS)...");
            controllerInfo = ScanUsagePoints.scan(
                    controllerInfo,
                    globalMethodIndex, // 传入“索引”
                    InterfaceToBean.pairs // 传入“DI映射” (我假设 InterfaceToBean 有这个 getter)
            );
            System.out.println("调用链扫描完毕。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 【阶段一】构建全局方法索引
     * 遍历所有 .java 文件，解析它们，并将每个方法（FQN -> AST节点）存入 Map。
     *
     * @param javaFiles 所有待扫描的 .java 文件
     * @return FQN -> MethodDeclaration 的全局映射
     */
    public static Map<String, MethodDeclaration> buildMap(List<Path> javaFiles) {
        Map<String, MethodDeclaration> globalMethodIndex = new HashMap<>();

        for (Path path : javaFiles) {
            File javaFile = path.toFile();
            if (!javaFile.exists()) continue;

            try {
                // 1. 解析
                //    StaticJavaParser 会自动使用你用 SymbolSolver.init() 初始化的全局求解器
                CompilationUnit cu = StaticJavaParser.parse(javaFile);

                // 2. 找到所有方法
                List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

                for (MethodDeclaration method : methods) {
                    try {
                        // 3. 拿到 FQN，存入索引
                        //    ★ 这里的 'method' 是“活跃”的，
                        //    ★ 因为 'cu' (编译单元) 节点还活着
                        String fqn = method.resolve().getQualifiedSignature();
                        globalMethodIndex.put(fqn, method);
                    } catch (Exception e) {
                        // (解析失败，跳过)
                        // System.err.println("无法解析 FQN: " + method.getNameAsString() + " in " + javaFile.getName());
                    }
                }
            } catch (Exception e) {
                // (解析失败，跳过)
                // System.err.println("无法解析文件: " + javaFile.getName());
            }
        }
        return globalMethodIndex;
    }
}