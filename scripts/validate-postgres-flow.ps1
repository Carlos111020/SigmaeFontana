param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-SigmaeJson {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = $null
    )

    $headers = @{}
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }

    $parameters = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = $headers
    }

    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = ($Body | ConvertTo-Json -Depth 8)
    }

    Invoke-RestMethod @parameters
}

function Login {
    param(
        [string]$Correo,
        [string]$Password
    )

    $response = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/auth/login" -Body @{
        correo = $Correo
        password = $Password
    }
    $response.token
}

$suffix = Get-Date -Format "yyyyMMddHHmmss"
$codigoEstudiante = "EST-VAL-$suffix"
$documentoEstudiante = "9$suffix"
$documentoAcudiente = "8$suffix"
$correoAcudiente = "acudiente.val$suffix@example.com"

Write-Host "Validando SIGMAE Fontana en $BaseUrl"

$adminToken = Login -Correo "admin@sigmae.edu.co" -Password "Admin123*"
$porteriaToken = Login -Correo "porteria@sigmae.edu.co" -Password "Porteria123*"
$coordinadorToken = Login -Correo "coordinador@sigmae.edu.co" -Password "Coord123*"
$acudienteToken = Login -Correo "acudiente@sigmae.edu.co" -Password "Acudiente123*"
Write-Host "Login OK para admin, porteria, coordinador y acudiente"

$grados = Invoke-SigmaeJson -Method "GET" -Path "/api/v1/grados?activo=true" -Token $adminToken
$grado = $grados | Where-Object { $_.nombre -eq "6A" } | Select-Object -First 1
if (-not $grado) {
    $grado = $grados | Select-Object -First 1
}
if (-not $grado) {
    throw "No hay grados disponibles. Inicie la aplicacion con APP_SEED_ENABLED=true o cree un grado primero."
}

$estudiante = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/estudiantes" -Token $adminToken -Body @{
    codigoEstudiantil = $codigoEstudiante
    documento = $documentoEstudiante
    nombres = "Prueba"
    apellidos = "PostgreSQL"
    gradoId = $grado.id
}
Write-Host "Estudiante creado: $($estudiante.codigoEstudiantil)"

$acudiente = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/acudientes" -Token $adminToken -Body @{
    documento = $documentoAcudiente
    nombres = "Acudiente"
    apellidos = "Validacion"
    telefono = "3000000000"
    correo = $correoAcudiente
}
Write-Host "Acudiente creado: $($acudiente.correo)"

Invoke-SigmaeJson -Method "POST" -Path "/api/v1/estudiantes/$($estudiante.id)/acudientes" -Token $adminToken -Body @{
    acudienteId = $acudiente.id
    parentesco = "Madre"
    responsablePrincipal = $true
} | Out-Null
Write-Host "Acudiente asociado al estudiante"

$ingreso = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/talanquera/ingresos" -Token $porteriaToken -Body @{
    codigoTarjeta = $codigoEstudiante
    observacion = "Ingreso validado contra PostgreSQL"
}
Write-Host "Ingreso OK: registro $($ingreso.registroId)"

$salida = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/talanquera/salidas" -Token $porteriaToken -Body @{
    codigoTarjeta = $codigoEstudiante
    observacion = "Salida validada contra PostgreSQL"
    crearNovedadSalidaAnticipada = $false
}
Write-Host "Salida OK: registro $($salida.registroId)"

$novedad = Invoke-SigmaeJson -Method "POST" -Path "/api/v1/novedades" -Token $coordinadorToken -Body @{
    tipoNovedad = "OBSERVACION_SEGURIDAD"
    descripcion = "Novedad de validacion PostgreSQL"
    estudianteId = $estudiante.id
}
Write-Host "Novedad creada: $($novedad.id)"

$dashboard = Invoke-SigmaeJson -Method "GET" -Path "/api/v1/dashboard/metricas" -Token $coordinadorToken
Write-Host "Dashboard OK: presentes=$($dashboard.estudiantesPresentes), ingresos=$($dashboard.ingresosDelDia), salidas=$($dashboard.salidasDelDia)"

$misEstudiantes = Invoke-SigmaeJson -Method "GET" -Path "/api/v1/acudientes/me/estudiantes" -Token $acudienteToken
Write-Host "Dashboard acudiente OK: estudiantes asociados=$($misEstudiantes.Count)"

Write-Host "Validacion PostgreSQL completada correctamente"
