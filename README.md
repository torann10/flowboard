# FlowBoard

FlowBoard egy idő- és feladatkövető alkalmazás.

## Előfeltételek

- **Java 21** (backend futtatásához)
- **Maven** (backend build-hez)
- **Node.js** és **npm** (frontend futtatásához)
- **Docker** és **Docker Compose** (adatbázis és Keycloak indításához)

## Lokális indítás

### 1. Adatbázis és Keycloak indítása

Az adatbázis és a Keycloak autentikációs szerver Docker Compose segítségével indítható:

```bash
cd docker
docker-compose up -d
```

Ez elindítja:
- **PostgreSQL** adatbázist a `5432` porton
- **Keycloak** autentikációs szervert a `9090` porton
  - Admin felhasználónév: `admin`
  - Admin jelszó: `password`

### 2. Backend indítása

A Spring Boot backend alkalmazás indítása:

```bash
cd be
./mvnw spring-boot:run
```

A backend a `http://localhost:8080` címen érhető el.

### 3. Frontend indítása

Az Angular frontend alkalmazás indítása:

```bash
cd ui
npm install
npm run start
```

A frontend a `http://localhost:4200` címen érhető el.

## Production környezet

A production környezet elérhető a következő [címen.](https://app.flowboardthesis.hu)