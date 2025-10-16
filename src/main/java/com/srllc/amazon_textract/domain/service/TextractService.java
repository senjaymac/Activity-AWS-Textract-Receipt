package com.srllc.amazon_textract.domain.service;

import com.srllc.amazon_textract.domain.record.ExtractTextResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TextractService {
    ExtractTextResponse extractTextFromImage(MultipartFile file);
}
