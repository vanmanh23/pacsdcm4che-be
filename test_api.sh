#!/bin/bash

# Test script cho DICOM API endpoints
# Sử dụng dcm4chee-core version 5

BASE_URL="http://localhost:8080/api/dicom"

echo "=== Testing DICOM API Endpoints ==="
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Lấy studies với DICOM tags
echo "1. Testing GET /studies-with-tags"
curl -X GET "$BASE_URL/studies-with-tags" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo ""

# Test 2: Lấy studies thông thường
echo "2. Testing GET /studies"
curl -X GET "$BASE_URL/studies" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo ""

# Test 3: Lấy patients
echo "3. Testing GET /patients"
curl -X GET "$BASE_URL/patients" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo ""

# Test 4: Lấy series
echo "4. Testing GET /series"
curl -X GET "$BASE_URL/series" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s
echo ""

# Test 5: Lấy study theo UID (nếu có data)
echo "5. Testing GET /studies/{studyInstanceUID}"
# Lấy study UID từ response trước đó
STUDY_UID=$(curl -s -X GET "$BASE_URL/studies-with-tags" | jq -r '.[0]."0020000D".value // empty' 2>/dev/null)

if [ ! -z "$STUDY_UID" ]; then
    echo "Using StudyInstanceUID: $STUDY_UID"
    curl -X GET "$BASE_URL/studies/$STUDY_UID" \
      -H "Accept: application/json" \
      -w "\nHTTP Status: %{http_code}\n" \
      -s
else
    echo "No study UID found, skipping this test"
fi
echo ""

# Test 6: Lấy DICOM tags cụ thể của study
echo "6. Testing GET /studies/{studyInstanceUID}/tags"
if [ ! -z "$STUDY_UID" ]; then
    curl -X GET "$BASE_URL/studies/$STUDY_UID/tags" \
      -H "Accept: application/json" \
      -w "\nHTTP Status: %{http_code}\n" \
      -s
else
    echo "No study UID found, skipping this test"
fi
echo ""

# Test 7: Lấy series của study
echo "7. Testing GET /studies/{studyInstanceUID}/series"
if [ ! -z "$STUDY_UID" ]; then
    curl -X GET "$BASE_URL/studies/$STUDY_UID/series" \
      -H "Accept: application/json" \
      -w "\nHTTP Status: %{http_code}\n" \
      -s
else
    echo "No study UID found, skipping this test"
fi
echo ""

# Test 8: Lấy instances của series (nếu có)
echo "8. Testing GET /studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances"
if [ ! -z "$STUDY_UID" ]; then
    SERIES_UID=$(curl -s -X GET "$BASE_URL/studies/$STUDY_UID/series" | jq -r '.[0]."0020000E".value // empty' 2>/dev/null)
    
    if [ ! -z "$SERIES_UID" ]; then
        echo "Using SeriesInstanceUID: $SERIES_UID"
        curl -X GET "$BASE_URL/studies/$STUDY_UID/series/$SERIES_UID/instances" \
          -H "Accept: application/json" \
          -w "\nHTTP Status: %{http_code}\n" \
          -s
    else
        echo "No series UID found, skipping this test"
    fi
else
    echo "No study UID found, skipping this test"
fi
echo ""

echo "=== Test completed ==="
echo ""
echo "Note: If you see HTTP Status: 000, it means the server is not running."
echo "Make sure to start the application with: mvn spring-boot:run" 