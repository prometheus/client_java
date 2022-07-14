package io.prometheus.client.hotspot;

/**
 * Contains utilities to check if we are running inside or building for native image. Default behavior is to check
 * if specific for graalvm runtime property is present. For additional optimizations it is possible to do add
 * "--initialize-at-build-time=io.prometheus.client.hotspot.NativeImageChecker" to graalvm native image build command and
 * the native image will be identified during build time.
 */
public final class NativeImageChecker {
    static final boolean isGraalVmNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private NativeImageChecker() {}
}
