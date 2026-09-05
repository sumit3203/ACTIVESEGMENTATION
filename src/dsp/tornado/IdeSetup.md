# TornadoVM IDE Setup and Configuration

This guide explains how to configure a local development environment for running TornadoVM applications from an IDE such as **IntelliJ IDEA** or **Eclipse**.

The TornadoVM launcher automatically provides the JVM and module configuration required to run TornadoVM applications. However, when launching an application directly from an IDE, these arguments may need to be provided manually.

## Prerequisites

Install:

- JDK 21
- TornadoVM
- JavaFX SDK, if required
- Fiji/ImageJ, if required

Add JDK and TornadoVM to the system `PATH`.

Verify the installation:

```bash
java --version
tornado --version
```

To display the required JVM arguments, run:

```bash
tornado --printJavaFlags
```

## IDE Configuration

The TornadoVM launcher adds the required JVM arguments automatically. When running from an IDE, add them manually.

### IntelliJ IDEA

Go to:

```text
Run > Edit Configurations > VM Options
```

### Eclipse

Go to:

```text
Run > Run Configurations > Arguments > VM Arguments
```

Add the following arguments:

```text
--enable-preview
--module-path "<TORNADOVM_HOME>/share/java/tornado"
--add-modules ALL-SYSTEM,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.nio=ALL-UNNAMED
--add-opens java.base/jdk.internal.misc=ALL-UNNAMED
--upgrade-module-path "<TORNADOVM_HOME>/share/java/graalJars"
@<TORNADOVM_HOME>/etc/exportLists/common-exports
@<TORNADOVM_HOME>/etc/exportLists/opencl-exports
-server
-XX:+UnlockExperimentalVMOptions
-XX:+EnableJVMCI
-XX:+UseParallelGC
-Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph
-Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime
-Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado
-Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor
-Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel
-Djava.library.path="<TORNADOVM_HOME>/lib;<JAVAFX_HOME>/bin"
-Dplugins.dir="<FIJI_HOME>/plugins"
```

Replace:

- `<TORNADOVM_HOME>` with the TornadoVM installation path
- `<JAVAFX_HOME>` with the JavaFX installation path
- `<FIJI_HOME>` with the Fiji/ImageJ installation path

For example:

```text
<TORNADOVM_HOME>
D:\DEV\tornadoVM\tornadovm-2.2.0-opencl
```

## Important

Ensure that TornadoVM JARs are included in the `--module-path`.

Do not add the same TornadoVM JARs to the classpath, as this may cause module or class-loading errors.

If the application does not run, compare the IDE VM arguments with:

```bash
tornado --printJavaFlags
```

