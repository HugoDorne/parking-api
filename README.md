# Parking API

### Objectif

Le but de l’exercice est de : développer une application “serveur” exposant une API REST pour permettre à une
application mobile, ou un site Web, d’afficher la liste des parkings à proximité comme illustré sur l’écran ci-dessous :
Ici, on utilisera la source de données suivante disponible à Poitiers pour récupérer la liste des parkings et le nombre
de places disponibles du parking en temps réel:

https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines

### Consignes

L’application doit pouvoir fonctionner dans d’autres villes : l’URL et format des données parking pourront donc être
complètement différents, cependant l’API REST exposée pour l’application mobile ne devra pas évoluer.
L’application à développer dans le cadre de l’exercice comporte uniquement la partie serveur (il n’est pas demandé de
développer l’application mobile).

### Livrables

Les livrables que nous attendons sont :

Le code source de l’application (via Github par exemple)
Un document (type Readme) expliquant :
Les choix que vous avez faits,
Les problèmes que vous n’avez peut-être pas traités, mais que vous avez identifiés,
Toute autre information utile pour apprécier votre travail.


## Solution développée

### Architecture

L'application a été développée en utilisant l'**architecture hexagonale** (Ports & Adapters) qui permet :
- **Indépendance du domaine** : La logique métier est isolée des détails techniques
- **Facilité de test** : Le domaine peut être testé sans dépendances externes
- **Extensibilité** : Possibilité d'ajouter facilement de nouveaux adaptateurs pour d'autres villes

#### Structure du projet

```
com.github.hugodorne.parkingapi/
├── domain/                          # Couche domaine (logique métier)
│   ├── model/                       # Entités du domaine
│   │   ├── Parking.java            # Modèle métier d'un parking
│   │   └── ParkingStatus.java      # Énumération des statuts
│   ├── port/                        # Ports (interfaces)
│   │   ├── in/                      # Ports d'entrée (use cases)
│   │   │   └── GetParkingsUseCase.java
│   │   └── out/                     # Ports de sortie
│   │       └── ParkingDataPort.java
│   └── service/                     # Services du domaine
│       └── ParkingService.java      # Implémentation de la logique métier
├── infrastructure/                  # Couche infrastructure (adaptateurs)
│   ├── adapter/
│   │   ├── in/                      # Adaptateurs d'entrée
│   │   │   └── rest/               # API REST
│   │   │       ├── ParkingController.java
│   │   │       └── ParkingResponse.java
│   │   └── out/                     # Adaptateurs de sortie
│   │       └── poitiers/           # Adaptateur pour Poitiers
│   │           ├── PoitiersParkingAdapter.java
│   │           ├── PoitiersApiResponse.java
│   │           └── PoitiersParkingProperties.java
│   └── config/                      # Configuration Spring
│       ├── DomainConfig.java
│       ├── CacheConfig.java
│       └── RestTemplateConfig.java
└── ParkingApiApplication.java
```

### API REST exposée

L'API est stable et ne changera pas même si on ajoute d'autres sources de données.

#### Endpoints

1. **GET /api/parkings**
   - Récupère tous les parkings disponibles
   - Réponse : Liste de parkings avec leurs informations

2. **GET /api/parkings/nearby?latitude={lat}&longitude={lon}&radius={km}**
   - Récupère les parkings à proximité d'une position
   - Paramètres :
     - `latitude` : Latitude de la position (obligatoire, -90.0 à 90.0)
     - `longitude` : Longitude de la position (obligatoire, -180.0 à 180.0)
     - `radius` : Rayon de recherche en km (optionnel, par défaut 5.0)
   - Réponse : Liste de parkings triés par distance

#### Exemple de réponse

```json
[
  {
    "id": "Parking Centre-Ville",
    "name": "Parking Centre-Ville",
    "address": "Place du Maréchal Leclerc",
    "latitude": 46.580224,
    "longitude": 0.340375,
    "totalSpaces": 500,
    "availableSpaces": 123,
    "status": "OPEN",
    "isOpen": true,
    "occupancyRate": 0.754
  }
]
```

### Fonctionnalités techniques

#### 1. Cache
- **Implémentation** : Spring Cache avec `ConcurrentMapCache`
- **Stratégie** : Les données des parkings sont mises en cache au niveau de l'adaptateur
- **Justification** : Les données de parkings changent fréquemment mais pas à chaque seconde. Le cache réduit la charge sur l'API externe
- **Configuration** : Cache nommé "parkings" configuré dans `CacheConfig.java`
- **Note** : Pour un environnement de production, il faudrait ajouter une expiration du cache (ex: 2 minutes) avec Redis ou Caffeine

#### 2. Calcul de distance
- **Formule** : Haversine
- **Fonction** : Calcule la distance entre deux points GPS en kilomètres
- **Usage** : Filtrage et tri des parkings à proximité

#### 3. Validation
- Validation des paramètres d'entrée avec `jakarta.validation`
- Latitude : entre -90.0 et 90.0
- Longitude : entre -180.0 et 180.0
- Radius : valeur positive

#### 4. Gestion des erreurs
- Logging des erreurs lors de l'appel à l'API externe
- Retour d'une liste vide en cas d'erreur (plutôt qu'une exception)
- Timeouts configurés sur le RestTemplate (10 secondes)

### Extensibilité pour d'autres villes

Pour ajouter une nouvelle ville (par exemple Lyon) :

1. **Créer un nouveau package** : `infrastructure.adapter.out.lyon`

2. **Créer l'adaptateur** :
```java
@Component
public class LyonParkingAdapter implements ParkingDataPort {
    @Override
    @Cacheable("parkings")
    public List<Parking> fetchParkings() {
        // Appel à l'API de Lyon
        // Mapping vers le modèle Parking du domaine
    }
}
```

3. **Configurer la source de données** dans `application.properties`

4. **Sélectionner l'adaptateur** via un profil Spring ou configuration

**Point clé** : L'API REST exposée ne change pas, seul l'adaptateur de sortie change.

### Choix techniques

#### Technologies utilisées
- **Spring Boot 3.5.6** : Framework principal
- **Java 21** : Version LTS récente
- **Lombok** : Réduction du boilerplate
- **Spring Web** : API REST
- **Spring Validation** : Validation des entrées
- **Spring Cache** : Mise en cache

#### Patterns appliqués
1. **Hexagonal Architecture** : Séparation claire domaine/infrastructure
2. **Dependency Inversion** : Le domaine définit les ports, l'infrastructure implémente
3. **Builder Pattern** : Construction des objets immuables (via Lombok)
4. **Strategy Pattern** : Différents adaptateurs peuvent implémenter ParkingDataPort

### Problèmes identifiés et améliorations possibles

#### Problèmes identifiés
1. **Cache sans expiration** : Le cache actuel n'expire jamais, il faudrait ajouter une expiration automatique
2. **Pas de gestion de circuit breaker** : Si l'API externe est down, l'application continue d'appeler

#### Améliorations possibles
1. **Cache avec expiration** :
   - Ajouter Redis (ou autre cache) avec TTL de 2 minutes
   - Invalidation manuelle via endpoint admin

2. **Résilience** :
   - Retry quand erreur sur API de Poitiers
   - Fallback sur données en cache même expirées

3. **Multi-source** :
   - Pattern Strategy avec sélection dynamique de l'adaptateur
   - Configuration par ville dans `application.yml`
   - Possibilité d'agréger plusieurs sources

4. **Tests** :
   - Tests d'intégration avec Mock du server Poitiers plus poussé
   - Tests de contrat pour l'API

5. **Documentation** :
   - OpenAPI/Swagger pour l'API REST
   - Exemples de requêtes

6. **Sécurité** :
   - Spring Security
   - Rate limiting
   - Authentification si nécessaire
   - CORS configuré

7. **Observabilité** :
   - Spring Boot Actuator
   - Logs structurés (JSON)
   - Tracing distribué

### Comment exécuter l'application

1. **Compiler** :
```bash
mvnw clean package
```

2. **Exécuter** :
```bash
mvnw spring-boot:run
```

3. **Tester** :
```bash
# Tous les parkings
curl http://localhost:8080/api/parkings

# Parkings à proximité de Poitiers centre
curl "http://localhost:8080/api/parkings/nearby?latitude=46.580224&longitude=0.340375&radius=5"
```

### Configuration

La configuration se trouve dans `application.properties` :
- `parking.data-source.poitiers.url` : URL de l'API Poitiers
- `spring.cache.*` : Configuration du cache

Pour changer de ville, il suffit de modifier l'URL ou d'activer un autre adaptateur.
