package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.core.WaitTask;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

//done
public class TaskManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
    private final Set<WaitTask> tasks = new HashSet<>();

    public void add(WaitTask task) {
        tasks.add(task);
    }

    public void delete(WaitTask task) {
        tasks.remove(task);
    }

    public void terminateAll(JvppeteerException error) {
        tasks.forEach(task -> {
                    try {
                        task.terminate(error);
                    } catch (
                            Exception e
                    ) {
                        LOGGER.error("", e);
                    }
                }
        );
        tasks.clear();
    }

    public void rerunAll() {
        this.tasks.forEach(task -> {
            try {
                task.rerun();
            }catch (Exception e){
                LOGGER.error("",e);
            }
        });
        // 等待所有任务完成
    }
}
