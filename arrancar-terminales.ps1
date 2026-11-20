# Arranca EduSpace: una ventana de PowerShell por servicio.
# Uso (en PowerShell, desde la carpeta del proyecto):
#   powershell -ExecutionPolicy Bypass -File .\arrancar-terminales.ps1

$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
$jwk  = "http://localhost:8090/.well-known/jwks.json"
$kafka = "localhost:19092"

function Abrir-Terminal($titulo, $carpeta, $comando) {
    $cmd = "`$host.UI.RawUI.WindowTitle='$titulo'; cd '$carpeta'; $comando"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}

function Esperar-Up($url, $nombre) {
    Write-Host "Esperando a $nombre ..." -ForegroundColor Yellow
    for ($i = 0; $i -lt 90; $i++) {
        try {
            $r = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 2
            if ($r.Content -match 'UP') { Write-Host "$nombre UP" -ForegroundColor Green; return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    Write-Warning "$nombre no respondio a tiempo. Revisa su ventana."
}

# 0. Contenedores de Docker (bases de datos, Kafka, etc.)
Write-Host "Encendiendo contenedores..." -ForegroundColor Cyan
docker start (docker ps -a -q --filter "name=e2e-")

# 1. Infraestructura: config, eureka, auth
$infra = "$raiz\infra\eduspace-config"
Abrir-Terminal "config" $infra "java -jar target\eduspace-config-0.0.1-SNAPSHOT.jar"
Esperar-Up "http://localhost:18888/actuator/health" "config"

Abrir-Terminal "eureka" "$raiz\infra\eduspace-eureka" "java -jar target\eduspace-eureka-0.0.1-SNAPSHOT.jar"
Esperar-Up "http://localhost:18761/actuator/health" "eureka"

Abrir-Terminal "auth-ms" "$raiz\services\auth-ms" "java -jar target\auth-ms-1.0.0-SNAPSHOT.jar"
Esperar-Up "http://localhost:8090/actuator/health" "auth-ms"

# 2. Servicios de negocio (cada uno en su ventana)
$servicios = @(
    @{ t = "planificacion-ms";   d = "services\planificacion-ms";   j = "planificacion-ms-0.0.1-SNAPSHOT.jar";   p = 8091 },
    @{ t = "alumnos-ms";         d = "services\alumnos-ms";         j = "alumnos-ms-0.0.1-SNAPSHOT.jar";         p = 8092 },
    @{ t = "matricula-ms";       d = "services\matricula-ms";       j = "matricula-ms-1.0.0-SNAPSHOT.jar";       p = 8093 },
    @{ t = "asistencia-ms";      d = "services\asistencia-ms";      j = "asistencia-ms-1.0.0-SNAPSHOT.jar";      p = 8094 },
    @{ t = "calificaciones-ms";  d = "services\calificaciones-ms";  j = "calificaciones-ms-1.0.0-SNAPSHOT.jar";  p = 8095 },
    @{ t = "pago-ms";            d = "services\pago-ms";            j = "pago-ms-1.0.0-SNAPSHOT.jar";            p = 8096 },
    @{ t = "examenes-ms";        d = "services\examenes-ms";        j = "examenes-ms-1.0.0-SNAPSHOT.jar";        p = 8097 },
    @{ t = "tareas-ms";          d = "services\tareas-ms";          j = "tareas-ms-1.0.0-SNAPSHOT.jar";          p = 8098 },
    @{ t = "notificaciones-ms";  d = "services\notificaciones-ms";  j = "notificaciones-ms-0.0.1-SNAPSHOT.jar";  p = 8099 }
)
foreach ($s in $servicios) {
    $cmd = "`$env:JWK_SET_URI='$jwk'; `$env:KAFKA_BOOTSTRAP_SERVERS='$kafka'; java -jar target\$($s.j)"
    Abrir-Terminal $s.t "$raiz\$($s.d)" $cmd
    Esperar-Up "http://localhost:$($s.p)/actuator/health" $s.t
}

# 3. Gateway (la puerta de entrada)
$cmdGw = "`$env:JWK_SET_URI='$jwk'; `$env:CORS_ALLOWED_ORIGINS='http://localhost:4200,http://127.0.0.1:4200'; java -jar target\eduspace-gateway-0.0.1-SNAPSHOT.jar"
Abrir-Terminal "gateway" "$raiz\infra\eduspace-gateway" $cmdGw
Esperar-Up "http://localhost:18080/actuator/health" "gateway"

# 4. Frontend
Abrir-Terminal "frontend" $raiz "npm.cmd --prefix frontend run auth:eduspace; npm.cmd --prefix frontend start"

Write-Host ""
Write-Host "EduSpace listo. Abre http://localhost:4200 (usuario admin / clave admin123)" -ForegroundColor Green
