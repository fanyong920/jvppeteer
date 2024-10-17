package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameTree {
    private final Map<String, Frame> frames = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, String> parentIds = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> childIds = new ConcurrentHashMap<>();
    private Frame mainFrame;
    private volatile boolean isMainFrameStale = false;
    final Map<String, Set<AwaitableResult<Frame>>> waitRequests = new ConcurrentHashMap<>();

    public Frame getMainFrame() {
        return this.mainFrame;
    }

    public Frame getById(String frameId) {
        return this.frames.get(frameId);
    }

    /**
     * 等待Frame
     *
     * @param frameId 等待的frame的id
     * @return 等待的frame
     */
    public Frame waitForFrame(String frameId) {
        AwaitableResult<Frame> awaitableResult = AwaitableResult.create();
        this.waitRequests.computeIfAbsent(frameId, k -> new CopyOnWriteArraySet<>()).add(awaitableResult);
        Frame frame = this.getById(frameId);
        if (frame != null) {
            return frame;
        }
        return awaitableResult.waitingGetResult(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public List<Frame> frames() {
        return new ArrayList<>(this.frames.values());
    }

    public void addFrame(Frame frame) {
        this.frames.put(frame.id(), frame);
        if (StringUtil.isNotEmpty(frame.parentId())) {
            this.parentIds.put(frame.id(), frame.parentId());
            this.childIds.computeIfAbsent(frame.parentId(), k -> new HashSet<>()).add(frame.id());
        } else if (this.mainFrame == null || this.isMainFrameStale) {
            this.mainFrame = frame;
            this.isMainFrameStale = false;
        }
        Set<AwaitableResult<Frame>> callbacks = this.waitRequests.remove(frame.id());
        if (ValidateUtil.isNotEmpty(callbacks)) {
            callbacks.forEach(request -> request.onSuccess(frame));
        }
    }

    public void removeFrame(Frame frame) {
        String frameId = frame.id();
        this.frames.remove(frameId);
        this.parentIds.remove(frameId); // Retrieve and remove in one operation
        if (StringUtil.isNotEmpty(frame.parentId())) {
            Set<String> children = this.childIds.get(frame.parentId());
            if (children != null) {
                children.remove(frameId);
            }
        } else {
            this.isMainFrameStale = true;
        }
    }

    public List<Frame> childFrames(String frameId) {
        Set<String> childIds = this.childIds.get(frameId);
        if (childIds == null) {
            return new ArrayList<>();
        }
        List<Frame> frames = new ArrayList<>();
        for (String id : childIds) {
            Frame frame = this.getById(id);
            if (frame != null) {
                frames.add(frame);
            }
        }
        return frames;
    }

    public Frame parentFrame(String frameId) {
        String parentId = this.parentIds.get(frameId);
        return StringUtil.isNotEmpty(parentId) ? this.getById(parentId) : null;
    }
}
