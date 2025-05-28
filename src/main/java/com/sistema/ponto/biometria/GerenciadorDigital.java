package com.sistema.ponto.biometria;

import com.zkteco.biometric.FingerprintSensorEx;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class GerenciadorDigital {
    private static GerenciadorDigital instance;
    private final LeitorBiometrico leitor;
    private byte[] imagemBuffer;
    private int larguraImagem;
    private int alturaImagem;
    private AtomicBoolean capturando;
    private int qualidadeDigital;
    private byte[] templateAtual;
    private QualidadeListener qualidadeListener;
    private ImagemListener imagemListener;
    
    public interface QualidadeListener {
        void onQualidadeAtualizada(int qualidade, String mensagem);
    }
    
    public interface ImagemListener {
        void onImagemCapturada(BufferedImage imagem);
    }
    
    private GerenciadorDigital() {
        leitor = LeitorBiometrico.getInstance();
        capturando = new AtomicBoolean(false);
        qualidadeDigital = 0;
        templateAtual = null;
    }
    
    public static GerenciadorDigital getInstance() {
        if (instance == null) {
            instance = new GerenciadorDigital();
        }
        return instance;
    }
    
    public void setQualidadeListener(QualidadeListener listener) {
        this.qualidadeListener = listener;
    }
    
    public void setImagemListener(ImagemListener listener) {
        this.imagemListener = listener;
    }
    
    public boolean iniciarCaptura() {
        if (!leitor.isInitialized()) {
            if (!leitor.inicializar()) {
                return false;
            }
        }
        
        if (!capturando.get()) {
            // Obtém parâmetros da imagem
            byte[] paramValue = new byte[4];
            int[] size = new int[1];
            
            FingerprintSensorEx.GetParameters(leitor.getDeviceHandle(), 1, paramValue, size);
            larguraImagem = byteArrayToInt(paramValue);
            
            FingerprintSensorEx.GetParameters(leitor.getDeviceHandle(), 2, paramValue, size);
            alturaImagem = byteArrayToInt(paramValue);
            
            System.out.println("Dimensões da imagem: " + larguraImagem + "x" + alturaImagem);
            System.out.println("Handle do dispositivo: " + leitor.getDeviceHandle());
            
            // Configura parâmetros do sensor
            paramValue = new byte[4];
            paramValue[0] = 1; // Alta velocidade
            FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 2001, paramValue, 4);
            
            paramValue[0] = 3; // Sensibilidade média-alta
            FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 4, paramValue, 4);
            
            paramValue[0] = 1; // LED ON
            FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 101, paramValue, 4);
            
            imagemBuffer = new byte[larguraImagem * alturaImagem];
            
            // Inicia thread de captura
            capturando.set(true);
            new Thread(this::threadCaptura).start();
            return true;
        }
        return false;
    }
    
    public void pararCaptura() {
        capturando.set(false);
    }
    
    private void threadCaptura() {
        System.out.println("Thread de captura iniciada");
        int falhasConsecutivas = 0;
        
        while (capturando.get()) {
            try {
                // Captura imagem
                int ret = FingerprintSensorEx.AcquireFingerprintImage(leitor.getDeviceHandle(), imagemBuffer);
                System.out.println("Resultado da captura: " + ret);
                
                if (ret == 0) {
                    System.out.println("Imagem capturada com sucesso");
                    falhasConsecutivas = 0;
                    
                    // Verifica se há pixels escuros suficientes para indicar presença de dedo
                    int pixelsEscuros = 0;
                    for (byte b : imagemBuffer) {
                        if ((b & 0xFF) < 128) {
                            pixelsEscuros++;
                            if (pixelsEscuros > 1000) { // Se encontrar mais de 1000 pixels escuros
                                break;
                            }
                        }
                    }
                    
                    System.out.println("Pixels escuros encontrados: " + pixelsEscuros);
                    
                    if (pixelsEscuros > 1000) {
                        // Calcula qualidade
                        qualidadeDigital = calcularQualidade(imagemBuffer);
                        System.out.println("Qualidade calculada: " + qualidadeDigital);
                        
                        String mensagem;
                        if (qualidadeDigital == -1) {
                            mensagem = "Digital suspeita detectada";
                        } else if (qualidadeDigital < 50) {
                            mensagem = "Qualidade ruim";
                        } else if (qualidadeDigital < 75) {
                            mensagem = "Qualidade média";
                        } else {
                            mensagem = "Qualidade boa";
                        }
                        
                        if (qualidadeListener != null) {
                            qualidadeListener.onQualidadeAtualizada(qualidadeDigital, mensagem);
                        }
                        
                        // Converte para BufferedImage e notifica listener
                        if (imagemListener != null) {
                            BufferedImage imagem = new BufferedImage(larguraImagem, alturaImagem, BufferedImage.TYPE_BYTE_GRAY);
                            byte[] pixels = ((java.awt.image.DataBufferByte) imagem.getRaster().getDataBuffer()).getData();
                            System.arraycopy(imagemBuffer, 0, pixels, 0, pixels.length);
                            imagemListener.onImagemCapturada(imagem);
                            System.out.println("Imagem enviada para o listener");
                        }
                    } else {
                        System.out.println("Poucos pixels escuros, provavelmente sem dedo");
                    }
                } else {
                    falhasConsecutivas++;
                    if (falhasConsecutivas > 10) {
                        System.out.println("Muitas falhas consecutivas, reiniciando leitor...");
                        leitor.finalizar();
                        Thread.sleep(1000);
                        if (leitor.inicializar()) {
                            falhasConsecutivas = 0;
                        }
                    }
                }
                Thread.sleep(100);
        } catch (Exception e) {
                System.out.println("Erro na thread de captura: " + e.getMessage());
            e.printStackTrace();
                break;
            }
        }
        System.out.println("Thread de captura finalizada");
    }
    
    private int calcularQualidade(byte[] imagem) {
        int pixelsEscuros = 0;
        int somaContraste = 0;
        int totalPixels = larguraImagem * alturaImagem;
        
        // Conta pixels escuros e calcula contraste
        for (int i = 0; i < totalPixels; i++) {
            int pixel = imagem[i] & 0xFF;
            if (pixel < 128) {
                pixelsEscuros++;
            }
            
            // Calcula contraste com o pixel vizinho (se não for borda)
            if (i < totalPixels - 1) {
                int nextPixel = imagem[i + 1] & 0xFF;
                somaContraste += Math.abs(pixel - nextPixel);
            }
        }
        
        // Calcula pontuação baseada em pixels escuros (40%) e contraste (60%)
        double percentualEscuro = (pixelsEscuros * 100.0) / totalPixels;
        double percentualContraste = (somaContraste * 100.0) / (totalPixels * 255);
        
        int pontuacaoEscuro = (int)((percentualEscuro > 15 ? 40 : (percentualEscuro * 40 / 15)));
        int pontuacaoContraste = (int)((percentualContraste > 30 ? 60 : (percentualContraste * 60 / 30)));
        
        return Math.min(100, pontuacaoEscuro + pontuacaoContraste);
    }
    
    public byte[] getTemplateAtual() {
        return templateAtual;
    }
    
    public int getQualidadeDigital() {
        return qualidadeDigital;
    }
    
    private int byteArrayToInt(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
               ((bytes[2] & 0xFF) << 16) |
               ((bytes[1] & 0xFF) << 8) |
               (bytes[0] & 0xFF);
    }
    
    public boolean verificarDigital(byte[] template1, byte[] template2) {
        if (template1 == null || template2 == null) {
            return false;
        }
        
        int ret = FingerprintSensorEx.DBMatch(leitor.getDBHandle(), template1, template2);
        return ret >= 50; // Score é retornado diretamente
    }
    
    public boolean identificarDigital(byte[] template) {
        if (template == null) {
            return false;
        }
        
        int[] score = new int[1];
        int[] processados = new int[1];
        int id = FingerprintSensorEx.DBIdentify(leitor.getDBHandle(), template, score, processados);
        
        return id >= 0;
    }

    public boolean alternarLED(boolean vermelho) {
        try {
            // Garante que o leitor está inicializado
            if (!leitor.isInitialized()) {
                if (!leitor.inicializar()) {
                    System.out.println("Falha ao inicializar o leitor");
                    return false;
                }
            }

            // Configura o LED
            byte[] paramValue = new byte[4];
            
            // Primeiro desliga o LED
            paramValue[0] = 0;
            int ret = FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 101, paramValue, 4);
            Thread.sleep(100); // Pequena pausa
            
            // Agora liga na cor desejada
            paramValue[0] = (byte)(vermelho ? 2 : 1); // 1 = Verde, 2 = Vermelho
            ret = FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 101, paramValue, 4);
            
            System.out.println("Resultado da alteração do LED: " + ret);
            
            // Se falhou, tenta reinicializar o leitor e tentar novamente
            if (ret != 0) {
                System.out.println("Tentando reinicializar o leitor...");
                leitor.finalizar();
                Thread.sleep(500);
                
                if (leitor.inicializar()) {
                    paramValue[0] = (byte)(vermelho ? 2 : 1);
                    ret = FingerprintSensorEx.SetParameters(leitor.getDeviceHandle(), 101, paramValue, 4);
                    System.out.println("Resultado após reinicialização: " + ret);
                }
            }
            
            return ret == 0;
        } catch (Exception e) {
            System.out.println("Erro ao alterar LED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 