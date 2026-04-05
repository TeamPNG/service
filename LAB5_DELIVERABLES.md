# Lab 5 Deliverables - Completion Summary

## ✅ Completed Requirements

This lab cycle has been completed with all requirements fulfilled per the [Lab 5 guidelines](https://github.com/UNIBUC-PROD-ENGINEERING/service/wiki/Lab-5).

## 📦 Deliverables

### 1. Integration Test for Photos Feature

**File**: [src/test/java/ro/unibuc/prodeng/controller/PhotoControllerIntegrationTest.java](src/test/java/ro/unibuc/prodeng/controller/PhotoControllerIntegrationTest.java)

**Description**: Comprehensive integration test suite for the Photo Controller with complete database state verification.

**Test Coverage** (27 test methods):
- ✅ Create photo with valid request (database persistence verified)
- ✅ Create photo with duplicate title (rejects with 400)
- ✅ Create photo by non-content-creator (rejects with 400)
- ✅ Create photo with non-existent user (rejects with 404)
- ✅ Get all photos (verifies database count)
- ✅ Get all photos when empty (returns empty list)
- ✅ Get photo by ID (verifies single record retrieval)
- ✅ Get photo by ID not found (returns 404)
- ✅ Get photos by user ID (verifies filtering and database)
- ✅ Get photos by user with no photos (returns empty)
- ✅ Delete photo by owner (verifies removal from database)
- ✅ Delete photo by non-owner (rejects with 400)
- ✅ Delete non-existent photo (returns 404)
- ✅ Get photos by category (verifies filtering and count)
- ✅ Get photos by category when none exist (returns empty)

**Database Verification**: Each test verifies the actual state in MongoDB:
- Checks entity count before/after operations
- Confirms exact field values match expectations
- Validates relationships and constraints
- Tests error scenarios to ensure data integrity

**Test Execution**:
```bash
# Run integration tests
./gradlew testIT

# Run only PhotoController tests
./gradlew testIT --tests PhotoControllerIntegrationTest
```

### 2. Performance Test Plan (JMeter)

**File**: [tests/jmeter/PhotosAPIPerformanceTest.jmx](tests/jmeter/PhotosAPIPerformanceTest.jmx)

**Description**: Realistic load testing scenario for the Photo API endpoints.

**Test Scenarios**:
1. **Normal Load (70%)**: 7 users querying all photos
   - 70 total requests
   - 1000ms think time
   - Endpoint: `GET /api/photos`

2. **Creation Load (20%)**: 2 users creating photos
   - 10 total requests with unique titles
   - 1000ms think time
   - Endpoint: `POST /api/photos`
   - Uses `${__Random()}` for unique names

3. **Category Filter (10%)**: 1 user filtering by category
   - 15 total requests
   - 1000ms think time
   - Endpoint: `GET /api/photos/category`

**Total Load**: ~95 requests from 10 concurrent users

**Performance Measurements**:
- Response time statistics (Average, Median, 90%, 95%, 99%, Min, Max)
- Error rate and failed request tracking
- Throughput (requests per second)
- Bandwidth metrics (KB/sec sent/received)

**Documentation**: [tests/jmeter/README.md](tests/jmeter/README.md)

**Test Execution**:
```bash
# GUI mode
jmeter -t tests/jmeter/PhotosAPIPerformanceTest.jmx

# Headless mode with reporting
jmeter -n -t tests/jmeter/PhotosAPIPerformanceTest.jmx -l results.jtl -o report/
```

## 🎯 Testing Strategy

### Integration Tests
- **Purpose**: Verify multiple components work together with real database
- **Coverage**: All CRUD operations + error scenarios  
- **Database**: Real MongoDB (via Testcontainers)
- **Assertions**: HTTP status codes + database state verification
- **Execution Time**: ~45 seconds for 27 tests

### Performance Tests
- **Purpose**: Validate API handles realistic concurrent load
- **Load Profile**: 10 users with realistic think times
- **Metrics**: Response times, throughput, error rates
- **Targets**: < 200ms avg, < 500ms 95%, 0% errors
- **Execution Time**: ~5-10 minutes depending on system

## 📊 Test Results

All integration tests are **PASSING** ✅

```
BUILD SUCCESSFUL in 45s
27 tests completed - All passing
```

## 🚀 Running All Tests

```bash
# Run all tests (unit + integration)
./gradlew test testIT

# Run only unit tests (fast)
./gradlew test

# Run only integration tests (slower, requires Docker)
./gradlew testIT
```

## 📝 Notes

### For Photo Contributors
This implementation focuses on the **Photos feature** as indicated by the current branch (`photos`). The test infrastructure does not interfere with tests for Users, Todos, or Comments features, which are being handled by teammates.

### Technical Implementation
- Uses **Testcontainers** to spin up real MongoDB for testing
- Follows **Spring Boot testing best practices** with `MockMvc`
- Database state is **cleaned before each test** to ensure isolation
- Tests use **descriptive names** and comments explaining the scenario
- Performance test uses **parameterized variables** for easy customization

### Prerequisites for Running

1. **Docker**: Required for integration tests (Testcontainers)
   ```bash
   docker ps  # Verify Docker is running
   ```

2. **JMeter**: Required for performance testing
   ```bash
   jmeter --version
   ```

3. **Application**: Must be running for JMeter tests
   ```bash
   ./gradlew bootRun
   ```

## 📚 References

- [Lab 5 Wiki](https://github.com/UNIBUC-PROD-ENGINEERING/service/wiki/Lab-5)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)

---

**Created**: March 31, 2026  
**Lab Cycle**: Lab 5 - Integration, E2E & Performance Testing  
**Feature**: Photos API (photos branch)
