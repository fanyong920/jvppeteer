package com.ruiyun.example;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    // --- 配置区域 ---
    private static final String SOURCE_PATH = "C:\\Users\\fanyong\\Pictures\\iCloud Photos\\Photos";
    private static final String PHOTO_TARGET_DIR = "C:\\Users\\fanyong\\Desktop\\Test";
    private static final String VIDEO_TARGET_DIR = "C:\\Users\\fanyong\\Desktop\\2023-02";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 定义图片和视频的扩展名
    private static final Set<String> PHOTO_EXTENSIONS = Stream.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp").collect(Collectors.toSet());
    private static final Set<String> VIDEO_EXTENSIONS = Stream.of("mp4", "mov", "avi", "mkv", "wmv", "flv", "webm", "m4v").collect(Collectors.toSet());


    public static void main(String[] args) {
        try {
            new Test().organize();
        } catch (IOException e) {
            System.err.println("程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主要组织逻辑
     */
    public void organize() throws IOException {
        Path sourceDir = Paths.get(SOURCE_PATH);
        if (!Files.exists(sourceDir)) {
            throw new IOException("源文件夹不存在: " + SOURCE_PATH);
        }

        // 创建目标文件夹
        Files.createDirectories(Paths.get(PHOTO_TARGET_DIR));
        Files.createDirectories(Paths.get(VIDEO_TARGET_DIR));

        // 用于跟踪每天的序号
        Map<String, AtomicInteger> photoCounterMap = new HashMap<>();
        Map<String, AtomicInteger> videoCounterMap = new HashMap<>();

        // 遍历源文件夹及其子文件夹
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString().toLowerCase();
                String extension = getFileExtension(fileName);

                if (PHOTO_EXTENSIONS.contains(extension)) {
                    processFile(file, PHOTO_TARGET_DIR, photoCounterMap);
                } else if (VIDEO_EXTENSIONS.contains(extension)) {
                    processFile(file, VIDEO_TARGET_DIR, videoCounterMap);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("文件整理完成！");
    }

    /**
     * 处理单个文件
     */
    private void processFile(Path sourceFile, String targetBaseDir, Map<String, AtomicInteger> counterMap) throws IOException {
        String dateStr = extractDateFromMetadataOrLastModified(sourceFile);
        String dayKey = dateStr; // 使用日期字符串作为map的key

        // 获取并更新当天的计数器
        AtomicInteger counter = counterMap.computeIfAbsent(dayKey, k -> new AtomicInteger(1));
        int currentCount;

        Path targetPath;
        do {
            currentCount = counter.getAndIncrement();
            String newFileName = String.format("%s-%02d", dateStr, currentCount);
            targetPath = Paths.get(targetBaseDir).resolve(newFileName + "." + getFileExtension(sourceFile.getFileName().toString()));

            // 如果文件已存在，则循环继续，计数器已由getAndIncrement()更新
        } while (Files.exists(targetPath));

        // 移动文件
        Files.move(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.printf("移动文件: %s -> %s%n", sourceFile.getFileName(), targetPath.getFileName());
    }

    /**
     * 提取文件的日期，优先从元数据，否则使用最后修改时间
     */
    private String extractDateFromMetadataOrLastModified(Path file) throws IOException {
        String extension = getFileExtension(file.getFileName().toString()).toLowerCase();

        // 尝试从图片元数据中提取日期
        if (PHOTO_EXTENSIONS.contains(extension)) {
            try {
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(file.toFile());
                if (imageInputStream != null) {
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        reader.setInput(imageInputStream);
                        IIOMetadata metadata = reader.getImageMetadata(0);
                        if (metadata != null) {
                            String dateTimeOriginal = null;
                            try {
                                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_jpeg_image_1.0");
                                if (root != null) {
                                    // 遍历查找包含 DateTime 的节点
                                    for (int i = 0; i < root.getLength(); i++) {
                                        org.w3c.dom.Node node = root.item(i);
                                        String nodeName = node.getNodeName();
                                        if (nodeName != null && nodeName.contains("DateTime")) {
                                            dateTimeOriginal = node.getTextContent();
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to extract DateTime from image metadata");
                            }

                            if (dateTimeOriginal != null && !dateTimeOriginal.isEmpty()) {
                                // 假设元数据格式为 "YYYY:MM:dd HH:mm:ss"
                                return LocalDate.parse(dateTimeOriginal.substring(0, 10).replace(":", "-"), DateTimeFormatter.ofPattern("yyyy-M-d")).format(DATE_FORMATTER);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 读取元数据失败，忽略异常，后续会使用修改时间
            }
        }

        // 对于视频或无法读取元数据的图片，使用最后修改时间
        long lastModifiedTimeMillis = Files.getLastModifiedTime(file).toMillis();
        Instant instant = Instant.ofEpochMilli(lastModifiedTimeMillis);
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DATE_FORMATTER);
    }


    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex + 1) : "";
    }
}
