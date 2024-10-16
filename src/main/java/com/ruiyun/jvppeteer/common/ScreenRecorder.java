package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.entities.ScreenCastFormat;
import com.ruiyun.jvppeteer.entities.ScreenRecorderOptions;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.events.ScreencastFrameEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.ruiyun.jvppeteer.entities.ScreenCastFormat.GIF;
import static com.ruiyun.jvppeteer.entities.ScreenCastFormat.WEBM;

public class ScreenRecorder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenRecorder.class);
    private static final int DEFAULT_FPS = 30;
    private static final int CRF_VALUE = 30;
    private static final String SCREENCAST_TEMP_CAAHE_DIR = "jvppeteer-screencast-temp-cache-";
    private final Page page;
    private final double width;
    private final double height;
    private final ScreenRecorderOptions options;
    private final Viewport defaultViewport;
    private final Viewport tempViewport;
    private String tempCacheDir;
    AtomicLong imgIndex = new AtomicLong(0);

    public ScreenRecorder(Page page, double width, double height, ScreenRecorderOptions options, Viewport defaultViewport, Viewport tempViewport) {
        this.page = page;
        this.options = options;
        this.width = width;
        this.height = height;
        this.defaultViewport = defaultViewport;
        this.tempViewport = tempViewport;
        createTempCacheDir();

        Consumer<Object> closeListener = (o) -> {
            ScreenRecorder.this.stop();
        };
        page.mainFrame().client().once(CDPSession.CDPSessionEvent.disconnected, closeListener);
        page.mainFrame().client().on(CDPSession.CDPSessionEvent.Page_screencastFrame, (Consumer<ScreencastFrameEvent>) this::writeFrame);
    }

    private void createTempCacheDir() {
        this.tempCacheDir = FileUtil.createProfileDir(SCREENCAST_TEMP_CAAHE_DIR);
    }

    private void writeFrame(ScreencastFrameEvent event) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("sessionId", event.getSessionId());
        page.mainFrame().client().send("Page.screencastFrameAck", params);
        if (event.getMetadata().getTimestamp() != null && event.getMetadata().getTimestamp() != 0) {
            byte[] buffer = Base64Util.decode(event.getData().getBytes());
            try {
                Path imgPath = Paths.get(Helper.join(this.tempCacheDir, imgIndex.incrementAndGet() + ".png"));
                Files.write(imgPath, buffer, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
    }

    private List<String> getFormatArgs(ScreenCastFormat format) {
        List<String> args = new ArrayList<>();
        switch (format) {
            case WEBM:
                args.add("-c:v");
                args.add("vp9");
                args.add("-f");
                args.add("webm");
                args.add("-crf");
                args.add(String.valueOf(CRF_VALUE));
                args.add("-deadline");
                args.add("realtime");
                args.add("-cpu-used");
                args.add("8");
                return args;
            case GIF:
                args.add("-f");
                args.add("gif");
                return args;
        }
        return args;
    }

    /**
     * 停止屏幕录制
     */
    public void stop() {
        //先暂停屏幕录制
        try {
            this.page.stopScreencast();
            convert();
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ", e);
        } finally {
            if (Objects.nonNull(defaultViewport) && Objects.nonNull(tempViewport)) {
                this.page.setViewport(defaultViewport);
            }
            try {
                FileUtil.removeFolderOnExit(this.tempCacheDir);
            } catch (IOException ignored) {

            }
        }
    }

    /**
     * 执行ffmpeg将png图片转换成webm或gif
     *
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for the process to complete.
     */
    private void convert() throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>();
        if (StringUtil.isEmpty(this.options.getFfmpegPath())) {
            this.options.setFfmpegPath("ffmpeg");
        }
        commands.add(this.options.getFfmpegPath());
        commands.add("-loglevel");
        commands.add("error");
        // 设置 I/O 为直接模式。这通常意味着数据会直接从磁盘读取到内存，而不经过操作系统缓存。这可以减少内存使用，但在某些情况下可能会影响性能。
        commands.add("-avioflags");
        commands.add("direct");
        // 强制覆盖输出文件和禁用音频流。
        commands.add("-y");
        commands.add("-an");
        // This drastically reduces stalling when cpu is overbooked. By default
        // VP9 tries to use all available threads?
        commands.add("-threads");
        commands.add("1");
        // 设置帧率是30
        commands.add("-framerate");
        commands.add(Integer.toString(DEFAULT_FPS));
        //读取图片
        commands.add("-i");
        commands.add(Helper.join(this.tempCacheDir, "%d.png"));
        // 设置视频比特率为动态调整和裁剪视频
        commands.addAll(getFormatArgs(Objects.isNull(options.getFormat()) ? WEBM : options.getFormat()));
        commands.add("-b:v");
        commands.add("0");
        if (GIF.equals(options.getFormat())) {
            commands.add("-filter_complex");
        } else {
            commands.add("-vf");
        }
        StringBuilder builder = new StringBuilder();
        if (options.getSpeed() != null) {
            builder.append("setpts=").append(1 / options.getSpeed()).append("*PTS");
        }
        if (options.getCrop() != null) {
            builder.append(",crop='min(").append(width).append(",iw):min(").append(height).append(",ih):0:0',pad=").append(width).append(":").append(height).append(":0:0");
            builder.append(",crop=").append(options.getCrop().getWidth()).append(":").append(options.getCrop().getHeight()).append(":").append(options.getCrop().getX()).append(":").append(options.getCrop().getY());
        }
        if (options.getScale() != null) {
            builder.append(",scale=iw*").append(options.getScale()).append(":").append("-1");
        }
        if (GIF.equals(options.getFormat())) {
            builder.append(",fps=5,palettegen=stats_mode=single[pal],[0:v][pal]paletteuse");
        }
        commands.add(builder.toString());
        //输出文件
        commands.add(this.options.getPath());
        ProcessBuilder pb = new ProcessBuilder(commands).redirectErrorStream(true);
        Process process = pb.start();
        LOGGER.info(StreamUtil.toString(process.getInputStream()));
        process.waitFor();
    }
}