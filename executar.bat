@echo off
chcp 65001 > nul
echo ===================================================
echo           BANCO SAP - INICIALIZADOR
echo ===================================================
echo.

set "LOCAL_MVN=%~dp0maven\apache-maven-3.9.8\bin\mvn.cmd"

if exist "%LOCAL_MVN%" (
    echo [OK] Usando Maven local embutido...
    echo Iniciando o Banco SAP...
    echo Acesse no navegador: http://localhost:8080
    echo.
    call "%LOCAL_MVN%" spring-boot:run
) else (
    where mvn >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Maven encontrado no PATH do sistema!
        echo Iniciando o Banco SAP...
        echo Acesse no navegador: http://localhost:8080
        echo.
        mvn spring-boot:run
    ) else (
        echo [ERRO] Maven nao encontrado.
        pause
    )
)
