Write-Host "=== Sequential Test - Horizontal Cap ===" -ForegroundColor Cyan
Write-Host "Adding 101 bot replies sequentially..." -ForegroundColor Yellow
Write-Host ""

# Clear existing counter using Redis CLI via PowerShell
$redisCli = "C:\Program Files\Redis\redis-cli.exe"
if (Test-Path $redisCli) {
    & $redisCli DEL post:1:bot_count
    & $redisCli DEL post:1:virality_score
}

$successCount = 0
$failedCount = 0

# Add 100 bot replies
for ($i = 1; $i -le 100; $i++) {
    try {
        $body = @{
            content = "Test $i"
            authorId = 1
            authorType = "BOT"
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
            -Method Post `
            -Body $body `
            -ContentType "application/json" `
            -ErrorAction Stop

        Write-Host "Request $i : SUCCESS (HTTP 200)" -ForegroundColor Green
        $successCount++
    }
    catch {
        Write-Host "Request $i : FAILED (HTTP $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
        $failedCount++
    }
}

# Try the 101st request
Write-Host "`nAttempting 101st request..." -ForegroundColor Yellow
try {
    $body = @{
        content = "Should fail"
        authorId = 1
        authorType = "BOT"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop

    Write-Host "101st request: SUCCESS (Should have failed!)" -ForegroundColor Red
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 429) {
        Write-Host "101st request: FAILED (HTTP 429 - Too Many Requests) - CORRECT!" -ForegroundColor Green
        $failedCount++
    } else {
        Write-Host "101st request: FAILED with HTTP $statusCode" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Results ===" -ForegroundColor Cyan
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $failedCount" -ForegroundColor Red postgres -d virality_db -t -c "SELECT COUNT(*) FROM comment WHERE post_id = 1 AND author_type = 'BOT';"