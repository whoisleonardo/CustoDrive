# CustoDrive

Aplicação web para cadastrar e comparar carros em estudo de compra, calcular desembolso e custo mensal conforme a planilha original e importar fichas de fontes públicas gratuitas.

## Stack

- Spring Boot 3.4 + Java 21
- PostgreSQL 16 + Flyway
- React + TypeScript + Vite
- Docker Compose e Nginx

## Começar a usar

Requisitos: Docker Engine e Docker Compose v2.

```bash
docker compose up --build
```

Abra [http://localhost:8080](http://localhost:8080). Não é preciso cadastrar uma API paga, criar chave ou ter os serviços de catálogo externos acessíveis para usar o simulador. Se quiser personalizar as variáveis, copie `.env.example` para `.env`; para execução local, os valores padrão do Compose já bastam.

Na primeira abertura, crie uma conta com nome, e-mail e senha com pelo menos 8 caracteres. Cada usuário acessa apenas os seus carros. A senha é armazenada com BCrypt; o token Bearer expira em 72 horas por padrão.

Para parar, pressione `Ctrl+C` (modo foreground) ou execute `docker compose down`. O volume `postgres_data` preserva os dados. `docker compose down -v` também remove o volume e **apaga os dados**.

Antes de expor a aplicação à internet, substitua `JWT_SECRET` no `.env` por uma chave aleatória longa (`openssl rand -hex 32`), defina senhas próprias para o PostgreSQL e ajuste o CORS.

## Fontes gratuitas de dados de veículos

A interface **pesquisa primeiro os snapshots locais**, sem fazer chamadas externas:

- **Inmetro PBE Veicular 2026:** referências brasileiras de consumo e energia, conforme dados publicados no [site oficial](https://www.gov.br/inmetro/pt-br/assuntos/regulamentacao/avaliacao-da-conformidade/programa-brasileiro-de-etiquetagem/tabelas-de-eficiencia-energetica/veiculos-automotivos-pbe-veicular). O snapshot desta versão estruturou **855 registros**; a tabela oficial informa **976 modelos/versões**, portanto a cobertura local é parcial. Confirme a versão e os valores; veículos faltantes podem ser cadastrados manualmente ou consultados na EPA quando houver correspondência.
- **Open EV Data 1.24.0:** snapshot filtrado para mercado BR, proveniente do [dataset oficial](https://github.com/open-ev-data/open-ev-data-dataset), acompanhado de sua licença no diretório `backend/src/main/resources/data/`. Inclui campos disponíveis como bateria, dimensões e autonomia. O esquema não classifica claramente todos os híbridos; o app não infere PHEV somente pela presença de bateria.
- **FuelEconomy.gov / U.S. EPA:** consulta sob demanda por ano, marca e modelo à [API pública documentada](https://www.fueleconomy.gov/feg/ws/index.shtml). Pode trazer versões a combustão, híbridas, plug-in e elétricas, conforme classificação da EPA. São **dados do mercado e do ciclo EPA dos EUA**, que podem diferir de um veículo/consumo brasileiro; a consulta é explicitamente iniciada pelo usuário.
- **NHTSA vPIC:** decodificação opcional de VIN via [API pública](https://vpic.nhtsa.dot.gov/api/). O usuário precisa acionar a consulta e fornecer VIN. É uma fonte norte-americana e não garante dimensões, consumo ou todos os atributos.

Fontes públicas podem ter limites de tráfego ou indisponibilidade; consultas EPA e NHTSA ficam em cache em memória por 12 horas. O backend não depende dessas fontes para inicializar. Campos ausentes permanecem em branco; ano, origem e unidade são apresentados para conferência. **Consumos EPA são convertidos para L/100 km ou kWh/100 km** para o cálculo do simulador; compare com o ciclo/versão correta.

### Escolha de combustível e unidade

O formulário importa a opção PBEV/EPA selecionada (por exemplo, gasolina ou etanol) e preenche consumo/unidade quando a fonte fornece o valor. O preço por litro/kWh é sempre informado pelo usuário; consumo e preço de uma ficha incompleta ficam vazios, sem estimativa inventada. O simulador calcula uma fonte de energia por veículo; **não soma simultaneamente uso elétrico e combustível de um plug-in**. Para PHEV, compare um modo por vez e informe consumo/preço que representem seu padrão de uso.

### Preencher a ficha de um carro já salvo

Abrir o carro para edição **não dispara consultas automáticas**. Na seção “Ficha técnica”, clique **Buscar dados gratuitos para este carro**, confira ano/versão na lista e escolha “Aplicar ficha” ou uma opção explícita de consumo. O preenchimento atualiza o registro em edição sem criar duplicata e preserva preço, custos e consumo existente quando você aplica apenas a ficha. É possível aplicar campos complementares de mais de um snapshot, mas confira diferenças de ano/versão e as fontes listadas antes de salvar. Essa busca só consulta os snapshots locais; as consultas EPA e NHTSA continuam na aba Catálogo gratuito.

## O que a aplicação calcula

As fórmulas reproduzem a planilha:

- **Desembolso inicial:** preço à vista + custos iniciais.
- **IPVA mensal:** preço à vista × IPVA (%) ÷ 100 ÷ 12.
- **Seguro mensal:** seguro anual ÷ 12.
- **Manutenção mensal:** manutenção anual ÷ 12.
- **Energia/combustível:** consumo por 100 km × preço da unidade × quilometragem mensal ÷ 100.
- **Custo mensal:** IPVA + seguro + manutenção + energia/combustível + estacionamento + outros.
- **Gasto recorrente anual:** custo mensal × 12.

Não há financiamento, revenda ou depreciação. É possível criar vários carros, editar ou excluir sua seleção e comparar custos lado a lado.

## API interna

- `GET /api/health`
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- CRUD `/api/cars` (dados sempre restritos ao usuário autenticado)
- `GET /api/catalog/search?q=...&type=...&year=...&page=...` — pesquisa snapshots locais
- `GET /api/external/fueleconomy/options?year=...&make=...&model=...` — busca EPA sob demanda
- `GET /api/external/fueleconomy/vehicle/{id}` — ficha EPA sob demanda
- `GET /api/external/nhtsa/vin/{vin}?year=...` — decodificação VIN opcional

Todos os endpoints de catálogo exigem autenticação CustoDrive. Não existem chaves pagas nem chamadas pagas. Os provedores externos não fazem parte do startup e são acionados apenas pelos controles específicos da tela Catálogo.

## Desenvolvimento sem Docker

### Backend

Configure PostgreSQL e exporte `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`; em seguida:

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

O Vite usa `/api` e faz proxy para `http://localhost:8081` durante o desenvolvimento.

## Estrutura

```text
backend/      API Spring Boot, autenticação, cálculos, fontes e persistência
frontend/     React/Vite, painel, formulários, comparação e catálogo
backend/src/main/resources/data/  snapshots locais PBEV e Open EV Data
backend/scripts/                  importador reprodutível do PDF PBEV
backend/src/test/                 testes de cálculo e catálogo
```

O cálculo é uma ferramenta de comparação e depende dos preços, custos, versão do veículo, ciclo de ensaio, tarifa e perfil informados; confira dados relevantes para sua região antes de tomar uma decisão de compra.
