package com.sistema.ponto;

import com.zkteco.biometric.FingerprintSensorEx;

public class TesteSimples {
    public static void main(String[] args) {
        System.out.println("Iniciando teste do leitor biométrico...");
        
        try {
            // Inicializa o SDK
            System.out.println("Tentando inicializar o SDK...");
            int ret = FingerprintSensorEx.Init();
            System.out.println("Inicialização do SDK: " + (ret == 0 ? "OK" : "Falha - código " + ret));
            
            // Obtém o número de leitores conectados
            System.out.println("Verificando leitores conectados...");
            int deviceCount = FingerprintSensorEx.GetDeviceCount();
            System.out.println("Leitores encontrados: " + deviceCount);
            
            if (deviceCount > 0) {
                // Abre o primeiro leitor
                System.out.println("Tentando abrir o primeiro leitor...");
                long devHandle = FingerprintSensorEx.OpenDevice(0);
                if (devHandle != 0) {
                    System.out.println("Leitor aberto com sucesso! Handle: " + devHandle);
                    
                    // Obtém informações do leitor
                    byte[] paramValue = new byte[4];
                    int[] size = new int[1];
                    size[0] = 4;
                    ret = FingerprintSensorEx.GetParameters(devHandle, 1, paramValue, size);
                    System.out.println("Parâmetros do leitor: " + ret);
                    
                    // Fecha o leitor
                    System.out.println("Fechando o leitor...");
                    FingerprintSensorEx.CloseDevice(devHandle);
                    System.out.println("Leitor fechado.");
                } else {
                    System.out.println("Falha ao abrir o leitor!");
                }
            }
            
            // Finaliza o SDK
            System.out.println("Finalizando SDK...");
            FingerprintSensorEx.Terminate();
            System.out.println("SDK finalizado.");
            
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Erro ao carregar as DLLs: " + e.getMessage());
            System.out.println("java.library.path: " + System.getProperty("java.library.path"));
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 