package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class DragData {
    private List<DragDataItem> items;
    /**
     * List of filenames that should be included when dropping
     */
    private List<String> files;
    /**
     * Bit field representing allowed drag operations. Copy = 1, Link = 2, Move = 16
     */
    private int dragOperationsMask;

    public List<DragDataItem> getItems() {
        return items;
    }

    public void setItems(List<DragDataItem> items) {
        this.items = items;
    }

    public int getDragOperationsMask() {
        return dragOperationsMask;
    }

    public void setDragOperationsMask(int dragOperationsMask) {
        this.dragOperationsMask = dragOperationsMask;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "DragData{" +
                "items=" + items +
                ", files=" + files +
                ", dragOperationsMask=" + dragOperationsMask +
                '}';
    }
}
