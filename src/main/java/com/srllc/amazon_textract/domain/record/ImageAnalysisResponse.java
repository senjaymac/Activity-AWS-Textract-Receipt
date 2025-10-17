package com.srllc.amazon_textract.domain.record;

import java.util.List;

public record ImageAnalysisResponse(
    List<DetectedLabel> labels,
    String analysisType
) {
    public record DetectedLabel(
        String name,
        Float confidence,
        List<String> categories
    ) {}
}