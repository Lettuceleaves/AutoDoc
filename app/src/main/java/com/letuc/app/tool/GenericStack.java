package com.letuc.app.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class GenericStack {

    Stack<String> stack = new Stack<>();

    public GenericStack(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return;
        }
        List<String> topLevelParams = parseDiamond(fullName);
        Collections.reverse(topLevelParams);
        for (String param : topLevelParams) {
            stack.push(param.trim());
        }
    }

    public String next() {
        if (stack.isEmpty()) {
            return null;
        }

        String currentType = stack.pop();
        List<String> nestedParams = parseDiamond(currentType);
        Collections.reverse(nestedParams);
        for (String param : nestedParams) {
            stack.push(param.trim());
        }

        return getBaseName(currentType);
    }

    private static List<String> parseDiamond(String typeName) {
        List<String> params = new ArrayList<>();
        int firstAngle = typeName.indexOf('<');
        int lastAngle = typeName.lastIndexOf('>');

        if (firstAngle == -1 || lastAngle == -1 || lastAngle < firstAngle) {
            return params;
        }

        String content = typeName.substring(firstAngle + 1, lastAngle).trim();
        if (content.isEmpty()) {
            return params;
        }

        int balance = 0;
        int start = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '<') {
                balance++;
            } else if (c == '>') {
                balance--;
            } else if (c == ',' && balance == 0) {
                params.add(content.substring(start, i).trim());
                start = i + 1;
            }
        }
        params.add(content.substring(start).trim());

        return params;
    }
    private static String getBaseName(String typeName) {
        int firstAngle = typeName.indexOf('<');
        if (firstAngle == -1) {
            return typeName.trim();
        } else {
            return typeName.substring(0, firstAngle).trim();
        }
    }
}