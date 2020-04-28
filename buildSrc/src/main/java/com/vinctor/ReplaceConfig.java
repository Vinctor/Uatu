package com.vinctor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vinctor.replace.ReplaceBean;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;
import org.objectweb.asm.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReplaceConfig {
    boolean enable = false;
    public boolean jarEnable = true;
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
            Log.e(TAG, configFile.getAbsolutePath() + "文件不存在");
        }
        if (!canRead) {
            Log.e(TAG, configFile.getAbsolutePath() + "文件不可读");
        }
    }

    private void hanldConfigFile(File configFile) {
        String jsonString = Utils.readJsonFile(configFile);
        if (TextUtils.isEmpty(jsonString)) {
            this.enable = false;
            Log.e(TAG, configFile.getAbsolutePath() + "读取失败");
            return;
        }
        try {
            replaceList = new Gson().fromJson(jsonString, new TypeToken<List<ReplaceBean>>() {
            }.getType());
            if (replaceList == null || replaceList.size() == 0) {
                this.enable = false;
                Log.e(TAG, configFile.getAbsolutePath() + "无转换数据");
                return;
            }
            Iterator<ReplaceBean> iterator = replaceList.iterator();
            while (iterator.hasNext()) {
                ReplaceBean bean = iterator.next();
                if (bean == null) {
                    iterator.remove();
                    continue;
                }
                ReplaceBean.methodBean from = bean.from;
                ReplaceBean.methodBean to = bean.to;
                if (from == null || to == null) {
                    Log.e(TAG, bean.desc + "，from或to为null，已移除");
                    iterator.remove();
                    continue;
                }
                if (!from.validate() || !to.validate()) {
                    Log.e(TAG, bean.desc + "，数据格式错误，已移除");
                    iterator.remove();
                    continue;
                }
                from.className = Utils.transformClassName(from.className);
                to.className = Utils.transformClassName(to.className);
                from.methodDesc = Utils.transformClassName(from.methodDesc);
                to.methodDesc = Utils.transformClassName(to.methodDesc);
                try {
                    Type fromMethodType = Type.getMethodType(from.methodDesc);
                    Type toMethodType = Type.getMethodType(to.methodDesc);
                    int fromArgsLength = fromMethodType.getArgumentTypes().length;
                    int toArgsLength = toMethodType.getArgumentTypes().length;
                    if (fromArgsLength != toArgsLength && fromArgsLength + 1 != toArgsLength) {
                        iterator.remove();
                        Log.e(TAG, bean.desc + "方法描述不正确，已移除,from:" + fromArgsLength + ",to:" + toArgsLength);
                        continue;
                    }
                } catch (Exception e) {
                    Log.e(TAG, configFile.getAbsolutePath() + ",转换json失败");
                    e.printStackTrace();
                    iterator.remove();
                    continue;
                }

                needToExclude.clear();
                needToExclude.add(to.className);
            }
        } catch (Exception e) {
            this.enable = false;
            Log.e(TAG, configFile.getAbsolutePath() + ",转换json失败");
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
                ", configFile='" + configFile + '\'' +
                ", replaceList=" + replaceList +
                '}';
    }
}
