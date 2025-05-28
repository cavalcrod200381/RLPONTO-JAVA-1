# Módulo de Captura Biométrica - Sistema de Ponto Eletrônico

## 📝 Descrição do Projeto

Este módulo faz parte do Sistema de Ponto Eletrônico completo da AiNexus Tecnologia, atuando como uma bridge entre o leitor biométrico ZK4500 e a aplicação web principal. 

### Integração com o Sistema Principal
Este módulo é responsável por:
- Captura e validação de impressões digitais em tempo real
- Comunicação direta com o hardware biométrico
- Envio das digitais capturadas para o sistema web principal
- Interface desktop para operações locais de captura

### Sistema Web Principal (Integração)
O sistema web principal (documentado separadamente) contém:
- Cadastro e gestão de funcionários
- Controle de ponto e frequência
- Dashboard administrativo
- Relatórios e análises
- Gestão de horários e turnos

O módulo de captura biométrica se comunica com o sistema web através de APIs REST, enviando as digitais capturadas e recebendo confirmações de processamento.

## 🛠️ Tecnologias Utilizadas

### Linguagens e Frameworks
- Java 8 (32 bits)
- Maven para gerenciamento de dependências
- Swing para interface gráfica
- Log4j para gerenciamento de logs
- JUnit para testes unitários

### Hardware
- Leitor Biométrico ZK4500
- SDK ZKFinger 5.3.0.33 ou superior
- Resolução de captura: 500 DPI
- Interface: USB 2.0

### Bibliotecas Principais
- ZKFingerReader.jar - SDK Java para comunicação com o leitor biométrico
- Conjunto de DLLs nativas do ZKFinger SDK:
  - libzkfp.dll - Biblioteca principal do SDK
  - libzkfpcsharp.dll - Interface C#
  - libsilkidcap.dll - Captura de imagem
  - zkfputil.dll - Utilitários do SDK

## 🗂️ Estrutura do Projeto

```
sistema-de-ponto/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── sistema/
│                   └── ponto/
│                       ├── biometria/    # Lógica de captura biométrica
│                       ├── ui/           # Interfaces gráficas
│                       └── modelo/       # Classes de modelo
├── lib/
│   ├── ZKFingerReader.jar
│   └── dll/
│       └── sdk/          # DLLs nativas do SDK
├── docs/                 # Documentação
├── digitais_capturadas/  # Armazenamento das digitais
├── setup.ps1            # Script de configuração
└── pom.xml             # Configuração Maven
```

## ⚙️ Requisitos do Sistema

### Hardware
- Processador: 1.8 GHz ou superior (x86 ou x64)
- Memória RAM: 4GB ou superior
- Espaço em disco: 500MB para instalação
- Porta USB 2.0 ou superior
- Leitor biométrico ZK4500

### Software
- Windows 10 (32 ou 64 bits)
- Java 8 (32 bits) - JDK ou JRE
- Maven 3.6 ou superior
- ZKFinger SDK 5.3.0.33 ou superior
- Drivers USB atualizados

## 🚀 Instalação e Configuração

### 1. Configuração Automática
```powershell
# Execute como administrador
.\setup.ps1
```

O script realizará automaticamente:
- Verificação do Java 32 bits
- Verificação do Maven
- Verificação do SDK
- Configuração da estrutura de diretórios
- Verificação das DLLs necessárias

### 2. Configuração Manual

1. **Java 32 bits**:
   ```powershell
   # Configure JAVA_HOME
   $env:JAVA_HOME = 'C:\Program Files (x86)\Java\jdk1.8.0_452'
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   ```

2. **Maven**:
   - Baixe e instale o Maven
   - Adicione ao PATH do sistema

3. **SDK e DLLs**:
   - Instale o ZKFinger SDK
   - Copie as DLLs para `lib/dll/sdk`
   - Copie `ZKFingerReader.jar` para `lib/`

## 🏃 Execução do Projeto

### Compilação
```bash
mvn clean package
```

### Execução
```bash
java -jar target/sistema-de-ponto-1.0-SNAPSHOT.jar
```

## 📖 Fluxo de Uso

1. **Inicialização**
   - Conecte o leitor biométrico à porta USB
   - Aguarde a detecção do dispositivo pelo Windows
   - Inicie o aplicativo via linha de comando ou executável
   - Clique em "Inicializar" para estabelecer conexão com o leitor

2. **Captura de Digital**
   - Posicione o dedo centralizado no leitor
   - Mantenha o dedo parado durante a captura
   - Aguarde a visualização na tela (qualidade mínima: 70%)
   - Clique em "Capturar Digital"
   - Aguarde confirmação de sucesso

3. **Armazenamento**
   - As digitais são salvas em `digitais_capturadas`
   - Formato: `digital_YYYYMMDD_HHMMSS.png`
   - Resolução: 500 DPI
   - Formato de imagem: PNG (sem compressão)

## 🔧 Convenções e Boas Práticas

### Código
- Nomenclatura em português para classes e métodos
- Documentação JavaDoc em métodos públicos
- Tratamento de exceções com logs detalhados
- Separação clara entre camadas (UI, lógica de negócio, acesso a hardware)

### Versionamento
- Branches: main (produção), develop (desenvolvimento)
- Commits seguindo padrão: `tipo: descrição`
  - feat: nova funcionalidade
  - fix: correção de bug
  - docs: documentação
  - refactor: refatoração

### Testes
- Testes unitários para lógica de negócio
- Testes de integração para comunicação com hardware
- Logs detalhados para depuração

## 🚨 Solução de Problemas Comuns

1. **Erro "DLL não encontrada"**
   - Verifique se todas as DLLs estão em `lib/dll/sdk`
   - Confirme o uso do Java 32 bits
   - Verifique a conexão USB do leitor

2. **Erro "Leitor não encontrado"**
   - Reconecte o dispositivo
   - Verifique os drivers no Gerenciador de Dispositivos
   - Reinicie o aplicativo

3. **Erro "Falha ao inicializar SDK"**
   - Execute `setup.ps1` para diagnóstico
   - Verifique a instalação do SDK
   - Confirme a compatibilidade das versões das DLLs

## 📋 Manutenção

### Logs
- Arquivos de log em `logs/`
  - `app.log` - Log principal da aplicação
  - `error.log` - Registro de erros
  - `device.log` - Comunicação com o leitor
- Rotação diária de logs (máximo 30 dias)
- Nível de log configurável em `application.properties`

### Backup
- Backup automático das digitais
- Configuração em `application.properties`:
  ```properties
  backup.directory=backups/
  backup.retention.days=90
  backup.schedule=0 0 2 * * ?  # Todo dia às 2h
  ```
- Retenção configurável dos backups

### Monitoramento
- Logs de performance
  - Tempo de resposta do dispositivo
  - Qualidade das capturas
  - Uso de memória
- Métricas de uso do hardware
- Alertas configuráveis via email

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'feat: Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Copyright © 2025 AiNexus Tecnologia

Este software é propriedade da AiNexus Tecnologia e seu código fonte está protegido por direitos autorais. 
Desenvolvido por Richardson Rodrigues (Proprietário).

Todos os direitos reservados. Este software não pode ser copiado, modificado, mesclado, publicado, distribuído, sublicenciado e/ou vendido sem permissão expressa da AiNexus Tecnologia.

Para informações sobre licenciamento comercial, entre em contato:
- Email: contato@ainexus.com.br
- Website: www.ainexus.com.br

## 📞 Suporte

- Email: [contato@ainexus.com.br]
- Issues: Utilize o sistema de issues do GitHub
- Wiki: Documentação detalhada na wiki do projeto
- Horário de suporte: Segunda a Sexta, 9h às 18h
- Empresa: AiNexus Tecnologia
- Desenvolvedor responsável: Richardson Rodrigues 