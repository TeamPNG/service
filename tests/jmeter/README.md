# Photos API Performance Test Plan

## Overview

This JMeter test plan (`PhotosAPIPerformanceTest.jmx`) is designed to perform performance testing of the Photos API endpoints under realistic load conditions.

## Test Configuration

The test plan simulates **10 concurrent users** across three traffic profiles:

### Thread Groups

#### 1. Normal Load - Read Photos (70% load)
- **Users**: 7 concurrent threads
- **Ramp-up**: 10 seconds
- **Loops**: 10 iterations per user
- **Total Requests**: ~70 requests
- **Endpoint**: `GET /api/photos`
- **Think Time**: 1000ms between requests
- **Purpose**: Simulate users browsing all photos

#### 2. Photo Creation (20% load)
- **Users**: 2 concurrent threads  
- **Ramp-up**: 10 seconds
- **Loops**: 5 iterations per user
- **Total Requests**: ~10 requests
- **Endpoint**: `POST /api/photos`
- **Think Time**: 1000ms between requests
- **Purpose**: Simulate users uploading photos
- **Data**: Random photo titles with thread/counter variables to avoid duplicates

#### 3. Heavy Load - Read by Category (10% load)
- **Users**: 1 concurrent thread
- **Ramp-up**: 10 seconds
- **Loops**: 15 iterations
- **Total Requests**: ~15 requests
- **Endpoint**: `GET /api/photos/category?category=nature`
- **Think Time**: 1000ms between requests
- **Purpose**: Simulate filtering photos by category

## How to Run

### Prerequisites

1. Ensure JMeter is installed:
   ```bash
   # macOS
   brew install jmeter
   
   # Ubuntu/Debian
   sudo apt install jmeter
   
   # Or download from https://jmeter.apache.org/download_jmeter.cgi
   ```

2. Ensure the application is running:
   ```bash
   ./gradlew bootRun
   ```

3. Ensure a content creator user exists in the database. The test uses `creator1` as the `CONTENT_CREATOR_ID`. You can modify this in the test plan's User Defined Variables.

### Running GUI Mode

```bash
jmeter -t tests/jmeter/PhotosAPIPerformanceTest.jmx
```

### Running Command Line Mode (Headless)

```bash
jmeter -n -t tests/jmeter/PhotosAPIPerformanceTest.jmx -l results.jtl -j jmeter.log -Jusers=10 -Jrampup=10
```

Generate HTML report:
```bash
jmeter -g results.jtl -o report/
```

## Performance Targets

These are the performance targets for the Photos API:

| Metric | Target | Threshold |
|--------|--------|-----------|
| Average Response Time | < 200ms | ⚠️ Warning at 500ms |
| 95th Percentile | < 500ms | ⚠️ Warning at 1000ms |
| 99th Percentile | < 1000ms | ⚠️ Warning at 2000ms |
| Error Rate | 0% | ⚠️ Warning at 1% |
| Throughput | > 10 req/s | ⚠️ Warning at < 5 req/s |

## Test Assertions

All requests are validated with response code assertions:

- **GET /api/photos**: Expect HTTP 200
- **POST /api/photos**: Expect HTTP 201
- **GET /api/photos/category**: Expect HTTP 200

## Variables

The test plan uses the following User Defined Variables (edit in Test Plan properties):

```
BASE_URL=http://localhost:8080
CONTENT_CREATOR_ID=creator1
THINK_TIME=1000
```

### Modifying Variables

1. Open the test plan in JMeter
2. Right-click on "Test Plan" → Edit
3. Modify values in "User Defined Variables"

## Expected Results

### Summary Report Fields

- **Samples**: Total number of requests sent
- **Average**: Mean response time in milliseconds
- **Median**: 50th percentile response time
- **90% Line**: 90th percentile response time
- **95% Line**: 95th percentile response time
- **99% Line**: 99th percentile response time
- **Min**: Minimum response time
- **Max**: Maximum response time
- **Error %**: Percentage of failed requests
- **Throughput**: Requests per second
- **Received KB/sec**: Data received from server
- **Sent KB/sec**: Data sent to server

## Interpreting Results

### ✅ Good Performance

- Error rate: **0%**
- Average response time: **< 200ms**
- 95th percentile: **< 500ms**
- Throughput: **stable and increasing**

### ⚠️ Needs Improvement

- Error rate: **> 1%**
- Average response time: **> 500ms**
- 95th percentile: **> 1000ms**
- Throughput: **declining over time** (possible memory leak)

### ❌ Critical Issues

- Error rate: **> 5%**
- Average response time: **> 2000ms**
- 95th percentile: **> 5000ms**
- Connection timeouts or service unavailable

## Scaling the Test

To simulate more users:

1. **Increase Thread Count**: Modify the "Number of Threads" in each Thread Group
2. **Increase Loops**: Increase the "Loop Count" for longer test duration
3. **Command Line**: `jmeter -n -t test.jmx -Jusers=50`

### Example: 50 Users

- Normal Load: 35 users (~350 requests)
- Photo Creation: 10 users (~50 requests)
- Heavy Load: 5 users (~75 requests)

## Troubleshooting

### "Connection refused" errors

1. Verify application is running: http://localhost:8080
2. Check URL and port in Variable "BASE_URL"
3. Check firewall settings

### "User not found" errors (404)

1. Verify content creator user exists with ID matching `CONTENT_CREATOR_ID`
2. Create a test user or update the variable

### High error rates

1. Check application logs for errors
2. Verify database is running (MongoDB)
3. Reduce number of users if system is overloaded
4. Increase think time between requests

## Next Steps

After running the test:

1. **Review Results**: Analyze response times and error rates
2. **Identify Bottlenecks**: Look for slow endpoints
3. **Optimize**: Consider caching, database indexing, or code optimization
4. **Baseline Comparison**: Compare results across runs to track improvements
5. **Load Test**: Gradually increase load to find the breaking point

## References

- [JMeter Official Documentation](https://jmeter.apache.org/usermanual/index.html)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Performance Testing Guide](https://www.guru99.com/performance-testing.html)
