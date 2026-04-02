package com.vinctor.handler;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.google.common.io.Files;
import com.vinctor.log.Log;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public abstract class BaseHanlder {
    TransformInvocation transformInvocation;
    private Collection<TransformInput> inputs;
    private TransformOutputProvider outputProvider;
    protected boolean incremental = false;

    public BaseHanlder(TransformInvocation transformInvocation) {
        this.transformInvocation = transformInvocation;
        init();
    }

    private void init() {
        inputs = transformInvocation.getInputs();
        outputProvider = transformInvocation.getOutputProvider();
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public void start() {
        for (TransformInput input : inputs) {
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs();
            Collection<JarInput> jarInputs = input.getJarInputs();
            for (DirectoryInput directoryInput : directoryInputs) {
                try {
                    if (incremental) {
                        handleDirectoryInputIncremental(directoryInput);
                    } else {
                        hanlderDirectoryInput(directoryInput);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (JarInput jarInput : jarInputs) {
                try {
                    if (incremental) {
                        handleJarInputIncremental(jarInput);
                    } else {
                        hanlderJarInput(jarInput);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void hanlderDirectoryInput(DirectoryInput directoryInput) throws IOException {
        File fileDir = directoryInput.getFile();
        if (fileDir.isDirectory()) {
            Iterable<File> files = Files.fileTraverser().depthFirstPreOrder(fileDir);
            for (File f : files) {
                String fileName = f.getName();
                if (f.isFile() && checkFileName(fileName) && isEnable()) {
                    byte[] data = Files.toByteArray(f);
                    byte[] resultBytes = onHanlerFileInput(data);
                    FileOutputStream fos = new FileOutputStream(
                            f.getParentFile().getAbsolutePath() + File.separator + fileName);
                    fos.write(resultBytes);
                    fos.close();
                }
            }
        }
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        FileUtils.copyDirectory(directoryInput.getFile(), dest);
    }

    private void handleDirectoryInputIncremental(DirectoryInput directoryInput) throws IOException {
        File dest = outputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY
        );

        Map<File, Status> changedFiles = directoryInput.getChangedFiles();
        for (Map.Entry<File, Status> entry : changedFiles.entrySet()) {
            File inputFile = entry.getKey();
            Status status = entry.getValue();
            String relativePath = getRelativePath(directoryInput.getFile(), inputFile);
            File outputFile = new File(dest, relativePath);

            switch (status) {
                case NOTCHANGED:
                    break;
                case ADDED:
                case CHANGED:
                    if (inputFile.isFile()) {
                        if (checkFileName(inputFile.getName()) && isEnable()) {
                            byte[] data = Files.toByteArray(inputFile);
                            byte[] result = onHanlerFileInput(data);
                            outputFile.getParentFile().mkdirs();
                            Files.write(result, outputFile);
                        } else {
                            outputFile.getParentFile().mkdirs();
                            FileUtils.copyFile(inputFile, outputFile);
                        }
                    }
                    break;
                case REMOVED:
                    FileUtils.deleteQuietly(outputFile);
                    break;
            }
        }
    }

    private void hanlderJarInput(JarInput jarInput) throws IOException {
        if (!jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            return;
        }
        String jarName = jarInput.getName();
        String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4);
        }
        File tmpFile = handlerJar(jarInput);

        File dest = outputProvider.getContentLocation(jarName + md5Name,
                jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
        FileUtils.copyFile(tmpFile, dest);
        tmpFile.delete();
    }

    private void handleJarInputIncremental(JarInput jarInput) throws IOException {
        Status status = jarInput.getStatus();
        String jarName = jarInput.getName();
        String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4);
        }
        File dest = outputProvider.getContentLocation(
                jarName + md5Name,
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR
        );

        switch (status) {
            case NOTCHANGED:
                break;
            case ADDED:
            case CHANGED:
                File tmpFile = handlerJar(jarInput);
                FileUtils.copyFile(tmpFile, dest);
                tmpFile.delete();
                break;
            case REMOVED:
                FileUtils.deleteQuietly(dest);
                break;
        }
    }

    File handlerJar(JarInput jarInput) throws IOException {
        File originFile = jarInput.getFile();
        if (!isEnable()) {
            return originFile;
        }
        JarFile jarFile = new JarFile(originFile);
        Log.i("jarInput:" + originFile);
        Enumeration enumeration = jarFile.entries();

        File tmpFile = new File(originFile.getParent() + File.separator + "classes_temp.jar");
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            String entryName = jarEntry.getName();
            if (!checkFileName(entryName)) continue;
            ZipEntry zipEntry = new ZipEntry(entryName);
            InputStream inputStream = jarFile.getInputStream(jarEntry);
            if (true) {
                jarOutputStream.putNextEntry(zipEntry);
                byte[] bytes = IOUtils.toByteArray(inputStream);

                byte[] code = onHanlerJarInput(bytes);

                jarOutputStream.write(code);
            } else {
                jarOutputStream.putNextEntry(zipEntry);
                jarOutputStream.write(IOUtils.toByteArray(inputStream));
            }
            jarOutputStream.closeEntry();
        }
        jarOutputStream.close();
        jarFile.close();
        return tmpFile;
    }

    private String getRelativePath(File baseDir, File file) {
        return baseDir.toURI().relativize(file.toURI()).getPath();
    }

    protected boolean checkFileName(String name) {
        if (TextUtils.isEmpty(name))
            return false;

        return name.endsWith(".class");
    }

    protected boolean isEnable() {
        return true;
    }

    abstract byte[] onHanlerFileInput(byte[] bytes);

    abstract byte[] onHanlerJarInput(byte[] bytes);
}
