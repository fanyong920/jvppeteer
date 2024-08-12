package com.ruiyun.jvppeteer.options;

import java.util.List;

public class MediaFeaturesState extends ActiveProperty {

    public List<MediaFeature> mediaFeatures;

    public MediaFeaturesState(boolean active, List<MediaFeature> mediaFeatures) {
        super(active);
        this.mediaFeatures = mediaFeatures;
    }

    public List<MediaFeature> getmediaFeatures() {
        return mediaFeatures;
    }

    public void setMediaFeature(List<MediaFeature> mediaFeatures) {
        this.mediaFeatures = mediaFeatures;
    }

    @Override
    public String toString() {
        return "MediaFeaturesState{" +
                "mediaFeature=" + mediaFeatures +
                ", active=" + active +
                '}';
    }
}