package com.vinctor.uatu

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.vinctor.Log
import com.vinctor.UatuConfig
import com.vinctor.UatuContext
import com.vinctor.classtree.ClassGraphVisitor
import com.vinctor.handler.PreStatisticsHanller
import com.vinctor.handler.TransformHandler
import com.vinctor.replace.ReplaceClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.http.util.TextUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class UatuTransform extends Transform {

    private Project project
    UatuConfig config;
    UatuContext context;

    UatuTransform(Project project) {
        this.project = project
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        this.config = project.UatuConfig;
        config.traceConfig = project.traceConfig;
        config.replaceConfig = project.replaceConfig;
        config.init()
        System.println(config.toString())
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        context = new UatuContext(config)

        new PreStatisticsHanller(transformInvocation).start();
        new TransformHandler(transformInvocation, config, context).start();

        /*hanlderPreParse(inputs)

        inputs.each { TransformInput input ->

            input.directoryInputs.each {
                DirectoryInput directoryInput ->

                    handlerFile(directoryInput)

                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                if (!jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    return
                }
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                File tmpFile = handlerJar(jarInput)

                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(tmpFile, dest)
                tmpFile.delete()
            }
        }*/
    }

    File handlerJar(JarInput jarInput) {
        JarFile jarFile = new JarFile(jarInput.file)
        println("jarInput:" + jarInput.file)
        Enumeration enumeration = jarFile.entries()

        File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            if (!checkFileName(entryName)) continue;
            println("entryName:" + entryName)
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream inputStream = jarFile.getInputStream(jarEntry)
            if (true) {
                jarOutputStream.putNextEntry(zipEntry)
                byte[] bytes = IOUtils.toByteArray(inputStream)

                byte[] code = handlerClass(bytes)

                jarOutputStream.write(code)
            } else {
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        jarFile.close()
        return tmpFile
    }

    private void handlerFile(DirectoryInput directoryInput) {
        if (directoryInput.file.isDirectory() && config.enable) {
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                if (checkFileName(name)) {

                    byte[] code = handlerClass(file.bytes)

                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }
    }

    boolean checkFileName(String name) {
        if (TextUtils.isEmpty(name))
            return false;
        return name.endsWith(".class") && !name.startsWith("R\$") &&
                !"R.class".equals(name) && !"BuildConfig.class".equals(name) && !name.endsWith("BuildConfig.class")
    }

    byte[] handlerClass(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        ClassVisitor upstreamCv = cw;
        Log.i("chain:" + upstreamCv)
        if (config.traceConfig != null && config.traceConfig.enable) {
            ClassVisitor traceCv = new UatuClassVisitor(upstreamCv, context)
            upstreamCv = traceCv;
        }
        Log.i("chain:" + upstreamCv)
        if (config.replaceConfig != null && config.replaceConfig.enable) {
            ClassVisitor replaceCv = new ReplaceClassVisitor(upstreamCv, context)
            upstreamCv = replaceCv
        }
        Log.i("chain:" + upstreamCv)
        cr.accept(upstreamCv, ClassReader.EXPAND_FRAMES)

        byte[] code = cw.toByteArray()
        return code
    }


    void hanlderPreParse(inputs) {
        inputs.each { TransformInput input ->
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    if (directoryInput.file.isDirectory()) {
                        directoryInput.file.eachFileRecurse { File file ->
                            def name = file.name
                            if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                    !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {
                                ClassReader cr = new ClassReader(file.bytes)
                                ClassGraphVisitor cv = new ClassGraphVisitor(Opcodes.ASM5);
                                cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                            }
                        }
                    }
            }


        }
    }
}
