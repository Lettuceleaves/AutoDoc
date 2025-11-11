package com.letuc.app.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScanFilePaths {
    public static List<Path> scan(List<Path> srcPaths) {
        List<Path> filePaths = new ArrayList<>();
        for (Path srcPath : srcPaths) {
            if (Files.isDirectory(srcPath)) {
                System.out.println("-> 开始扫描目录: " + srcPath);
                try (Stream<Path> stream = Files.walk(srcPath)) {
                    List<Path> filesInDir = stream
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                            .collect(Collectors.toList());
                    filePaths.addAll(filesInDir);
                } catch (IOException e) {
                    System.err.println("错误: 遍历目录失败 " + srcPath + ": " + e.getMessage());
                }
            } else {
                System.err.println("警告: 扫描路径不存在或不是有效目录: " + srcPath);
            }
        }
        printCollectedFiles(filePaths);
        return filePaths;
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
