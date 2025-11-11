package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.letuc.app.model.InputParam;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.ParseRecursiveEndPoint;
import com.letuc.app.tool.SymbolSolver;

import java.util.*;

public class ParseInputParams {

    public static Map<String, SingleMethodInfo> parse(Map<String, SingleMethodInfo> controllers) {
        for (Map.Entry<String, SingleMethodInfo> singleMethodInfoEntry : controllers.entrySet()) {
            for (InputParam inputParam : singleMethodInfoEntry.getValue().getInputParams()) {
                parseInputParam(inputParam);
            }
        }
        return controllers;
    }

    private static void parseInputParam(InputParam inputParam) {
        List<InputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(inputParam.getType());

            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                InputParam fieldParam = new InputParam(field.getType().describe(), field.getName(), null, null);

                if (canResolveType(field.getType().describe()) && !ParseRecursiveEndPoint.set.contains(field.getType().describe())) {
                    parseInputParam(fieldParam);
                } else {
                    fieldParam.setSubParams(null);
                }

                subParams.add(fieldParam);
            }

        } catch (Exception e) {
            return;
        }

        inputParam.setSubParams(subParams);
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
