package io.prometheus.client.it.common;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Temporary directory in ./target/ to be mounted as a volume in Docker containers.
 */
public class Volume {

    private final Path tmpDir; // will be created in the ./target/ directory

    private Volume(Path tmpDir) {
        this.tmpDir = tmpDir;
    }

    public static Volume create(String prefix) throws IOException, URISyntaxException {
        Path targetDir = Paths.get(Volume.class.getResource("/").toURI()).getParent();
        Assert.assertEquals("failed to locate target/ directory", "target", targetDir.getFileName().toString());
        return new Volume(Files.createTempDirectory(targetDir, prefix + "-"));
    }

    /**
     * Copy a file or directory to this volume.
     * @param src is relative to {@code ./target/}
     */
    public Volume copy(String src) throws IOException {
        Path srcPath = tmpDir.getParent().resolve(src);
        if (Files.isRegularFile(srcPath)) {
            Files.copy(srcPath, tmpDir.resolve(srcPath.getFileName()), REPLACE_EXISTING);
        } else if (Files.isDirectory(srcPath)) {
            Path dest = tmpDir.resolve(srcPath.getFileName());
            Files.createDirectories(dest);
            Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {

                // create parent directories
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(dest.resolve(srcPath.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                // copy file
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, dest.resolve(srcPath.relativize(file)), REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Assert.fail(src + ": No such file or directory");
        }
        return this;
    }

    /**
     * Remove files in tmpDir if they match the predicate.
     */
    public void rm(Predicate<Path> predicate) throws IOException {
        Files.walkFileTree(tmpDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (predicate.test(file)) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public String getHostPath() {
        return tmpDir.toString();
    }

    /**
     * Recursively remove tmpDir and its contents.
     */
    public void remove() throws IOException {
        if (!deleteRecursively(tmpDir.toFile())) {
            throw new IOException(tmpDir + ": Failed to remove temporary test directory.");
        }
    }

    private boolean deleteRecursively(File file) {
        File[] allContents = file.listFiles();
        if (allContents != null) {
            for (File child : allContents) {
                deleteRecursively(child);
            }
        }
        return file.delete();
    }
}
