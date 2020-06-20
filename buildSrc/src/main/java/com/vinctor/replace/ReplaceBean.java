package com.vinctor.replace;

import org.apache.http.util.TextUtils;

public class ReplaceBean {

    public String desc;
    private String scopeType;
    public MethodBean from;
    public MethodBean to;

    public String getScopeType() {
        if (TextUtils.isEmpty(scopeType)) {
            scopeType = Constants.Scope.SCOPE_SELF;
        }
        scopeType = scopeType.toLowerCase();
        return scopeType;
    }

    public int getScopeTypeInt() {
        String scopeType = getScopeType();
        return Constants.Scope.getTypeInt(scopeType);
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public static class MethodBean {
        public String className;
        public String methodName;
        public String methodDesc;
        public String method;

        public boolean validate() {
            return !TextUtils.isEmpty(className)
                    && !TextUtils.isEmpty(method)
                    && !method.contains("<init>");
        }


        @Override
        public String toString() {
            return "methodBean{" +
                    ", className='" + className + '\'' +
                    ", methodName='" + method + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReplaceBean{\n" +
                "desc='" + desc + '\'' +
                ", \n" + index + "-->from=" + from +
                ", \n" + (index++) + "-->to=" + to +
                "type='" + scopeType + '\'' +
                '}';
    }

    private static int index = 1;
}
