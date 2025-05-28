# Script de configuração do ambiente para o Sistema de Ponto
# Autor: Claude
# Data: 2024-05-27

# Função para verificar se um diretório existe
function Test-DirectoryExists {
    param([string]$path)
    if (-not (Test-Path $path)) {
        Write-Host "Criando diretório: $path"
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

# Função para verificar se o Java está instalado
function Test-Java {
    $javaPath = "C:\Program Files\Eclipse Adoptium\jdk-8.0.452.9-hotspot"
    $javaExe = Join-Path $javaPath "bin\java.exe"
    $javacExe = Join-Path $javaPath "bin\javac.exe"
    
    if (-not (Test-Path $javaExe) -or -not (Test-Path $javacExe)) {
        Write-Host "ERRO: JDK 64 bits não encontrado em: $javaPath" -ForegroundColor Red
        Write-Host "Por favor, instale o OpenJDK8U-jdk_x64_windows_hotspot_8u452b09" -ForegroundColor Yellow
        Write-Host "Download: https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u452-b09/OpenJDK8U-jdk_x64_windows_hotspot_8u452b09.msi" -ForegroundColor Yellow
        return $false
    }
    
    try {
        $output = & $javaExe -version 2>&1
        if ($output -notlike "*64-Bit*") {
            Write-Host "AVISO: O Java encontrado não é 64 bits. Por favor, instale a versão 64 bits." -ForegroundColor Yellow
            return $false
        }
        
        # Configura JAVA_HOME e PATH
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, [System.EnvironmentVariableTarget]::Process)
        $env:Path = "$env:JAVA_HOME\bin;$env:Path"
        
        Write-Host "JDK 64 bits encontrado em: $javaPath" -ForegroundColor Green
        Write-Host "JAVA_HOME configurado: $env:JAVA_HOME" -ForegroundColor Green
        Write-Host "Versão do Java: $output" -ForegroundColor Green
        
        return $true
        
    } catch {
        Write-Host "ERRO ao verificar versão do Java: $_" -ForegroundColor Red
        return $false
    }
}

# Função para verificar se o Maven está instalado
function Test-Maven {
    try {
        # Força o uso do JAVA_HOME configurado
        $env:Path = "$env:JAVA_HOME\bin;$env:Path"
        
        $mvnOutput = & mvn -version 2>&1
        if ($mvnOutput -like "*not recognized*") {
            Write-Host "ERRO: Maven não encontrado" -ForegroundColor Red
            Write-Host "Por favor, instale o Maven: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
            return $false
        }
        Write-Host "Maven encontrado: $mvnOutput" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "ERRO: Maven não encontrado" -ForegroundColor Red
        Write-Host "Por favor, instale o Maven: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
        return $false
    }
}

# Função para verificar a arquitetura das DLLs
function Check-DLLArchitecture {
    param([string]$dllPath)
    
    try {
        # Usa o comando file do Git Bash
        $output = & 'C:\Program Files\Git\usr\bin\file.exe' $dllPath 2>&1
        if ($output -match "PE32\+ executable") {
            return "64-bit"
        } elseif ($output -match "PE32 executable") {
            return "32-bit"
        } else {
            return "Desconhecido"
        }
    } catch {
        Write-Host "AVISO: Comando 'file' não encontrado. Por favor, instale o Git for Windows" -ForegroundColor Yellow
        return "Desconhecido"
    }
}

# Função para verificar componentes do SDK
function Test-SDK {
    $dllPath = "lib\dll\sdk"
    
    # Verifica se o diretório existe
    if (-not (Test-Path $dllPath)) {
        Write-Host "ERRO: Diretório $dllPath não encontrado" -ForegroundColor Red
        return $false
    }
    
    # Lista de DLLs necessárias
    $requiredDlls = @(
        "libzkfp.dll",
        "libzkfpcsharp.dll",
        "libidfprcap.dll",
        "libsilkidcap.dll",
        "libzkfpmodulecap.dll",
        "libzklibcap.dll",
        "libcorrect.dll",
        "libsilkid.dll",
        "libzksensorcore.dll",
        "mi.dll",
        "USB.dll",
        "usb_dll.dll",
        "wd_utils.dll",
        "ZKFPCap_ASYNC.dll",
        "zkfputil.dll"
    )
    
    $missingDlls = @()
    $wrongArchDlls = @()
    
    foreach ($dll in $requiredDlls) {
        $dllFullPath = Join-Path $dllPath $dll
        if (-not (Test-Path $dllFullPath)) {
            $missingDlls += $dll
        } else {
            $arch = Check-DLLArchitecture $dllFullPath
            if ($arch -eq "64-bit") {
                $wrongArchDlls += "$dll ($arch)"
            }
        }
    }
    
    if ($missingDlls.Count -gt 0) {
        Write-Host "AVISO: DLLs faltando em ${dllPath}:" -ForegroundColor Yellow
        foreach ($dll in $missingDlls) {
            Write-Host "  - $dll" -ForegroundColor Yellow
        }
        return $false
    }
    
    if ($wrongArchDlls.Count -gt 0) {
        Write-Host "AVISO: DLLs com arquitetura incompatível em ${dllPath}:" -ForegroundColor Yellow
        Write-Host "O sistema está configurado para Java 32 bits, mas as seguintes DLLs são 64 bits:" -ForegroundColor Yellow
        foreach ($dll in $wrongArchDlls) {
            Write-Host "  - $dll" -ForegroundColor Yellow
        }
        Write-Host "Por favor, substitua as DLLs por versões 32 bits ou use Java 64 bits" -ForegroundColor Yellow
        return $false
    }
    
    Write-Host "Componentes do SDK encontrados em: $dllPath" -ForegroundColor Green
    Write-Host "Todas as DLLs são 32 bits e compatíveis com o Java" -ForegroundColor Green
    return $true
}

# Função para verificar e criar estrutura de diretórios
function Setup-Directories {
    $directories = @(
        "lib",
        "lib\dll",
        "lib\dll\sdk",
        "src\main\java\com\sistema\ponto\biometria"
    )
    
    foreach ($dir in $directories) {
        Test-DirectoryExists $dir
    }
    
    Write-Host "Estrutura de diretórios criada com sucesso!" -ForegroundColor Green
}

# Função para verificar ZKFingerReader.jar
function Check-JAR {
    if (-not (Test-Path "lib\ZKFingerReader.jar")) {
        Write-Host "ERRO: ZKFingerReader.jar não encontrado em lib\" -ForegroundColor Red
        Write-Host "Por favor, copie o arquivo ZKFingerReader.jar para a pasta lib\" -ForegroundColor Yellow
        return $false
    }
    
    Write-Host "ZKFingerReader.jar encontrado!" -ForegroundColor Green
    return $true
}

# Função principal
function Main {
    Write-Host "=== Configuração do Sistema de Ponto ===" -ForegroundColor Cyan
    Write-Host "Verificando requisitos..." -ForegroundColor White
    
    $success = $true
    
    # Verifica Java
    if (-not (Test-Java)) {
        $success = $false
    }
    
    # Verifica Maven
    if (-not (Test-Maven)) {
        $success = $false
    }
    
    # Cria estrutura de diretórios
    Setup-Directories
    
    # Verifica componentes do SDK
    if (-not (Test-SDK)) {
        $success = $false
    }
    
    # Verifica JAR
    if (-not (Check-JAR)) {
        $success = $false
    }
    
    Write-Host "`n=== Resultado da Verificação ===" -ForegroundColor Cyan
    if ($success) {
        Write-Host "Ambiente configurado com sucesso!" -ForegroundColor Green
        Write-Host "Você pode compilar o projeto usando: mvn clean package" -ForegroundColor White
        Write-Host "Para executar o programa de teste: .\run.ps1" -ForegroundColor White
    } else {
        Write-Host "Existem problemas na configuração que precisam ser resolvidos." -ForegroundColor Red
        Write-Host "Por favor, corrija os problemas indicados acima e execute este script novamente." -ForegroundColor Yellow
    }
}

# Executa o script
Main 