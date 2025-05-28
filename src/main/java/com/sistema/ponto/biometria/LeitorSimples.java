package com.sistema.ponto.biometria;

import com.zkteco.biometric.FingerprintSensorEx;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LeitorSimples extends JFrame {
    private long deviceHandle;
    private boolean initialized;
    private int largura;
    private int altura;
    private volatile boolean visualizando = false;
    private BufferedImage ultimaImagem = null;
    
    private JLabel imageLabel;
    private JButton btnIniciar;
    private JButton btnCapturar;
    private JButton btnFinalizar;
    private JButton btnTesteBeep;
    private JLabel statusLabel;
    
    private static final String PASTA_DIGITAIS = "digitais_capturadas";
    
    public LeitorSimples() {
        super("Leitor Biometrico ZK4500");
        criarPastaDigitais();
        initComponents();
    }
    
    private void criarPastaDigitais() {
        File pasta = new File(PASTA_DIGITAIS);
        if (!pasta.exists()) {
            if (pasta.mkdir()) {
                log("Pasta 'digitais_capturadas' criada com sucesso!");
            } else {
                log("[ERRO] Não foi possível criar a pasta 'digitais_capturadas'");
            }
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Status no topo
        statusLabel = new JLabel("Aguardando inicializacao", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        
        // Area central para imagem da digital
        imageLabel = new JLabel("Posicione o dedo no leitor", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.add(imageLabel, BorderLayout.CENTER);
        
        // Painel de botoes
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        btnIniciar = new JButton("Inicializar");
        btnCapturar = new JButton("Capturar Digital");
        btnFinalizar = new JButton("Finalizar");
        btnTesteBeep = new JButton("Testar Beep");
        
        btnIniciar.setPreferredSize(new Dimension(100, 30));
        btnCapturar.setPreferredSize(new Dimension(100, 30));
        btnFinalizar.setPreferredSize(new Dimension(100, 30));
        btnTesteBeep.setPreferredSize(new Dimension(100, 30));
        
        btnCapturar.setEnabled(false);
        btnFinalizar.setEnabled(false);
        btnTesteBeep.setEnabled(false);
        
        buttonPanel.add(btnIniciar);
        buttonPanel.add(btnCapturar);
        buttonPanel.add(btnFinalizar);
        buttonPanel.add(btnTesteBeep);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        btnIniciar.addActionListener(e -> inicializarLeitor());
        btnCapturar.addActionListener(e -> capturarDigital());
        btnFinalizar.addActionListener(e -> finalizarLeitor());
        btnTesteBeep.addActionListener(e -> {
            if (initialized) {
                if (testarBeep()) {
                    log("Teste de beep executado com sucesso!");
                } else {
                    log("[ERRO] Falha no teste de beep");
                }
            } else {
                log("[ERRO] Leitor não inicializado");
            }
        });
        
        setContentPane(mainPanel);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                finalizarLeitor();
            }
        });
    }
    
    private void log(String message) {
        System.out.println(message);
    }
    
    private void atualizarStatus(String status, Color cor) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            statusLabel.setForeground(cor);
        });
    }
    
    private void inicializarLeitor() {
        new Thread(() -> {
            btnIniciar.setEnabled(false);
            atualizarStatus("Inicializando...", Color.BLUE);
            
            try {
                log("\n=== Iniciando Leitor Biometrico ===");
                
                // Finaliza qualquer instancia anterior
                log("Finalizando instancias anteriores...");
                FingerprintSensorEx.CloseDevice(deviceHandle);
                FingerprintSensorEx.Terminate();
                Thread.sleep(1000);
                
                // Inicializa o SDK
                log("Inicializando SDK...");
                int ret = FingerprintSensorEx.Init();
                log("Retorno da inicialização do SDK: " + ret);
                if (ret != 0) {
                    log("[ERRO] Falha ao inicializar SDK: " + ret);
                    atualizarStatus("Erro ao inicializar SDK", Color.RED);
                    btnIniciar.setEnabled(true);
                    return;
                }
                
                // Abre o dispositivo
                log("Abrindo dispositivo...");
                deviceHandle = FingerprintSensorEx.OpenDevice(0);
                log("Retorno do OpenDevice: " + deviceHandle);
                if (deviceHandle == 0) {
                    log("[ERRO] Falha ao abrir dispositivo");
                    atualizarStatus("Erro ao abrir dispositivo", Color.RED);
                    btnIniciar.setEnabled(true);
                    return;
                }
                log("Handle do dispositivo: " + deviceHandle);
                
                // Verifica se o dispositivo está respondendo
                byte[] paramValue = new byte[4];
                int[] size = new int[1];
                size[0] = 4;
                
                log("Verificando parâmetros do dispositivo...");
                ret = FingerprintSensorEx.GetParameters(deviceHandle, 1, paramValue, size);
                log("Retorno do GetParameters (largura): " + ret);
                if (ret != 0) {
                    log("[ERRO] Dispositivo não está respondendo: " + ret);
                    atualizarStatus("Erro de comunicação", Color.RED);
                    FingerprintSensorEx.CloseDevice(deviceHandle);
                    btnIniciar.setEnabled(true);
                    return;
                }
                
                // Obtém dimensões do sensor
                largura = byteArrayToInt(paramValue);
                
                ret = FingerprintSensorEx.GetParameters(deviceHandle, 2, paramValue, size);
                altura = byteArrayToInt(paramValue);
                
                log("Dimensões do sensor: " + largura + "x" + altura);
                
                initialized = true;
                log("Leitor inicializado com sucesso!");
                atualizarStatus("Pronto para captura", Color.GREEN);
                
                btnCapturar.setEnabled(true);
                btnFinalizar.setEnabled(true);
                btnTesteBeep.setEnabled(true);
                
                // Inicia a visualização contínua
                visualizando = true;
                iniciarVisualizacaoContinua();
                
            } catch (Exception e) {
                log("[ERRO] Falha ao inicializar leitor: " + e.getMessage());
                e.printStackTrace();
                atualizarStatus("Erro ao inicializar", Color.RED);
                btnIniciar.setEnabled(true);
            }
        }).start();
    }
    
    private void iniciarVisualizacaoContinua() {
        new Thread(() -> {
            byte[] imgbuf = new byte[largura * altura];
            
            while (visualizando && initialized) {
                try {
                    int ret = FingerprintSensorEx.AcquireFingerprintImage(deviceHandle, imgbuf);
                    
                    if (ret == 0) {
                        BufferedImage fingerImage = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_GRAY);
                        byte[] imgData = ((DataBufferByte) fingerImage.getRaster().getDataBuffer()).getData();
                        System.arraycopy(imgbuf, 0, imgData, 0, imgbuf.length);
                        
                        Image scaledImage = fingerImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                        ultimaImagem = fingerImage;
                        
                        SwingUtilities.invokeLater(() -> {
                            imageLabel.setIcon(new ImageIcon(scaledImage));
                            imageLabel.setText(null);
                        });
                    }
                    
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log("[ERRO] Falha durante visualização: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
    
    private void capturarDigital() {
        if (!initialized || ultimaImagem == null) {
            log("[ERRO] Nenhuma digital detectada para capturar");
            JOptionPane.showMessageDialog(this, "Posicione o dedo no leitor primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivo = PASTA_DIGITAIS + File.separator + "digital_" + timestamp + ".png";
            
            File outputfile = new File(nomeArquivo);
            ImageIO.write(ultimaImagem, "png", outputfile);
            
            log("Digital salva com sucesso: " + nomeArquivo);
            JOptionPane.showMessageDialog(this, "Digital capturada com sucesso!\nSalva em: " + nomeArquivo);
            
        } catch (Exception e) {
            log("[ERRO] Falha ao salvar digital: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar a digital!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int byteArrayToInt(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
               ((bytes[2] & 0xFF) << 16) |
               ((bytes[1] & 0xFF) << 8) |
               (bytes[0] & 0xFF);
    }
    
    private void finalizarLeitor() {
        if (initialized) {
            try {
                visualizando = false;
                Thread.sleep(200);
                
                log("\n=== Finalizando Leitor ===");
                FingerprintSensorEx.CloseDevice(deviceHandle);
                FingerprintSensorEx.Terminate();
                initialized = false;
                log("Leitor finalizado com sucesso");
                atualizarStatus("Leitor finalizado", Color.GRAY);
                
                btnIniciar.setEnabled(true);
                btnCapturar.setEnabled(false);
                btnFinalizar.setEnabled(false);
                btnTesteBeep.setEnabled(false);
                
                imageLabel.setIcon(null);
                imageLabel.setText("Posicione o dedo no leitor");
                
            } catch (Exception e) {
                log("[ERRO] Falha ao finalizar leitor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean testarBeep() {
        try {
            log("\n=== Testando Beep ===");
            log("Device Handle: " + deviceHandle);
            
            // Ativa o beep
            byte[] paramValue = new byte[4];
            paramValue[0] = 1;
            log("Enviando comando para ativar beep...");
            int ret = FingerprintSensorEx.SetParameters(deviceHandle, 2002, paramValue, 4);
            log("SetParameters (ativar beep) retornou: " + ret);
            
            if (ret != 0) {
                log("[ERRO] Falha ao ativar beep: " + ret);
                return false;
            }
            
            // Aguarda um pouco
            Thread.sleep(200);
            
            // Desativa o beep
            paramValue[0] = 0;
            log("Enviando comando para desativar beep...");
            ret = FingerprintSensorEx.SetParameters(deviceHandle, 2002, paramValue, 4);
            log("SetParameters (desativar beep) retornou: " + ret);
            
            if (ret != 0) {
                log("[ERRO] Falha ao desativar beep: " + ret);
                return false;
            }
            
            log("Teste de beep concluído com sucesso!");
            return true;
        } catch (Exception e) {
            log("[ERRO] Falha ao testar beep: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LeitorSimples().setVisible(true);
        });
    }
}