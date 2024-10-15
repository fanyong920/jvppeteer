package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrameTree {
    private final Map<String, Frame> frames = new HashMap<>();
    private final Map<String, String> parentIds = new HashMap<>();
    private final Map<String, Set<String>> childIds = new HashMap<>();
    private Frame mainFrame;
    private boolean isMainFrameStale;
    final Map<String, Set<AwaitableResult<Frame>>> waitRequests = new HashMap<>();

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
        Frame frame = this.getById(frameId);
        if (frame != null) {
            return frame;
        }
        AwaitableResult<Frame> waitableResult = AwaitableResult.create();
        Set<AwaitableResult<Frame>> callbacks = waitRequests.get(frameId);
        if (callbacks == null) {
            callbacks = new HashSet<>();
        }
        callbacks.add(waitableResult);
        waitRequests.put(frameId, callbacks);
        return waitableResult.waitingGetResult();
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
        Set<AwaitableResult<Frame>> callbacks = this.waitRequests.get(frame.id());
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
            isMainFrameStale = true;
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
