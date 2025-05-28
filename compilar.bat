@echo off
echo === Iniciando Compilacao do Sistema ===
echo.

:: Cria pasta para logs se não existir
if not exist "compilador" mkdir compilador

:: Data e hora para o nome do arquivo de log
set datetime=%date:~6,4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set datetime=%datetime: =0%

:: Executa o Maven e salva o log
echo Compilando o sistema...
call mvn clean package > "compilador\log_compilacao_%datetime%.txt" 2>&1

:: Verifica se a compilação foi bem sucedida
if %ERRORLEVEL% EQU 0 (
    echo Compilacao concluida com sucesso!
    echo Log salvo em: compilador\log_compilacao_%datetime%.txt
) else (
    echo ERRO na compilacao!
    echo Verifique o log em: compilador\log_compilacao_%datetime%.txt
)

echo.
echo === Processo de Compilacao Finalizado ===
pause 