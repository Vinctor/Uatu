package com.vinctor.replace;

import java.util.HashMap;

public class Constants {
    public static class Scope {
        public final static String SCOPE_SELF = "self";
        public final static String SCOPE_ALL = "all";
        public final static String SCOPE_LEAF = "leaf";

        public final static int SCOPE_SELF_TYPE = 1;
        public final static int SCOPE_ALL_TYPE = 2;
        public final static int SCOPE_LEAF_TYPE = 3;

        static HashMap<String, Integer> hashMap = new HashMap<>();

        static {
            hashMap.put(SCOPE_SELF, SCOPE_SELF_TYPE);
            hashMap.put(SCOPE_ALL, SCOPE_ALL_TYPE);
            hashMap.put(SCOPE_LEAF, SCOPE_LEAF_TYPE);
        }

        public static int getTypeInt(String type) {
            Integer integer = hashMap.get(type.toLowerCase());
            if (integer == null) {
                integer = SCOPE_SELF_TYPE;
            }
            return integer;
        }
    }
}
