package com.letuc.app.tool;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class SymbolSolver {

    public static volatile CombinedTypeSolver combinedTypeSolver;

    public static synchronized void init(List<Path> paths) {
        if (combinedTypeSolver != null) return;

        combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        for (Path path : paths) {
            File file = path.toFile();
            if (file.exists() && file.isDirectory()) {
                combinedTypeSolver.add(new JavaParserTypeSolver(file));
            }
        }

        StaticJavaParser.getConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
    }

}