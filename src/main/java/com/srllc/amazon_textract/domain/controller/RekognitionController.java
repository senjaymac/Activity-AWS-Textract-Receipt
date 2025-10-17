package com.srllc.amazon_textract.domain.controller;

import com.srllc.amazon_textract.domain.record.ImageAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rekognition")
@Tag(name = "Image Analysis", description = "AWS Rekognition image content analysis")
@RequiredArgsConstructor
public class RekognitionController {

    private final RekognitionClient rekognitionClient;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Analyze image content",
        description = "Upload an image to identify objects, people, activities, and other content using AWS Rekognition"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Image analyzed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ImageAnalysisResponse.class),
                examples = @ExampleObject(
                    name = "Sample Analysis Response",
                    value = """
                    {
                      "labels": [
                        {
                          "name": "Person",
                          "confidence": 99.8,
                          "categories": ["Person"]
                        },
                        {
                          "name": "Human",
                          "confidence": 99.8,
                          "categories": ["Person"]
                        },
                        {
                          "name": "Clothing",
                          "confidence": 95.2,
                          "categories": ["Apparel"]
                        }
                      ],
                      "analysisType": "LABEL_DETECTION"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format or corrupted image"),
        @ApiResponse(responseCode = "413", description = "File size too large"),
        @ApiResponse(responseCode = "500", description = "AWS Rekognition service error")
    })
    public ResponseEntity<ImageAnalysisResponse> analyzeImage(
        @Parameter(
            description = "Image file to analyze (JPG, PNG supported)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart("file") MultipartFile file
    ) throws IOException {
        
        byte[] imageBytes = file.getBytes();
        
        var request = DetectLabelsRequest.builder()
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .maxLabels(20)
                .minConfidence(70F)
                .build();

        var response = rekognitionClient.detectLabels(request);

        List<ImageAnalysisResponse.DetectedLabel> labels = response.labels().stream()
                .map(label -> new ImageAnalysisResponse.DetectedLabel(
                        label.name(),
                        label.confidence(),
                        label.categories().stream()
                                .map(category -> category.name())
                                .toList()
                ))
                .toList();

        return ResponseEntity.ok(new ImageAnalysisResponse(labels, List.of(), "LABEL_DETECTION"));
    }

    @PostMapping(value = "/celebrity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Recognize celebrities in image",
        description = "Upload an image to identify celebrities using AWS Rekognition celebrity recognition"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Celebrity recognition completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ImageAnalysisResponse.class),
                examples = @ExampleObject(
                    name = "Celebrity Recognition Response",
                    value = """
                    {
                      "labels": [],
                      "celebrities": [
                        {
                          "name": "Celebrity Name",
                          "confidence": 99.5,
                          "urls": ["https://www.imdb.com/name/nm0000123"]
                        }
                      ],
                      "analysisType": "CELEBRITY_RECOGNITION"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format"),
        @ApiResponse(responseCode = "500", description = "AWS Rekognition service error")
    })
    public ResponseEntity<ImageAnalysisResponse> recognizeCelebrities(
        @Parameter(
            description = "Image file containing celebrities (JPG, PNG supported)",
            required = true
        )
        @RequestPart("file") MultipartFile file
    ) throws IOException {
        
        byte[] imageBytes = file.getBytes();
        
        var request = RecognizeCelebritiesRequest.builder()
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .build();

        var response = rekognitionClient.recognizeCelebrities(request);

        List<ImageAnalysisResponse.DetectedCelebrity> celebrities = response.celebrityFaces().stream()
                .map(celebrity -> new ImageAnalysisResponse.DetectedCelebrity(
                        celebrity.name(),
                        celebrity.matchConfidence(),
                        celebrity.urls()
                ))
                .toList();

        return ResponseEntity.ok(new ImageAnalysisResponse(List.of(), celebrities, "CELEBRITY_RECOGNITION"));
    }
}