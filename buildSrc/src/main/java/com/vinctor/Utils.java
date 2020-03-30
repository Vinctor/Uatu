package com.vinctor;

import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class Utils {
    public static String transformClassName(String className) {
        return className.trim().replace('.', '/');
    }

    public static String readJsonFile(File jsonFile) {
        String jsonStr = "";
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Type getBasicType(String item) {
        switch (item) {
            case "boolean":
                return Type.BOOLEAN_TYPE;
            case "char":
                return Type.CHAR_TYPE;
            case "byte":
                return Type.BYTE_TYPE;
            case "short":
                return Type.SHORT_TYPE;
            case "int":
                return Type.INT_TYPE;
            case "float":
                return Type.FLOAT_TYPE;
            case "long":
                return Type.LONG_TYPE;
            case "double":
                return Type.DOUBLE_TYPE;
            default:
                return null;
        }
    }

    public static Type getType(String item) {
        Type basicType = getBasicType(item);
        if (basicType != null) {
            return basicType;
        } else {
            return Type.getObjectType(item);
        }
    }
}
