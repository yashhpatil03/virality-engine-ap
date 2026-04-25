Write-Host "=== Sequential Test - Horizontal Cap ===" -ForegroundColor Cyan
Write-Host "Adding 101 bot replies sequentially..." -ForegroundColor Yellow
Write-Host ""

$successCount = 0

# Add 100 bot replies
for ($i = 1; $i -le 100; $i++) {
    $body = @{
        content = "Test $i"
        authorId = 1
        authorType = "BOT"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
            -Method Post `
            -Body $body `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        Write-Host "Request $i : SUCCESS" -ForegroundColor Green
        $successCount++
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Request $i : FAILED (HTTP $statusCode)" -ForegroundColor Red
    }
}

# Try the 101st request
Write-Host "`nAttempting 101st request..." -ForegroundColor Yellow
$body = @{
    content = "Should fail"
    authorId = 1
    authorType = "BOT"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    Write-Host "101st request: SUCCESS (This should NOT happen!)" -ForegroundColor Red
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 429) {
        Write-Host "101st request: FAILED with HTTP 429 - CORRECT! Horizontal cap working." -ForegroundColor Green
    } else {
        Write-Host "101st request: FAILED with HTTP $statusCode" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Results ===" -ForegroundColor Cyan
Write-Host "Successful replies: $successCount/100" -ForegroundColor Green

# Check Redis using curl
Write-Host "`nChecking Redis counter:" -ForegroundColor Cyan
curl.exe -X GET http://localhost:8080/api/posts/1/virality-score 2>$null
Write-Host ""