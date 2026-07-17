$ErrorActionPreference = "Stop"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Frontend = Join-Path $Root "frontend"

Write-Host "== Travel Agent local verification ==" -ForegroundColor Cyan
Write-Host "Project: $Root"

Write-Host "`n[1/2] Running backend tests..." -ForegroundColor Cyan
Push-Location $Root
try {
    & ".\mvnw.cmd" test
}
finally {
    Pop-Location
}

Write-Host "`n[2/2] Building frontend..." -ForegroundColor Cyan
Push-Location $Frontend
try {
    & "pnpm.cmd" build
}
finally {
    Pop-Location
}

Write-Host "`nVerification passed: backend tests and frontend build completed." -ForegroundColor Green
