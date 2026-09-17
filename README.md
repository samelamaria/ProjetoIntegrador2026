# Orça · Gestão de usuários e orçamentos

Projeto didático com Spring Boot 3, Java 17, Kotlin, Thymeleaf e MySQL 8.4 no Docker. Os testes usam H2 isolado. O cálculo de ICMS utiliza o padrão Strategy.

## Executar

Requisitos: JDK 17, Docker Desktop iniciado (containers Linux) e acesso à internet na primeira execução.

No PowerShell, na pasta do projeto, inicie primeiro o banco:

```powershell
docker compose up -d --wait
.\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080/usuarios. A raiz também redireciona para essa página.

## Interface

- Layout responsivo para computador e celular.
- Cadastro com nome obrigatório e limite de 100 caracteres.
- CPF obrigatório, com validação dos dígitos verificadores e armazenamento sem pontuação.
- Data de nascimento obrigatória, sem permitir datas futuras.
- Mensagens de sucesso e validação junto ao formulário.
- Lista ordenada pelos cadastros mais recentes.
- Busca local por nome, CPF (com ou sem pontuação) ou ID, sem distinguir acentos.
- Navegação por teclado e suporte à preferência de movimento reduzido.
- CSS em `src/main/resources/static/css/style.css`.
- JavaScript em `src/main/resources/static/js/usuarios.js`.
- Template em `src/main/resources/templates/usuarioPage.html`.

O cadastro e a listagem funcionam mesmo sem JavaScript; a busca é uma melhoria progressiva.

## API de orçamentos

| Método | Rota | Ação |
| --- | --- | --- |
| GET | /orcamentos | Listar |
| GET | /orcamentos/pesquisaid/{id} | Consultar |
| POST | /orcamentos | Criar |
| POST | /orcamentos/put/{id} | Atualizar |
| DELETE | /orcamentos/delete/{id} | Excluir |

Exemplo de corpo JSON:

```json
{
  "icmsEstados": "ICMS_SP",
  "valorOrcamento": 100.00,
  "usuario": { "id": 1 }
}
```

O usuário é opcional; quando informado, deve existir. O valor de ICMS é calculado no servidor na criação e na atualização, com arredondamento de duas casas decimais. As taxas originais do exercício foram mantidas: MG 18%, SP 12% e RJ 15%; são exemplos didáticos.

Criação retorna 201, exclusão retorna 204, dados inválidos retornam 400 e registros não encontrados retornam 404. As rotas existentes foram preservadas.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes verificam a inicialização da aplicação, renderização do template, recursos estáticos, validação do cadastro, escape de HTML e operações de orçamento com recálculo de ICMS.

Para verificar também a busca no JavaScript, com Node.js instalado:

```powershell
node --test src/test/js/usuarios.test.cjs
```

## Banco de dados MySQL no Docker

O serviço está definido em `compose.yaml`. O MySQL usa um volume Docker persistente: os dados permanecem ao reiniciar a aplicação ou recriar o container.

As credenciais locais estão no arquivo `.env`, ignorado pelo Git. O Spring Boot e o Docker Compose leem esse mesmo arquivo. Execute os comandos a partir da raiz do projeto.

Em outra máquina, copie `.env.example` para `.env` e defina senhas próprias antes de iniciar:

```powershell
Copy-Item .env.example .env
notepad .env
docker compose up -d --wait
.\mvnw.cmd spring-boot:run
```

Não sobrescreva um `.env` existente: trocar as variáveis após a criação do volume não altera automaticamente as senhas do banco.

### Conexão pelo MySQL Workbench ou DBeaver

- Host: `127.0.0.1`
- Porta: `3306` (ou `MYSQL_PORT` do `.env`)
- Banco: `orcamento`
- Usuário: `orca`
- Senha: valor de `MYSQL_PASSWORD` no `.env`

A aplicação usa um usuário próprio; a senha root é separada. A porta do banco fica acessível apenas neste computador. O Hibernate cria e atualiza as tabelas quando a aplicação inicia.

### Comandos úteis

```powershell
docker compose ps
docker compose logs --tail 50 mysql
docker compose stop
docker compose start
```

`docker compose down` remove o container e mantém o volume. **Não use `docker compose down -v` se quiser manter os cadastros**, pois esse comando remove o volume.

O console H2 não é mais usado na aplicação. Os cadastros do antigo H2 em memória não são migrados automaticamente. Os testes Maven usam H2 em memória e não acessam o MySQL.

Se a porta 3306 estiver ocupada, altere `MYSQL_PORT` no `.env`, execute `docker compose up -d --wait` e reinicie a aplicação. Para atualizar a aplicação em execução, pressione Ctrl+C no terminal dela e execute novamente `.\mvnw.cmd spring-boot:run`.
