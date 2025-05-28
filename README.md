# RLPONTO-JAVA-1

Módulo de Captura Biométrica para o Sistema de Ponto Eletrônico da AiNexus Tecnologia.

## 📝 Sobre o Projeto

Este repositório contém o módulo bridge responsável pela comunicação entre o leitor biométrico ZK4500 e o sistema web de ponto eletrônico. Para documentação completa, consulte [docs/README.md](docs/README.md).

## 🔄 Processo de Desenvolvimento

### Fluxo de Trabalho
1. Crie uma nova branch a partir da `main` para sua feature/correção
2. Desenvolva e teste suas alterações localmente
3. Atualize a documentação em `docs/README.md` se necessário
4. Faça commit das alterações seguindo as convenções
5. Abra um Pull Request para revisão
6. Após aprovação e testes, será feito o merge na `main`

### Convenções de Commit
```
tipo: descrição curta

Descrição detalhada das alterações (se necessário)
```

Tipos de commit:
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Alteração na documentação
- `refactor`: Refatoração de código
- `test`: Adição/modificação de testes
- `chore`: Alterações em arquivos de configuração

### Documentação
- Toda alteração significativa deve ser documentada
- A documentação principal está em `docs/README.md`
- Alterações na API devem ser documentadas em `docs/api.md`
- Screenshots e diagramas devem ser salvos em `docs/images/`

## 🚀 Quick Start

1. Clone o repositório:
```bash
git clone https://github.com/cavalcrod200381/RLPONTO-JAVA-1.git
cd RLPONTO-JAVA-1
```

2. Configure o ambiente:
```powershell
.\setup.ps1
```

3. Compile o projeto:
```bash
mvn clean package
```

4. Execute:
```bash
java -jar target/sistema-de-ponto-1.0-SNAPSHOT.jar
```

## 📄 Licença

Copyright © 2025 AiNexus Tecnologia

Este software é propriedade da AiNexus Tecnologia. Todos os direitos reservados.
Para informações sobre licenciamento comercial:
- Email: contato@ainexus.com.br
- Website: www.ainexus.com.br

## 📞 Suporte

- Email: contato@ainexus.com.br
- Issues: Utilize o sistema de issues do GitHub
- Horário: Segunda a Sexta, 9h às 18h 