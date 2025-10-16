package com.srllc.amazon_textract.domain.controller;

import com.srllc.amazon_textract.domain.record.ExtractTextResponse;
import com.srllc.amazon_textract.domain.record.RawTextResponse;
import com.srllc.amazon_textract.domain.service.TextractService;
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
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/textract")
@Tag(name = "Receipt Processing", description = "AWS Textract receipt processing and text extraction")
@RequiredArgsConstructor
public class TextractController {

    private final TextractService textractService;
    private final TextractClient textractClient;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Process receipt image and extract structured data",
        description = "Upload a receipt image to extract and parse structured data including store info, items, and transaction details. Data is automatically saved to database."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Receipt processed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ExtractTextResponse.class),
                examples = @ExampleObject(
                    name = "Sample Receipt Response",
                    value = """
                    {
                      "store": {
                        "name": "Robinson Malls",
                        "branch": "Caloocan City",
                        "manager": "Jane Doe",
                        "cashier_number": 5
                      },
                      "items": [
                        {
                          "product": "Ginger Tea",
                          "quantity": 1,
                          "price": 9.20
                        },
                        {
                          "product": "Brewed Coffee",
                          "quantity": 1,
                          "price": 19.20
                        }
                      ],
                      "transaction": {
                        "subtotal": 107.60,
                        "cash": 200.00,
                        "change": 92.40
                      }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format or corrupted image"),
        @ApiResponse(responseCode = "413", description = "File size too large"),
        @ApiResponse(responseCode = "500", description = "AWS Textract service error or internal server error")
    })
    public ResponseEntity<ExtractTextResponse> extractReceiptData(
        @Parameter(
            description = "Receipt image file (JPG, PNG, PDF supported)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(textractService.extractTextFromImage(file));
    }

    @PostMapping(value = "/raw-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Extract raw text from image",
        description = "Upload an image to extract raw text lines without parsing or structuring. Useful for debugging or custom processing."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Raw text extracted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RawTextResponse.class),
                examples = @ExampleObject(
                    name = "Raw Text Response",
                    value = """
                    {
                      "rawText": [
                        "Robinson Malls",
                        "Caloocan City",
                        "Manager",
                        "Jane Doe",
                        "Cashier",
                        "#5",
                        "Name",
                        "Qty",
                        "Price",
                        "Ginger Tea",
                        "1",
                        "9.20"
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format"),
        @ApiResponse(responseCode = "500", description = "AWS Textract service error")
    })
    public ResponseEntity<RawTextResponse> extractRawText(
        @Parameter(
            description = "Image file to extract raw text from",
            required = true
        )
        @RequestPart("file") MultipartFile file
    ) throws IOException {
        byte[] imageBytes = file.getBytes();
        
        var request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .build();

        var response = textractClient.detectDocumentText(request);

        List<String> lines = response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .toList();

        return ResponseEntity.ok(new RawTextResponse(lines));
    }
}