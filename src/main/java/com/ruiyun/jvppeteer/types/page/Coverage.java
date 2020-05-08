package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.protocol.CSS.Point;
import com.ruiyun.jvppeteer.protocol.CSS.Range;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coverage gathers information about parts of JavaScript and CSS that were used by the page.
 */
public class Coverage {

    private CSSCoverage cssCoverage;

    private JSCoverage jsCoverage;

    public Coverage(CDPSession client) {
        this.cssCoverage = new CSSCoverage(client);
        this.jsCoverage = new JSCoverage(client);
    }

    public void startJSCoverage(boolean resetOnNavigation,boolean reportAnonymousScripts){
        this.jsCoverage.start(resetOnNavigation,reportAnonymousScripts);
    }

    public List<CoverageEntry> stopJSCoverage() throws JsonProcessingException {
        return  this.jsCoverage.stop();
    }

    public void startCSSCoverage(boolean resetOnNavigation) {
        this.cssCoverage.start(resetOnNavigation);
    }

    /**
     * @return {!Promise<!Array<!CoverageEntry>>}
     */
    public List<CoverageEntry> stopCSSCoverage() {
        return  this.cssCoverage.stop();
    }

    public static List<Range> convertToDisjointRanges(List<CoverageRange> nestedRanges) {
        List<Point> points = new ArrayList<>();
        if(ValidateUtil.isNotEmpty(nestedRanges)){
            for (CoverageRange range : nestedRanges) {
                points.add(createPoint(range.getStartOffset(), 0, range));
                points.add(createPoint(range.getStartOffset(), 1, range));
            }
        }
        // Sort points to form a valid parenthesis sequence.
        points.sort(new Comparator<Point>() {
            @Override
            public int compare(Point a, Point b) {

                // Sort with increasing offsets.
                if (a.getOffset() != b.getOffset())
                    return a.getOffset() - b.getOffset();
                // All "end" points should go before "start" points.
                if (a.getType() != b.getType())
                    return b.getType() - a.getType();
                int aLength = a.getRange().getEndOffset() - a.getRange().getStartOffset();
                int bLength = b.getRange().getEndOffset() - b.getRange().getStartOffset();
                // For two "start" points, the one with longer range goes first.
                if (a.getType() == 0)
                    return bLength - aLength;
                // For two "end" points, the one with shorter range goes first.
                return aLength - bLength;
            }

        });

        LinkedList<Integer> hitCountStack = new LinkedList<>();

        List<Range> results = new ArrayList<>();
        int lastOffset = 0;
        // Run scanning line to intersect all ranges.
        for (Point point : points) {
            if (hitCountStack.size() > 0 && lastOffset < point.getOffset() && hitCountStack.get(hitCountStack.size() - 1) > 0) {
                Range lastResult = results.size() > 0 ? results.get(results.size() - 1) : null;
                if (lastResult != null && lastResult.getEnd() == lastOffset)
                    lastResult.setEnd(point.getOffset());
                else
                    results.add(createRange(lastOffset, point.getOffset()));
            }
            lastOffset = point.getOffset();
            if (point.getType() == 0)
                hitCountStack.addLast(point.getRange().getCount());
            else
                hitCountStack.pop();
        }
        // Filter out empty ranges.
        return results.stream().filter(range -> range.getEnd() - range.getStart() > 1).collect(Collectors.toList());
    }

    private static Point createPoint(int startOffset, int type, CoverageRange range) {
        return new Point(startOffset, type, range);
    }
    private static Range createRange(int start, int end) {
        return new Range(start, end);
    }
}
