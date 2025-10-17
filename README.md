# AWS Textract & Rekognition Application

A Spring Boot application that uses AWS Textract for receipt processing and AWS Rekognition for image content analysis, with structured data storage in MySQL database.

## Features

- **Receipt Processing**: Extract and parse receipt data using AWS Textract
- **Image Analysis**: Identify objects, people, and activities using AWS Rekognition
- Parse and structure receipt data (merchant name, total amount, items)
- Store structured data in MySQL database
- REST API with Swagger UI documentation
- Global exception handling
- JPA entity relationships

## Prerequisites

- Java 21
- Maven 3.6+
- MySQL 8.0+
- AWS Account with Textract access

## Setup

### 1. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE textract_db;
```

### 2. Configuration
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/textract_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# AWS Configuration
aws.region=your-region
aws.accessKey=your-access-key
aws.secretKey=your-secret-key
```

### 3. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Receipt Processing (Textract)
- `POST /api/v1/textract/extract` - Upload receipt image for processing
- `POST /api/v1/textract/raw-text` - Extract raw text from images
- `GET /api/v1/receipts` - Get all processed receipts
- `GET /api/v1/receipts/{id}` - Get specific receipt by ID

### Image Analysis (Rekognition)
- `POST /api/v1/rekognition/analyze` - Analyze image content and identify objects/people

### Swagger UI
Access the API documentation at: `http://localhost:8084/swagger-ui/index.html`

## Database Schema

### Receipts Table
- `id` - Primary key
- `merchant_name` - Store/merchant name
- `total_amount` - Total receipt amount
- `receipt_date` - Date of receipt
- `created_at` - Processing timestamp

### Receipt Items Table
- `id` - Primary key
- `receipt_id` - Foreign key to receipts
- `item_name` - Product/service name
- `quantity` - Item quantity
- `price` - Item price

## Usage Examples

### Receipt Processing
1. Upload a receipt image via Swagger UI or curl:
```bash
curl -X POST "http://localhost:8084/api/v1/textract/extract" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@receipt.jpg"
```

2. Response includes structured data:
```json
{
  "receiptId": 1,
  "merchantName": "Store Name",
  "totalAmount": 25.99,
  "receiptDate": "2024-01-01T10:00:00",
  "items": [
    {
      "itemName": "Product 1",
      "quantity": 2,
      "price": 12.99
    }
  ],
  "rawText": ["Store Name", "Product 1 2 $12.99", "Total: $25.99"]
}
```

### Image Analysis
1. Analyze image content:
```bash
curl -X POST "http://localhost:8084/api/v1/rekognition/analyze" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@photo.jpg"
```

2. Response includes detected labels:
```json
{
  "labels": [
    {
      "name": "Person",
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
```

## Error Handling

The application includes global exception handling for:
- File upload errors
- AWS Textract service errors
- AWS Rekognition service errors
- Database connection issues
- Invalid input validation

All errors return structured JSON responses with appropriate HTTP status codes.# Activity-AWS-Textract-Receipt
