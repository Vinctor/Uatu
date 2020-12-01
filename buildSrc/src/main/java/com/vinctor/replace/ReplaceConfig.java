package com.vinctor.replace;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vinctor.log.Log;
import com.vinctor.Utils;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReplaceConfig {
    boolean enable = false;
    public boolean jarEnable = false;
    String configFile;


    final static String TAG = ReplaceConfig.class.getSimpleName();
    private final Project project;
    private List<ReplaceBean> replaceList;
    private List<String> needToExclude = new ArrayList<>();

    public ReplaceConfig(Project project) {
        this.project = project;
    }

    public boolean isEnable() {
        return enable && !TextUtils.isEmpty(configFile) && replaceList != null && replaceList.size() != 0;
    }

    public void init(boolean enable) {
        if (!enable) {
            this.enable = false;
            return;
        }
        File projectDir = project.getProjectDir();
        File configFile = new File(projectDir, this.configFile);
        Log.i(TAG, configFile.getAbsolutePath());

        boolean exists = configFile.exists();
        boolean canRead = configFile.canRead();
        if (exists && canRead) {
            hanldConfigFile(configFile);
        } else {
            this.enable = false;
        }
        if (!exists) {
            Log.e(TAG, configFile.getAbsolutePath() + " file isn't exist!");
        }
        if (!canRead) {
            Log.e(TAG, configFile.getAbsolutePath() + " file can't read!");
        }
    }

    private void hanldConfigFile(File configFile) {
        String jsonString = Utils.readJsonFile(configFile);
        if (TextUtils.isEmpty(jsonString)) {
            this.enable = false;
            Log.e(TAG, configFile.getAbsolutePath() + " read fail!");
            return;
        }
        try {
            replaceList = new Gson().fromJson(jsonString, new TypeToken<List<ReplaceBean>>() {
            }.getType());
            if (replaceList == null || replaceList.size() == 0) {
                this.enable = false;
                Log.e(TAG, configFile.getAbsolutePath() + " data is empty!");
                return;
            }
            needToExclude.clear();
            Iterator<ReplaceBean> iterator = replaceList.iterator();
            while (iterator.hasNext()) {
                ReplaceBean bean = iterator.next();
                if (bean == null) {
                    iterator.remove();
                    continue;
                }
                ReplaceBean.MethodBean from = bean.from;
                ReplaceBean.MethodBean to = bean.to;
                if (from == null || to == null) {
                    Log.e(TAG, bean.desc + "，from or to is null,remove!");
                    iterator.remove();
                    continue;
                }
                if (!from.validate() || !to.validate()) {
                    Log.e(TAG, bean.desc + "，data is error，remove!");
                    iterator.remove();
                    continue;
                }
                from.className = Utils.transformClassName(from.className);
                to.className = Utils.transformClassName(to.className);
                Method fromMethod = Method.getMethod(from.method);
                Method toMethod = Method.getMethod(to.method);
                from.methodName = fromMethod.getName();
                from.methodDesc = fromMethod.getDescriptor();
                to.methodName = toMethod.getName();
                to.methodDesc = toMethod.getDescriptor();
                try {
                    Type fromMethodType = Type.getMethodType(from.methodDesc);
                    Type toMethodType = Type.getMethodType(to.methodDesc);
                    int fromArgsLength = fromMethodType.getArgumentTypes().length;
                    int toArgsLength = toMethodType.getArgumentTypes().length;
                    if (fromArgsLength != toArgsLength && fromArgsLength + 1 != toArgsLength) {
                        iterator.remove();
                        Log.e(TAG, bean.desc + "method desc is error，remove,from:" + fromArgsLength + ",to:" + toArgsLength);
                        continue;
                    }
                } catch (Exception e) {
                    Log.e(TAG, configFile.getAbsolutePath() + ",parse json fail!");
                    e.printStackTrace();
                    iterator.remove();
                    continue;
                }

                needToExclude.add(to.className);
            }
        } catch (Exception e) {
            this.enable = false;
            Log.e(TAG, configFile.getAbsolutePath() + ",parse json fail!");
            e.printStackTrace();
            throw e;
        }
    }


    public List<String> getNeedToExclude() {
        return needToExclude;
    }

    public List<ReplaceBean> getReplaceList() {
        return replaceList;
    }

    public boolean isJarEnable() {
        return jarEnable;
    }

    @Override
    public String toString() {
        return "ReplaceConfig{" +
                "enable=" + enable +
                ", jarEnable=" + jarEnable +
                ", configFile='" + configFile + '\'' +
                ", replaceList=" + replaceList +
                ", needToExclude=" + needToExclude +
                '}';
    }
}
