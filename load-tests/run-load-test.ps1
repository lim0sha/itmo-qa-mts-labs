param(
    [int[]]$Users = @(5, 10, 20),
    [int]$Duration = 60,
    [int]$ThinkTime = 1000,
    [string]$Jmeter = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$results = Join-Path $root "results"
$report = Join-Path $root "report"
$jmx = Join-Path $root "load-test.jmx"

if (-not $Jmeter) {
    $Jmeter = if ($env:JMETER_HOME) { Join-Path $env:JMETER_HOME "bin\jmeter.bat" } else { "jmeter" }
}

New-Item -ItemType Directory -Force $results | Out-Null
if (Test-Path $report) { Remove-Item $report -Recurse -Force }

foreach ($userCount in $Users) {
    $jtl = Join-Path $results "run-$userCount-users.jtl"
    Write-Host "=== Run: $userCount users, ramp-up $userCount s, duration $Duration s, think time $ThinkTime ms ==="
    & $Jmeter -n -t $jmx -l $jtl "-Jusers=$userCount" "-Jrampup=$userCount" "-Jduration=$Duration" "-JthinkTime=$ThinkTime"
    if ($LASTEXITCODE -ne 0) { throw "JMeter failed for $userCount users (exit code $LASTEXITCODE)" }
}

$lastJtl = Join-Path $results "run-$($Users[-1])-users.jtl"
Write-Host "=== HTML report for $($Users[-1]) users ==="
& $Jmeter -g $lastJtl -o $report
if ($LASTEXITCODE -ne 0) { throw "JMeter report generation failed (exit code $LASTEXITCODE)" }

Write-Host "Done. Results: $results; report: $report"
