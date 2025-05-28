package com.sistema.ponto;

import com.sistema.ponto.biometria.LeitorBiometrico;
import com.sistema.ponto.biometria.GerenciadorDigital;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TesteLeitorBiometrico {
    private static JFrame janela;
    private static JLabel labelImagem;
    private static JLabel labelStatus;
    private static GerenciadorDigital gerenciador;
    private static byte[] ultimoTemplate;
    private static boolean capturando = false;
    private static JButton btnCapturar;
    private static JButton btnSalvar;
    private static JButton btnDescartar;
    private static JButton btnTestarLED;
    private static boolean ledVermelho = false;
    private static BufferedImage imagemAtual;
    
    public static void main(String[] args) {
        try {
            // Configura o look and feel nativo do sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Cria o gerenciador de digitais
            gerenciador = GerenciadorDigital.getInstance();
            
            // Cria a interface gráfica
            criarInterface();
            
            // Configura os listeners
            gerenciador.setImagemListener(imagem -> {
                imagemAtual = imagem;
                SwingUtilities.invokeLater(() -> {
                    // Redimensiona a imagem para o tamanho do label
                    Image imagemRedimensionada = imagemAtual.getScaledInstance(
                        labelImagem.getWidth(), 
                        labelImagem.getHeight(), 
                        Image.SCALE_SMOOTH);
                    labelImagem.setIcon(new ImageIcon(imagemRedimensionada));
                    btnCapturar.setEnabled(true);
                    
                    // Força o repaint do label
                    labelImagem.repaint();
                });
            });
            
            gerenciador.setQualidadeListener((qualidade, mensagem) -> {
                SwingUtilities.invokeLater(() -> {
                    labelStatus.setText(mensagem + " - Qualidade: " + qualidade + "%");
                    
                    // Atualiza a cor do status baseado na qualidade
                    if (qualidade >= 80) {
                        labelStatus.setForeground(new Color(0, 150, 0)); // Verde escuro
                    } else if (qualidade >= 60) {
                        labelStatus.setForeground(new Color(0, 100, 0)); // Verde médio
                    } else if (qualidade >= 40) {
                        labelStatus.setForeground(Color.BLUE);
                    } else {
                        labelStatus.setForeground(Color.RED);
                    }
                });
            });
            
            // Inicia a captura
            if (!gerenciador.iniciarCaptura()) {
                JOptionPane.showMessageDialog(null, "Falha ao inicializar o leitor biométrico!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            System.out.println("Captura iniciada com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void criarInterface() {
        // Cria a janela principal
        janela = new JFrame("Teste do Leitor Biométrico");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLayout(new BorderLayout(10, 10));
        
        // Painel para a imagem
        JPanel painelImagem = new JPanel(new BorderLayout());
        painelImagem.setBorder(BorderFactory.createTitledBorder("Digital"));
        labelImagem = new JLabel();
        labelImagem.setPreferredSize(new Dimension(242, 266)); // Tamanho padrão do ZK4500
        labelImagem.setMinimumSize(new Dimension(242, 266));
        labelImagem.setMaximumSize(new Dimension(242, 266));
        labelImagem.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        labelImagem.setHorizontalAlignment(SwingConstants.CENTER);
        labelImagem.setVerticalAlignment(SwingConstants.CENTER);
        painelImagem.add(labelImagem, BorderLayout.CENTER);
        
        // Painel de status e botões
        JPanel painelControles = new JPanel(new BorderLayout(5, 5));
        painelControles.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Label de status
        labelStatus = new JLabel("Aguardando dedo...");
        labelStatus.setFont(new Font("Arial", Font.BOLD, 14));
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);
        painelControles.add(labelStatus, BorderLayout.NORTH);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Botão de teste do LED
        btnTestarLED = new JButton("Testar LED (Verde)");
        btnTestarLED.addActionListener(e -> {
            ledVermelho = !ledVermelho;
            if (gerenciador.alternarLED(ledVermelho)) {
                btnTestarLED.setText("Testar LED (" + (ledVermelho ? "Vermelho" : "Verde") + ")");
                labelStatus.setText("LED alterado com sucesso!");
            } else {
                labelStatus.setText("Falha ao alterar LED!");
                labelStatus.setForeground(Color.RED);
            }
        });
        
        btnCapturar = new JButton("Capturar");
        btnCapturar.setEnabled(false);
        btnCapturar.addActionListener(e -> {
            btnCapturar.setEnabled(false);
            ultimoTemplate = gerenciador.getTemplateAtual();
            if (ultimoTemplate != null) {
                btnSalvar.setEnabled(true);
                btnDescartar.setEnabled(true);
                labelStatus.setText("Digital capturada com sucesso! Qualidade: " + gerenciador.getQualidadeDigital() + "%");
                labelStatus.setForeground(Color.GREEN);
            } else {
                labelStatus.setText("Falha ao capturar digital!");
                labelStatus.setForeground(Color.RED);
                btnCapturar.setEnabled(true);
            }
        });
        
        btnSalvar = new JButton("Salvar");
        btnSalvar.setEnabled(false);
        btnSalvar.addActionListener(e -> {
            // Implementar salvamento
            JOptionPane.showMessageDialog(janela, "Digital salva com sucesso!");
            resetarInterface();
        });
        
        btnDescartar = new JButton("Descartar");
        btnDescartar.setEnabled(false);
        btnDescartar.addActionListener(e -> resetarInterface());
        
        painelBotoes.add(btnTestarLED);
        painelBotoes.add(btnCapturar);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnDescartar);
        painelControles.add(painelBotoes, BorderLayout.CENTER);
        
        // Adiciona os painéis à janela
        janela.add(painelImagem, BorderLayout.CENTER);
        janela.add(painelControles, BorderLayout.SOUTH);
        
        // Configura e exibe a janela
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
        
        // Adiciona listener para fechar o leitor quando a janela for fechada
        janela.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gerenciador.pararCaptura();
                LeitorBiometrico.getInstance().finalizar();
            }
        });
    }
    
    private static void resetarInterface() {
        btnCapturar.setEnabled(false);
        btnSalvar.setEnabled(false);
        btnDescartar.setEnabled(false);
        labelStatus.setText("Aguardando dedo...");
        labelStatus.setForeground(Color.BLACK);
        labelImagem.setIcon(null);
        ultimoTemplate = null;
    }
} 