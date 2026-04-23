# Gemini CLI Hook Script
# 이 스크립트는 포매터, 린트, 테스트를 실행하여 코드 품질을 보장합니다.
# 현재 프로젝트 구성에서는 Gradle의 'check' 태스크를 통해 이를 수행합니다.

Write-Host "--- Quality Check Start ---" -ForegroundColor Cyan

# monolith 디렉토리 경로 정의
$monolithPath = Join-Path $PSScriptRoot "monolith"

# monolith 디렉토리 존재 확인 및 이동
if (Test-Path $monolithPath) {
    Push-Location $monolithPath
    try {
        Write-Host "Executing: ./gradlew check in $monolithPath" -ForegroundColor Yellow
        # Gradle check 태스크 실행 (테스트 및 품질 검사 포함)
        ./gradlew check
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error: Quality Check failed with exit code $LASTEXITCODE" -ForegroundColor Red
            exit $LASTEXITCODE
        }
    }
    catch {
        Write-Host "Error: An unexpected error occurred during execution." -ForegroundColor Red
        Write-Host $_
        exit 1
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "Error: monolith directory not found." -ForegroundColor Red
    exit 1
}

Write-Host "--- Quality Check Passed Successfully ---" -ForegroundColor Green
