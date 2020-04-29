package com.vinctor.replace;

import org.apache.http.util.TextUtils;

public class ReplaceBean {

    public String desc;
    public methodBean from;
    public methodBean to;

    public static class methodBean {
        private String scopeType;
        public String className;
        public String methodName;
        public String methodDesc;

        public boolean validate() {
            return !TextUtils.isEmpty(className)
                    && !TextUtils.isEmpty(methodName)
                    && !TextUtils.isEmpty(methodDesc)
                    && !"<init>".equals(methodName);
        }

        public String getScopeType() {
            if (TextUtils.isEmpty(scopeType)) {
                scopeType = Constants.Scope.SCOPE_SELF;
            }
            scopeType = scopeType.toLowerCase();
            return scopeType;
        }

        public void setScopeType(String scopeType) {
            this.scopeType = scopeType;
        }

        public int getScopeTypeInt() {
            String scopeType = getScopeType();
            return Constants.Scope.getTypeInt(scopeType);
        }

        @Override
        public String toString() {
            return "methodBean{" +
                    "type='" + scopeType + '\'' +
                    ", className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", methodDesc='" + methodDesc + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReplaceBean{\n" +
                "desc='" + desc + '\'' +
                ", \n" + index + "-->from=" + from +
                ", \n" + (index++) + "-->to=" + to +
                '}';
    }

    private static int index = 1;
}
