package com.letuc.app.entry;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;

public class DebugResolver {
    public static void main(String[] args) throws Exception {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JavaParserTypeSolver(new File("E:/projects/AutoDoc/test/src/main/java")));

        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(solver));

        CompilationUnit cu = StaticJavaParser.parse(new File("E:/projects/AutoDoc/test/src/main/java/com/letuc/test/controller/TestController.java"));
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();

        method.getParameters().forEach(param -> {
            try {
                System.out.println(param.getTypeAsString() + " -> " + param.getType().resolve().describe());
            } catch (Exception e) {
                System.err.println("❌ 无法解析类型: " + param.getTypeAsString() + " - " + e);
            }
        });
    }
}
