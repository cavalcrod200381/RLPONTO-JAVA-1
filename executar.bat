@echo off
echo === Iniciando Sistema de Ponto com Leitor Biometrico ===
echo.

java -Djava.library.path="lib/dll/sdk" -cp "target/sistema-de-ponto-1.0-SNAPSHOT-jar-with-dependencies.jar;lib/ZKFingerReader.jar" com.sistema.ponto.biometria.LeitorSimples

echo.
echo === Sistema Finalizado ===
pause 