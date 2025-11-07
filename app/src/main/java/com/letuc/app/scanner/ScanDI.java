package com.letuc.app.scanner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.letuc.app.tool.InterfaceToBean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ScanDI {
    public static void scan(List<Path> paths) throws IOException {
        for (Path path : paths) {
            CompilationUnit cu = StaticJavaParser.parse(path);
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                if (isSpringComponent(clazz)) {
                    String implementationName = getFullClassName(cu, clazz.getNameAsString());
                    for (ClassOrInterfaceType implementedInterface : clazz.getImplementedTypes()) {
                        String interfaceName = resolveFullQualifiedName(cu, implementedInterface.getNameAsString());
                        InterfaceToBean.set(interfaceName, implementationName);
                    }
                }
            });
        }
    }
    private static boolean isSpringComponent(ClassOrInterfaceDeclaration clazz) {
        return clazz.isAnnotationPresent("Service") ||
                clazz.isAnnotationPresent("Component") ||
                clazz.isAnnotationPresent("Repository");
    }

    private static String getFullClassName(CompilationUnit cu, String simpleName) {
        return cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString() + "." + simpleName)
                .orElse(simpleName);
    }

    private static String resolveFullQualifiedName(CompilationUnit cu, String simpleName) {
        return cu.getImports().stream()
                .filter(i -> !i.isStatic())
                .filter(i -> i.getNameAsString().endsWith("." + simpleName) || i.getNameAsString().equals(simpleName))
                .findFirst()
                .map(NodeWithName::getNameAsString)
                .orElseGet(() -> getFullClassName(cu, simpleName));
    }
}
