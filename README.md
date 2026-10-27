# 🍺 Beer Recipe

[![Licence : GPL v3](https://img.shields.io/badge/licence-GPLv3-blue.svg)](LICENSE)

Application Android de conception de recettes de bière pour le brassage amateur en tout grain.
Elle calcule densités, amertume, couleur, pH d'empâtage et volumes d'eau, compare la recette au
style visé et propose des corrections chiffrées.

> Application entièrement hors ligne : toutes les données restent sur l'appareil (base Room locale).

## Fonctionnalités

### Recettes
- Création, duplication, mise en favori et suppression de recettes
- Mise à l'échelle d'une recette vers un nouveau volume
- Édition par onglets : **Résumé**, **Maque**, **Houblons**, **Levures**, **Additifs**, **Procédé**, **Journal**
- Paliers d'empâtage et étapes de fermentation
- Journal de brassage : suivi des sessions et des mesures réelles

### Calculs
| Indicateur | Méthode |
|---|---|
| Densités initiale et finale (OG / FG) | Points d'extrait × rendement de l'installation, atténuation de la part fermentescible seulement |
| Alcool (ABV) | À partir de l'OG et de la FG |
| Amertume (IBU) | **Tinseth**, **Rager** ou **Garetz**, avec facteurs de forme (pellets, cryo, cônes…), premier moût, whirlpool selon la température, houblonnage à cru |
| Couleur | Formule de **Morey**, affichage en EBC avec rendu de la teinte |
| pH d'empâtage | Modèle par malt : pH en eau distillée et pouvoir tampon, estimés d'après le type et la couleur quand la fiche ne les précise pas |
| Volumes d'eau | Du concassage à l'embouteillage, pertes de l'installation comprises |
| Levure | Taux d'ensemencement : cellules nécessaires contre cellules apportées |
| Refermentation | Sucre de resucrage (saccharose ou dextrose) selon les volumes de CO₂ visés |
| Coût | Coût total et coût au litre |

### Conseiller de recette
Le conseiller compare les statistiques calculées aux plages du style et transforme chaque écart en
geste concret : combien de grammes de quel malt, combien de millilitres d'acide, quel volume ou
quelle température. Les quantités proposées sont obtenues en inversant les formules du moteur de
calcul. Il vérifie notamment la densité, l'amertume, la couleur, le pH, la fermentation, la maque,
l'installation et l'eau.

### Catalogue
Fiches modifiables, préremplies au premier lancement :
- Malts, houblons, levures et additifs
- Styles du guide **BJCP 2021**, couleurs converties en EBC
- Profils d'eau

### Installations
Description de son matériel (volumes, pertes, rendement…) utilisée par les calculs de chaque recette.

## Stack technique

- **Kotlin** · **Jetpack Compose** · **Material 3**
- **Room** (KSP) pour la persistance, schémas exportés dans `app/schemas/`
- **Navigation Compose**, **ViewModel** et **Coroutines / Flow**
- JUnit 4 pour les tests unitaires

| | Version |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 |
| Kotlin | 2.2.10 |
| `minSdk` / `targetSdk` | 36 / 37 |

## Démarrage

### Prérequis
- Android Studio récent, compatible AGP 9.4
- JDK 17 ou plus récent pour lancer Gradle
- Un appareil ou un émulateur sous Android 16 (API 36) au minimum

### Compiler et installer

```bash
git clone https://github.com/Florianpal1/BeerRecipe.git
cd BeerRecipe
./gradlew assembleDebug        # construit l'APK de debug
./gradlew installDebug         # installe sur l'appareil connecté
```

Ou ouvrir le dossier dans Android Studio et lancer la configuration `app`.

### Tests

```bash
./gradlew test                 # tests unitaires (moteur de calcul, conseiller, catalogue)
```

## Architecture

```
app/src/main/java/fr/florianpal/beerrecipe/
├── data/
│   ├── dao/          # Accès Room
│   ├── db/           # Base de données et convertisseurs
│   ├── entity/       # Entités Room et mappers
│   ├── repository/   # BrewRepository, point d'entrée unique des données
│   └── seed/         # Catalogue initial (malts, houblons, levures, styles…)
├── domain/
│   ├── advisor/      # Conseiller de recette
│   ├── calc/         # Moteur de calcul : densité, IBU, couleur, pH…
│   └── model/        # Énumérations et unités
└── ui/
    ├── common/       # Composants partagés
    ├── navigation/   # Navigation et barre d'onglets
    ├── screens/      # Recettes, catalogue, installations, styles
    └── theme/
```

Le moteur de calcul (`domain/calc`) ne dépend ni de Room ni de l'interface : il prend un
`RecipeContext` et rend un `RecipeStats`, ce qui le rend directement testable.

## Contribuer

Les contributions sont les bienvenues. Voir [CONTRIBUTING.md](CONTRIBUTING.md) et le
[code de conduite](CODE_OF_CONDUCT.md). L'historique des versions se trouve dans
[CHANGELOG.md](CHANGELOG.md).

## Licence

Copyright (C) 2026 Florianpal1

Ce programme est un logiciel libre distribué sous les termes de la
[licence publique générale GNU, version 3](LICENSE) (GPL-3.0) : vous pouvez le redistribuer et le
modifier, à condition que toute version dérivée distribuée reste sous la même licence et que son
code source soit fourni. Il est livré **sans aucune garantie**.
