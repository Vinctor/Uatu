package com.vinctor.uatu

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.vinctor.Log
import com.vinctor.UatuConfig
import com.vinctor.replace.ReplaceClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class UatuTransform extends Transform {

    private Project project
    UatuConfig config;

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
            outputProvider.deleteAll();
        }

        inputs.each { TransformInput input ->

            input.directoryInputs.each {
                DirectoryInput directoryInput ->

                    handlerFile(directoryInput, outputProvider)

                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }


                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    private void handlerFile(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        if (directoryInput.file.isDirectory() && config.enable) {
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                if (name.endsWith(".class") && !name.startsWith("R\$") &&
                        !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {

                    ClassReader cr = new ClassReader(file.bytes)
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor upstreamCv = cw;
                    Log.i("chain:" + upstreamCv)
                    if (config.traceConfig != null && config.traceConfig.enable) {
                        ClassVisitor traceCv = new UatuClassVisitor(upstreamCv, config.traceConfig)
                        upstreamCv = traceCv;
                    }
                    Log.i("chain:" + upstreamCv)
                    if (config.replaceConfig != null && config.replaceConfig.enable) {
                        ClassVisitor replaceCv = new ReplaceClassVisitor(upstreamCv, config.replaceConfig)
                        upstreamCv = replaceCv
                    }
                    Log.i("chain:" + upstreamCv)
                    cr.accept(upstreamCv, ClassReader.EXPAND_FRAMES)

                    byte[] code = cw.toByteArray()

                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }
    }
}
