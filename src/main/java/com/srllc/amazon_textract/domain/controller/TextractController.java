package com.srllc.amazon_textract.domain.controller;

import com.srllc.amazon_textract.domain.record.ExtractTextResponse;
import com.srllc.amazon_textract.domain.service.TextractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/textract")
@Tag(name = "AWS Textract Controller", description = "Operation managing Textract")
@RequiredArgsConstructor
public class TextractController {

    private final TextractService textractService;

    @PostMapping(value = "/extract",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract text from an uploaded image or document")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Text extracted successfully"),
            @ApiResponse(responseCode = "400",description = "Invalid file or request"),
            @ApiResponse(responseCode = "500",description = "Internal Server Error")
    })
    public ResponseEntity<ExtractTextResponse> extractText(
            @Parameter(description = "Input file to extract",required = true)
            @RequestPart("file") MultipartFile file){
        return ResponseEntity.ok(textractService.extractTextFromImage(file));
    }
}
