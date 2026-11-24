package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class VerifyCallerTask extends Task {

    private File jarFile;
    private String className;
    private String expectedPackage;
    private String forbiddenPackage;

    public VerifyCallerTask() {
    }

    public VerifyCallerTask(String className, String expectedPackage, String forbiddenPackage) {
        this.className = className;
        this.expectedPackage = expectedPackage;
        this.forbiddenPackage = forbiddenPackage;
    }

    public void setJar(File jarFile) {
        this.jarFile = jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public void setFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public File getJarFile() {
        return jarFile;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setCallerClass(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setExpectedPackage(String expectedPackage) {
        this.expectedPackage = expectedPackage;
    }

    public String getExpectedPackage() {
        return expectedPackage;
    }

    public void setForbiddenPackage(String forbiddenPackage) {
        this.forbiddenPackage = forbiddenPackage;
    }

    public String getForbiddenPackage() {
        return forbiddenPackage;
    }

    @Override
    public void execute() throws BuildException {
        if (jarFile == null) {
            jarFile = getProject() != null ? getProject().resolveFile("deployment/result.jar") : new File("deployment/result.jar");
        }

        if (!jarFile.exists()) {
            throw new BuildException("JAR file not found: " + jarFile.getAbsolutePath());
        }

        if (className == null || className.trim().isEmpty()) {
            throw new BuildException("className attribute must be specified");
        }

        String classEntryPath = className.replace('.', '/') + ".class";
        log("Verifying bytecode references in class " + className + " from " + jarFile.getPath());

        Set<String> referencedClasses = extractReferencedClasses(jarFile, classEntryPath);

        log("Referenced classes found in bytecode: " + referencedClasses);

        Set<String> expectedMatches = new HashSet<>();
        Set<String> forbiddenMatches = new HashSet<>();

        for (String ref : referencedClasses) {
            if (expectedPackage != null && !expectedPackage.isEmpty() && ref.startsWith(expectedPackage)) {
                expectedMatches.add(ref);
            }
            if (forbiddenPackage != null && !forbiddenPackage.isEmpty() && ref.startsWith(forbiddenPackage)) {
                forbiddenMatches.add(ref);
            }
        }

        if (!forbiddenMatches.isEmpty()) {
            throw new BuildException("Verification failed: " + className + " in " + jarFile.getName()
                    + " still statically references forbidden package '" + forbiddenPackage + "': " + forbiddenMatches);
        }

        if (expectedPackage != null && !expectedPackage.isEmpty() && expectedMatches.isEmpty()) {
            throw new BuildException("Verification failed: " + className + " in " + jarFile.getName()
                    + " does not statically reference expected package '" + expectedPackage + "'");
        }

        log("Static bytecode verification SUCCESSFUL: " + className + " uses repackaged classes " + expectedMatches);
    }

    public static Set<String> extractReferencedClasses(File jar, String classEntryName) throws BuildException {
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry(classEntryName);
            if (entry == null) {
                // Try case or fallback matching
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry e = entries.nextElement();
                    if (e.getName().equalsIgnoreCase(classEntryName)) {
                        entry = e;
                        break;
                    }
                }
            }

            if (entry == null) {
                throw new BuildException("Class entry " + classEntryName + " was not found in " + jar.getAbsolutePath());
            }

            try (InputStream in = jarFile.getInputStream(entry)) {
                return extractReferencedClasses(in);
            }
        } catch (BuildException be) {
            throw be;
        } catch (Exception e) {
            throw new BuildException("Failed to read bytecode from " + jar.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public static Set<String> extractReferencedClasses(InputStream classStream) throws Exception {
        ClassReader reader = new ClassReader(classStream);
        ReferenceCollectorVisitor visitor = new ReferenceCollectorVisitor();
        reader.accept(visitor, 0);
        return visitor.getReferencedClasses();
    }

    private static class ReferenceCollectorVisitor extends ClassVisitor {
        private final Set<String> referencedClasses = new HashSet<>();

        public ReferenceCollectorVisitor() {
            super(Opcodes.ASM9);
        }

        public Set<String> getReferencedClasses() {
            return referencedClasses;
        }

        private void addType(Type type) {
            if (type == null) {
                return;
            }
            if (type.getSort() == Type.ARRAY) {
                addType(type.getElementType());
            } else if (type.getSort() == Type.OBJECT) {
                referencedClasses.add(type.getClassName());
            } else if (type.getSort() == Type.METHOD) {
                for (Type arg : type.getArgumentTypes()) {
                    addType(arg);
                }
                addType(type.getReturnType());
            }
        }

        private void addInternalName(String internalName) {
            if (internalName != null && !internalName.isEmpty()) {
                if (internalName.startsWith("[")) {
                    addDescriptor(internalName);
                } else {
                    referencedClasses.add(internalName.replace('/', '.'));
                }
            }
        }

        private void addDescriptor(String desc) {
            if (desc != null && !desc.isEmpty()) {
                try {
                    if (desc.startsWith("(")) {
                        addType(Type.getMethodType(desc));
                    } else {
                        addType(Type.getType(desc));
                    }
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            addInternalName(superName);
            if (interfaces != null) {
                for (String iface : interfaces) {
                    addInternalName(iface);
                }
            }
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            addDescriptor(descriptor);
            return new FieldVisitor(Opcodes.ASM9, super.visitField(access, name, descriptor, signature, value)) {
                @Override
                public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    addDescriptor(descriptor);
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public org.objectweb.asm.AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
                    addDescriptor(descriptor);
                    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            addDescriptor(descriptor);
            if (exceptions != null) {
                for (String exc : exceptions) {
                    addInternalName(exc);
                }
            }
            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    addInternalName(type);
                    super.visitTypeInsn(opcode, type);
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    addInternalName(owner);
                    addDescriptor(descriptor);
                    super.visitFieldInsn(opcode, owner, name, descriptor);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    addInternalName(owner);
                    addDescriptor(descriptor);
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    addDescriptor(descriptor);
                    if (bootstrapMethodHandle != null) {
                        addInternalName(bootstrapMethodHandle.getOwner());
                        addDescriptor(bootstrapMethodHandle.getDesc());
                    }
                    if (bootstrapMethodArguments != null) {
                        for (Object arg : bootstrapMethodArguments) {
                            if (arg instanceof Type) {
                                addType((Type) arg);
                            } else if (arg instanceof Handle) {
                                Handle h = (Handle) arg;
                                addInternalName(h.getOwner());
                                addDescriptor(h.getDesc());
                            }
                        }
                    }
                    super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                }

                @Override
                public void visitLdcInsn(Object value) {
                    if (value instanceof Type) {
                        addType((Type) value);
                    }
                    super.visitLdcInsn(value);
                }

                @Override
                public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                    addDescriptor(descriptor);
                    super.visitMultiANewArrayInsn(descriptor, numDimensions);
                }

                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    addInternalName(type);
                    super.visitTryCatchBlock(start, end, handler, type);
                }

                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                    addDescriptor(descriptor);
                    super.visitLocalVariable(name, descriptor, signature, start, end, index);
                }
            };
        }
    }
}
