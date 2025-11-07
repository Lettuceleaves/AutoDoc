package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.letuc.app.model.OutputParam;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.SymbolSolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParseOutputParam {
    public static void parse(List<SingleMethodInfo> controllers) {
        for (SingleMethodInfo singleMethodInfo : controllers) {
            Path path = singleMethodInfo.getFilePath();
            parseOutputParam(singleMethodInfo.getOutputParam());
        }
    }
    public static void parseOutputParam(OutputParam outputParam) {
        List<OutputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(outputParam.getType());

            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                try {
                    OutputParam fieldParam = new OutputParam(field.getType().describe(), field.getName(), null);

                    if (canResolveType(field.getType().describe())) {
                        parseOutputParam(fieldParam);
                    } else {
                        fieldParam.setSubParams(null);
                    }

                    subParams.add(fieldParam);
                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            return;
        }

        outputParam.setSubParams(subParams);
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
