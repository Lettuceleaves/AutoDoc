package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.letuc.app.model.InputParam;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.SymbolSolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParseParams {
    public static List<SingleMethodInfo> parse(List<SingleMethodInfo> controllers) {
        for (SingleMethodInfo singleMethodInfo : controllers) {
            Path path = singleMethodInfo.getFilePath();
            for (InputParam inputParam : singleMethodInfo.getInputParams()) {
                parseParam(inputParam, path);
            }
        }
        return controllers;
    }

    private static InputParam parseParam(InputParam inputParam, Path path) {
        List<InputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(inputParam.getType());

            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                InputParam fieldParam = new InputParam(field.getName(), field.getType().describe(), null);

                if (canResolveType(field.getType().describe())) {
                    parseParam(fieldParam, path);
                } else {
                    fieldParam.setSubParams(null);
                }

                subParams.add(fieldParam);
            }

        } catch (Exception e) {
            return null;
        }

        inputParam.setSubParams(subParams);
        return inputParam;
    }

    private static boolean canResolveType(String typeName) {
        try {
            SymbolSolver.combinedTypeSolver.solveType(typeName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
