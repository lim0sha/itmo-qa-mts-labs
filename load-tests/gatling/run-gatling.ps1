param(
    [int[]]$Users = @(5, 10, 20),
    [int]$Duration = 60,
    [int]$ThinkTime = 1000
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$report = Join-Path $root "report"
Push-Location $root

try {
    foreach ($userCount in $Users) {
        Write-Host "=== Gatling run: $userCount users, ramp-up $userCount s, duration $Duration s, think time $ThinkTime ms ==="
        mvn -B gatling:test "-Dusers=$userCount" "-Drampup=$userCount" "-Dduration=$Duration" "-DthinkTime=$ThinkTime"
        if ($LASTEXITCODE -ne 0) { throw "Gatling failed for $userCount users (exit code $LASTEXITCODE)" }
    }

    $latest = Get-ChildItem (Join-Path $root "target\gatling") -Directory |
        Sort-Object LastWriteTime | Select-Object -Last 1
    if (Test-Path $report) { Remove-Item $report -Recurse -Force }
    Copy-Item $latest.FullName $report -Recurse

    Write-Host "Done. Reports: $(Join-Path $root 'target\gatling'); final: $report"
} finally {
    Pop-Location
}
