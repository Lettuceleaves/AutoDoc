package com.letuc.app.tool;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ASTMap {
    public static  Map<String, CompilationUnit> AST = new HashMap<>();
    public static  Map<Path, String> classNameOfFile =  new HashMap<>();
    public static void buildMap(List<Path> javaFiles) {
        for (Path path : javaFiles) {
            File javaFile = path.toFile();
            if (!javaFile.exists()) continue;

            try {
                CompilationUnit cu = StaticJavaParser.parse(javaFile);
                Optional<TypeDeclaration<?>> primaryTypeOpt = cu.getPrimaryType();

                if (primaryTypeOpt.isPresent()) {
                    try {
                        primaryTypeOpt.get().getFullyQualifiedName().ifPresent(fqn -> {
                            AST.put(fqn, cu);
                            classNameOfFile.put(path, fqn);
                        });
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }
}
