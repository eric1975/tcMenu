/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeParameter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.thecoderscorner.menu.pluginapi.AbstractCodeCreator.LINE_BREAK;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType;

public class CodeVariableCppExtractor implements CodeVariableExtractor {
    private final CodeConversionContext context;

    public CodeVariableCppExtractor(CodeConversionContext context) {
        this.context = context;
    }

    @Override
    public String mapFunctions(List<FunctionCallBuilder> functions) {
        return functions.stream().map(this::functionToCode)
                .collect(Collectors.joining(LINE_BREAK));

    }

    private String functionToCode(FunctionCallBuilder func) {
        var memberAccessor = (func.isPointerType()) ? "->" : ".";

        String fn = "    ";
        if(func.getObjectName().isPresent()) {
            fn += func.getObjectName().get() + memberAccessor;
        }
        fn += func.getFunctionName() + "(";
        var parameters = func.getParams().stream().map(p -> p.getParameterValue(context)).collect(Collectors.joining(", "));
        fn += parameters;
        fn += ");";
        return fn;
    }

    @Override
    public String mapVariables(List<CodeVariableBuilder> variables) {
        return variables.stream().filter(CodeVariableBuilder::isVariableDefNeeded)
                .distinct().map(this::variableToCode)
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String variableToCode(CodeVariableBuilder var) {
        String paramList;
        List<CodeParameter> params = var.getParams();
        if(var.isByAssignment()) {
            if(params.size() != 1) {
                throw new IllegalArgumentException("ByAssignment param list size must always be 1");
            }
            paramList = " = " + params.get(0).getParameterValue(context);
        }
        else {
            paramList = "(" + params.stream()
                    .map(p -> p.getParameterValue(context))
                    .collect(Collectors.joining(", ")) + ")";
        }

        if(var.isProgmem())
            return "const " + var.getType() + " PROGMEM " + var.getName() + paramList + ";";
        else
            return var.getType() + " " + var.getName() + paramList + ";";
    }

    @Override
    public String mapExports(List<CodeVariableBuilder> variables) {
        return variables.stream().filter(CodeVariableBuilder::isExported)
                .distinct()
                .map(this::exportToCode)
                .collect(Collectors.joining(LINE_BREAK));

    }

    private String exportToCode(CodeVariableBuilder exp) {
        return "extern " + (exp.isProgmem() ? "const " : "") + exp.getType() + " " + exp.getName() + ";";
    }

    @Override
    public String mapDefines() {
        return context.getProperties().stream()
                .filter(cp-> cp.getPropType() == PropType.USE_IN_DEFINE)
                .map(prop -> ("#define " + prop.getName() + " " + prop.getLatestValue()))
                .distinct()
                .collect(Collectors.joining(LINE_BREAK));
    }

    @Override
    public String mapIncludes(List<HeaderDefinition> includeList) {
        return includeList.stream()
                .distinct()
                .sorted(Comparator.comparingInt(HeaderDefinition::getPriority))
                .map(HeaderDefinition::getHeaderCode)
                .collect(Collectors.joining(LINE_BREAK));
    }

    @Override
    public String mapStructSource(BuildStructInitializer s) {
        if(s.isStringChoices()) {
            return doStringSource(s);
        }

        StringBuilder sb = new StringBuilder(256);
        if(s.isProgMemInfo()) {
            sb.append("const PROGMEM ").append(s.getStructType()).append(" minfo").append(s.getStructName()).append(" = { ");
            sb.append(String.join(", ", s.getStructElements()));
            sb.append(" };");
        }
        else {
            sb.append(s.getStructType()).append(" menu").append(s.getStructName()).append("(");
            sb.append(String.join(", ", s.getStructElements()));
            sb.append(");");
        }
        return sb.toString();

    }

    private String doStringSource(BuildStructInitializer s) {
        StringBuilder sb = new StringBuilder(256);
        IntStream.range(0, s.getStructElements().size()).forEach(i -> {
            String textRep = s.getStructElements().get(i);
            sb.append(String.format("const char enumStr%s_%d[] PROGMEM = %s;%s", s.getStructName(), i, textRep, LINE_BREAK));
        });
        sb.append(String.format("const char* const enumStr%s[] PROGMEM  = { ", s.getStructName()));
        sb.append(IntStream.range(0, s.getStructElements().size())
                .mapToObj(i -> "enumStr" + s.getStructName() + "_" + i)
                .collect(Collectors.joining(", ")));
        sb.append(" };");

        return sb.toString();
    }

    public String mapStructHeader(BuildStructInitializer s) {
        String header = "";

        if(s.isRequiresExtern()) {
            header = header + "extern " + s.getStructType() + " menu" + s.getStructName() + ";";
        }

        return header;
    }


}
