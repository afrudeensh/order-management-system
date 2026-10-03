$root = "E:\order-management-system"
$env:DB_PASSWORD = "root"
$env:JWT_SECRET = "AfrudeenMicroservices2026SecretKeyForJWTDevelopmentOnly"

function Start-Svc($name, $delaySeconds) {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", `
        "`$host.UI.RawUI.WindowTitle='$name'; cd '$root\backend\$name'; .\mvnw.cmd spring-boot:run"
    Start-Sleep -Seconds $delaySeconds
}

docker compose -f "$root\infrastructure\docker-compose.yml" up -d
Start-Sleep -Seconds 8

Start-Svc "service-discovery" 25
Start-Svc "product-service" 10
Start-Svc "user-service" 10
Start-Svc "order-service" 10
Start-Svc "notification-service" 10
Start-Svc "api-gateway" 5

Write-Host "Wait ~40 s, then open http://localhost:8761 (Eureka) and start Angular: ng serve"
