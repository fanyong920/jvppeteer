package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.entities.CSSCoverageOptions;
import com.ruiyun.jvppeteer.entities.CoveragePoint;
import com.ruiyun.jvppeteer.entities.JSCoverageEntry;
import com.ruiyun.jvppeteer.entities.JSCoverageOptions;
import com.ruiyun.jvppeteer.entities.Range;
import com.ruiyun.jvppeteer.entities.CoverageEntry;
import com.ruiyun.jvppeteer.entities.CoverageRange;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coverage 类提供了收集有关页面使用的 JavaScript 和 CSS。
 */
public class Coverage {

    private final CSSCoverage cssCoverage;

    private final JSCoverage jsCoverage;

    public Coverage(CDPSession client) {
        this.cssCoverage = new CSSCoverage(client);
        this.jsCoverage = new JSCoverage(client);
    }

    public void updateClient(CDPSession client) {
        this.cssCoverage.updateClient(client);
        this.jsCoverage.updateClient(client);
    }

    /**
     * 匿名脚本是没有关联 URL 的脚本。这些是使用 eval 或 new Function 在页面上动态创建的脚本。
     */
    public void startJSCoverage() {
        this.jsCoverage.start(new JSCoverageOptions());
    }

    /**
     * 匿名脚本是没有关联 URL 的脚本。这些是使用 eval 或 new Function 在页面上动态创建的脚本。
     *
     * @param options 覆盖范围选项
     */
    public void startJSCoverage(JSCoverageOptions options) {
        this.jsCoverage.start(options);
    }

    /**
     * 默认情况下，JavaScript 覆盖范围不包括匿名脚本。但是，会报告带有 sourceURL 的脚本。
     *
     * @return 所有脚本的覆盖率报告数组。
     * @throws JsonProcessingException 异常
     */
    public List<JSCoverageEntry> stopJSCoverage() throws JsonProcessingException {
        return this.jsCoverage.stop();
    }

    public void startCSSCoverage() {
        this.cssCoverage.start(new CSSCoverageOptions());
    }

    /**
     * @param options 一组可配置的覆盖范围选项，默认为 resetOnNavigation : true
     */
    public void startCSSCoverage(CSSCoverageOptions options) {
        this.cssCoverage.start(options);
    }

    /**
     * CSS Coverage 不包括没有 sourceURL 的动态注入样式标签。
     *
     * @return 所有样式表的覆盖率报告数组。
     */
    public List<CoverageEntry> stopCSSCoverage() {
        return this.cssCoverage.stop();
    }

    public static List<Range> convertToDisjointRanges(List<CoverageRange> nestedRanges) {
        List<CoveragePoint> points = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(nestedRanges)) {
            for (CoverageRange range : nestedRanges) {
                points.add(new CoveragePoint(range.getStartOffset(), 0, range));
                points.add(new CoveragePoint(range.getEndOffset(), 1, range));
            }
        }
        // Sort points to form a valid parenthesis sequence.
        points.sort((a, b) -> {
            // Sort with increasing offsets.
            if (a.getOffset() != b.getOffset())
                return (int) (a.getOffset() - b.getOffset());
            // All "end" points should go before "start" points.
            if (a.getType() != b.getType())
                return (int) (b.getType() - a.getType());
            double aLength = a.getRange().getEndOffset() - a.getRange().getStartOffset();
            double bLength = b.getRange().getEndOffset() - b.getRange().getStartOffset();
            // For two "start" points, the one with longer range goes first.
            if (a.getType() == 0)
                return (int) (bLength - aLength);
            // For two "end" points, the one with shorter range goes first.
            return (int) (aLength - bLength);
        });
        LinkedList<Double> hitCountStack = new LinkedList<>();
        List<Range> results = new ArrayList<>();
        double lastOffset = 0;
        // Run scanning line to intersect all ranges.
        for (CoveragePoint point : points) {
            if (!hitCountStack.isEmpty() && lastOffset < point.getOffset() && hitCountStack.get(hitCountStack.size() - 1) > 0) {
                Range lastResult = !results.isEmpty() ? results.get(results.size() - 1) : null;
                if (lastResult != null && lastResult.getEnd() == lastOffset)
                    lastResult.setEnd(point.getOffset());
                else
                    results.add(new Range(lastOffset, point.getOffset()));
            }
            lastOffset = point.getOffset();
            if (point.getType() == 0)
                hitCountStack.offer(point.getRange().getCount());
            else
                hitCountStack.poll();
        }
        // Filter out empty ranges.
        return results.stream().filter(range -> range.getEnd() - range.getStart() > 0).collect(Collectors.toList());
    }


}
