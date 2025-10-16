package com.srllc.amazon_textract.domain.record;

import java.util.List;

public record RawTextResponse(
    List<String> rawText
) {}