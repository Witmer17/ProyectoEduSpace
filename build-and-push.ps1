param(
  [string]$DockerUser = $env:DOCKER_HUB_USER,
  [string]$Tag = "latest",
  [switch]$SkipCompile
)

if (-not $DockerUser) {
  $DockerUser = "witmer17"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  EduSpace: Build and Push Docker Images (PowerShell)" -ForegroundColor Cyan
Write-Host "  Usuario Docker Hub : $DockerUser" -ForegroundColor Yellow
Write-Host "  Etiqueta (Tag)     : $Tag" -ForegroundColor Yellow
Write-Host "  Omitir compilación : $SkipCompile" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$services = @(
  @{ name = "eduspace-config";    dir = "infra/eduspace-config";    type = "java" },
  @{ name = "eduspace-eureka";    dir = "infra/eduspace-eureka";    type = "java" },
  @{ name = "eduspace-gateway";   dir = "infra/eduspace-gateway";   type = "java" },
  @{ name = "auth-ms";            dir = "services/auth-ms";         type = "java" },
  @{ name = "planificacion-ms";   dir = "services/planificacion-ms";type = "java" },
  @{ name = "alumnos-ms";         dir = "services/alumnos-ms";      type = "java" },
  @{ name = "matricula-ms";       dir = "services/matricula-ms";    type = "java" },
  @{ name = "asistencia-ms";      dir = "services/asistencia-ms";   type = "java" },
  @{ name = "calificaciones-ms";  dir = "services/calificaciones-ms"; type = "java" },
  @{ name = "pago-ms";            dir = "services/pago-ms";         type = "java" },
  @{ name = "examenes-ms";        dir = "services/examenes-ms";     type = "java" },
  @{ name = "tareas-ms";          dir = "services/tareas-ms";       type = "java" },
  @{ name = "notificaciones-ms";  dir = "services/notificaciones-ms"; type = "java" },
  @{ name = "eduspace-frontend";  dir = "frontend";                 type = "frontend" }
)

$rootDir = Get-Location

# ==============================================================================
# FASE 1: Compilación Local
# ==============================================================================
if (-not $SkipCompile) {
  Write-Host "`n>>> FASE 1: Compilación local de servicios..." -ForegroundColor Yellow

  foreach ($s in $services) {
    Write-Host "`n--- Compilando $($s.name) ($($s.dir)) ---" -ForegroundColor Cyan
    Set-Location "$rootDir/$($s.dir)"

    if ($s.type -eq "java") {
      if (Test-Path "mvnw.cmd") {
        Write-Host "Ejecutando .\mvnw.cmd clean package -DskipTests ..." -ForegroundColor Gray
        & ".\mvnw.cmd" clean package -DskipTests -q
      } else {
        Write-Host "Ejecutando mvn clean package -DskipTests ..." -ForegroundColor Gray
        & mvn clean package -DskipTests -q
      }

      if ($LASTEXITCODE -ne 0) {
        Write-Host "Error compilando $($s.name)" -ForegroundColor Red
        Set-Location $rootDir
        exit 1
      }

      $jars = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -notmatch "\.original$" }
      if ($jars.Count -eq 0) {
        Write-Host "Error: No se encontró el .jar en target/ para $($s.name)" -ForegroundColor Red
        Set-Location $rootDir
        exit 1
      }
      Write-Host "✔ Compilación Java exitosa para $($s.name)" -ForegroundColor Green

    } elseif ($s.type -eq "frontend") {
      Write-Host "Ejecutando npm run build ..." -ForegroundColor Gray
      & npm run build
      if ($LASTEXITCODE -ne 0) {
        Write-Host "Error compilando frontend" -ForegroundColor Red
        Set-Location $rootDir
        exit 1
      }
      Write-Host "✔ Compilación Frontend exitosa." -ForegroundColor Green
    }

    Set-Location $rootDir
  }
} else {
  Write-Host "`n>>> Compilación omitida (-SkipCompile activado)." -ForegroundColor Yellow
}

# ==============================================================================
# FASE 2: Empaquetado Docker y Push
# ==============================================================================
Write-Host "`n>>> FASE 2: Construcción y subida de imágenes Docker..." -ForegroundColor Yellow
$total = $services.Count
$current = 0

foreach ($s in $services) {
  $current++
  $fullImage = "$DockerUser/$($s.name):$Tag"
  Write-Host "`n[$current/$total] Construyendo $($s.name) desde $($s.dir) -> $fullImage..." -ForegroundColor Green

  & docker build -t $fullImage $s.dir
  if ($LASTEXITCODE -ne 0) {
    Write-Host "Error construyendo imagen para $($s.name)" -ForegroundColor Red
    exit 1
  }

  Write-Host "Subiendo imagen $fullImage a Docker Hub..." -ForegroundColor Cyan
  & docker push $fullImage
  if ($LASTEXITCODE -ne 0) {
    Write-Host "Error subiendo imagen $fullImage a Docker Hub" -ForegroundColor Red
    exit 1
  }

  Write-Host "✔ Imagen subida exitosamente: $fullImage" -ForegroundColor Green
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  ¡Todas las imágenes fueron construidas y subidas con éxito!" -ForegroundColor Green
Write-Host "  Usuario Docker Hub : $DockerUser" -ForegroundColor Yellow
Write-Host "  Tag                : $Tag" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Green
