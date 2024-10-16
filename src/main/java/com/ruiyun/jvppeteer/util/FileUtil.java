package com.ruiyun.jvppeteer.util;

import com.ruiyun.jvppeteer.exception.JvppeteerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 操作文件的一些公告方法
 */
public class FileUtil {

    /**
     * 根据给定的前缀创建临时文件夹
     *
     * @param prefix 临时文件夹前缀
     * @return 临时文件夹路径
     */
    public static String createProfileDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toRealPath().toString();
        } catch (Exception e) {
            throw new JvppeteerException("Create temp profile dir fail:", e);
        }

    }

    /**
     * 断言路径是否是可执行的exe文件
     *
     * @param executablePath 要断言的文件
     * @return 可执行，返回true
     */
    public static boolean assertExecutable(String executablePath) {
        Path path = Paths.get(executablePath);
        return Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path);
    }

    /**
     * 程序退出时删除文件
     *
     * @param path 要移除的路径
     * @throws IOException 异常
     */
    public static void removeFolderOnExit(String path) throws IOException {
        Path userDirPath = Paths.get(path);
        if (Files.exists(userDirPath)) {
            try (Stream<Path> paths = Files.walk(userDirPath)) {
                // 确保先删除子目录中的文件和子目录
                paths .map(Path::toFile).forEach(File::deleteOnExit);
            }
            userDirPath.toFile().deleteOnExit();
        }
    }

    /**
     * 移除文件
     *
     * @param path 要移除的路径
     * @throws IOException 异常
     */
    public static void removeFolder(String path) throws IOException {
        Path userDirPath = Paths.get(path);
        if (Files.exists(userDirPath)) {
            try (Stream<Path> paths = Files.walk(userDirPath)) {
                paths.sorted(Comparator.reverseOrder())  // 确保先删除子目录中的文件和子目录
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }


    /**
     * 创建一个文件，如果该文件上的有些文件夹路径不存在，会自动创建文件夹。
     *
     * @param path 创建的文件
     * @throws IOException 异常
     */
    public static void createNewFile(String path) throws IOException {
        Path path1 = Paths.get(path);
        if (!Files.exists(path1)) {
            Set<PosixFilePermission> rwxrwxrwx = PosixFilePermissions.fromString("rwxrwxrwx");
            Path parent = path1.getParent();
            if (parent != null && !Files.exists(parent)) {
                if (Helper.isMac() || Helper.isLinux()) {
                    Files.createDirectories(path1.getParent(), PosixFilePermissions.asFileAttribute(rwxrwxrwx));
                } else if (Helper.isWindows()) {
                    Files.createDirectories(path1.getParent());
                }
            }
            if (Helper.isMac() || Helper.isLinux()) {
                Files.createFile(path1, PosixFilePermissions.asFileAttribute(rwxrwxrwx));
            } else if (Helper.isWindows()) {
                Files.createFile(path1);
            }
        }
    }

}
