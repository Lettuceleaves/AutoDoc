package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.letuc.app.model.OutputParam;
import com.letuc.app.model.OutputParamString;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.SymbolSolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParseOutputParam {
    public static void parse(Map<String, SingleMethodInfo> controllers) {
        for (Map.Entry<String, SingleMethodInfo> singleMethodInfo : controllers.entrySet()) {
            parseOutputParam(singleMethodInfo.getValue().getOutputParam());
            singleMethodInfo.getValue().getOutputParam().addAllArgsConstructorToInitMethods();
        }
    }
    public static void parseOutputParam(OutputParam outputParam) {
        List<OutputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(outputParam.getType());

            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                try {
                    if (field.isStatic()) {
                        continue;
                    }
                    OutputParam fieldParam = new OutputParam(field.getType().describe(), field.getName(), null, null, null);
                    if (field.getType().describe().equals("java.lang.String")) {
                        fieldParam = new OutputParamString(field.getType().describe(), field.getName(), null, null, null, new LinkedList<>(), new LinkedList<>());
                        subParams.add(fieldParam);
                    } else {
                        if (canResolveType(field.getType().describe())) {
                            parseOutputParam(fieldParam);
                        } else {
                            fieldParam.setSubParams(null);
                        }
                        subParams.add(fieldParam);
                    }
                } catch (Exception ignored) {
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
