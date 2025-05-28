package com.sistema.ponto.biometria;

import com.zkteco.biometric.FingerprintSensorEx;

public class LeitorBiometrico {
    private static LeitorBiometrico instance;
    private boolean initialized = false;
    private long deviceHandle;
    private long dbHandle;
    
    private LeitorBiometrico() {
        // Construtor privado para Singleton
    }
    
    public static LeitorBiometrico getInstance() {
        if (instance == null) {
            instance = new LeitorBiometrico();
        }
        return instance;
    }
    
    private int byteArrayToInt(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
               ((bytes[2] & 0xFF) << 16) |
               ((bytes[1] & 0xFF) << 8) |
               (bytes[0] & 0xFF);
    }
    
    public boolean inicializar() {
        if (!initialized) {
            try {
                // Finaliza qualquer instância anterior
                try {
                    FingerprintSensorEx.CloseDevice(deviceHandle);
                    FingerprintSensorEx.Terminate();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Ignora erros aqui, pois pode não haver dispositivo aberto
                }
                
                // Inicializa o SDK
                System.out.println("Inicializando SDK...");
                int ret = FingerprintSensorEx.Init();
                if (ret != 0) {
                    System.out.println("Erro ao inicializar SDK: " + ret);
                    return false;
                }
                
                // Espera um pouco para o dispositivo estar pronto
                Thread.sleep(1000);
                
                // Abre o dispositivo
                System.out.println("Abrindo dispositivo...");
                deviceHandle = FingerprintSensorEx.OpenDevice(0);
                if (deviceHandle == 0) {
                    System.out.println("Erro ao abrir dispositivo");
                    return false;
                }
                
                // Espera o dispositivo estabilizar
                Thread.sleep(500);
                
                // Obtém parâmetros da imagem
                byte[] paramValue = new byte[4];
                int[] size = new int[1];
                size[0] = 4;
                
                ret = FingerprintSensorEx.GetParameters(deviceHandle, 1, paramValue, size);
                int width = byteArrayToInt(paramValue);
                
                ret = FingerprintSensorEx.GetParameters(deviceHandle, 2, paramValue, size);
                int height = byteArrayToInt(paramValue);
                
                System.out.println("Dimensões do sensor: " + width + "x" + height);
                
                // Inicializa o banco de dados de templates
                dbHandle = FingerprintSensorEx.DBInit();
                if (dbHandle == 0) {
                    System.out.println("Erro ao inicializar banco de templates");
                    return false;
                }
                
                // Tenta ligar o LED verde
                paramValue = new byte[4];
                paramValue[0] = 1;
                ret = FingerprintSensorEx.SetParameters(deviceHandle, 101, paramValue, 4);
                if (ret != 0) {
                    System.out.println("Aviso: Não foi possível configurar o LED: " + ret);
                }
                
                initialized = true;
                System.out.println("Leitor inicializado com sucesso! Handle: " + deviceHandle);
                return true;
                
            } catch (Exception e) {
                System.out.println("Erro ao inicializar leitor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public boolean alternarLED(boolean vermelho) {
        try {
            if (!initialized) {
                if (!inicializar()) {
                    System.out.println("Falha ao inicializar o leitor");
                    return false;
                }
            }
            
            byte[] paramValue = new byte[4];
            paramValue[0] = (byte)(vermelho ? 2 : 1); // 1 = Verde, 2 = Vermelho
            
            int ret = FingerprintSensorEx.SetParameters(deviceHandle, 101, paramValue, 4);
            System.out.println("Resultado da alteração do LED: " + ret);
            
            return ret == 0;
        } catch (Exception e) {
            System.out.println("Erro ao alterar LED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void finalizar() {
        if (initialized) {
            try {
                // Desliga LED
                byte[] paramValue = new byte[4];
                paramValue[0] = 0;
                FingerprintSensorEx.SetParameters(deviceHandle, 101, paramValue, 4);
                
                if (dbHandle != 0) {
                    FingerprintSensorEx.DBFree(dbHandle);
                }
                
                FingerprintSensorEx.CloseDevice(deviceHandle);
                FingerprintSensorEx.Terminate();
                initialized = false;
                System.out.println("Leitor finalizado");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public long getDeviceHandle() {
        return deviceHandle;
    }
    
    public long getDBHandle() {
        return dbHandle;
    }
} 