param(
    [string]$BaseUrl = "http://localhost",
    [switch]$ResetVolumes,
    [int]$HealthRetries = 30,
    [int]$HealthDelaySeconds = 10
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Command
    )
    Write-Host "==> $Name"
    & $Command
}

function Test-Health {
    param(
        [string]$Name,
        [int]$Port
    )
    $url = "$BaseUrl`:$Port/actuator/health"
    for ($attempt = 1; $attempt -le $HealthRetries; $attempt++) {
        Write-Host "Checking $Name $url ($attempt/$HealthRetries)"
        try {
            $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 20
            if ($response.status -eq "UP") {
                return
            }
            Write-Host "$Name health is $($response.status). Response: $($response | ConvertTo-Json -Compress)"
        } catch {
            Write-Host "$Name health check failed: $($_.Exception.Message)"
        }
        Start-Sleep -Seconds $HealthDelaySeconds
    }
    docker compose ps
    docker compose logs --tail=120 $Name
    throw "$Name health did not become UP after $HealthRetries attempts."
}

Invoke-Step "Gradle clean build" { .\gradlew.bat clean build }
Invoke-Step "Gradle bootJar" { .\gradlew.bat bootJar }
if ($ResetVolumes) {
    Invoke-Step "Docker compose reset volumes" { docker compose down --volumes --remove-orphans }
}
Invoke-Step "Docker compose config" { docker compose config --quiet }
Invoke-Step "Docker compose up" { docker compose up -d --build }
Invoke-Step "Docker compose ps" { docker compose ps }

$services = @(
    @{ Name = "api-gateway"; Port = 8080 },
    @{ Name = "iam-service"; Port = 8081 },
    @{ Name = "user-profile-service"; Port = 8082 },
    @{ Name = "policy-service"; Port = 8083 },
    @{ Name = "claim-service"; Port = 8084 },
    @{ Name = "health-record-service"; Port = 8085 },
    @{ Name = "ai-risk-service"; Port = 8086 },
    @{ Name = "fraud-detection-service"; Port = 8087 },
    @{ Name = "notification-service"; Port = 8088 },
    @{ Name = "payment-service"; Port = 8089 },
    @{ Name = "discovery-server"; Port = 8761 },
    @{ Name = "config-server"; Port = 8888 }
)

foreach ($service in $services) {
    Test-Health -Name $service.Name -Port $service.Port
}

Write-Host "Docker smoke test completed successfully."
