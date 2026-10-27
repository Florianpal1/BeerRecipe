# 🍺 Beer Recipe

[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

An Android app for designing all-grain homebrew beer recipes. It calculates gravities, bitterness,
colour, mash pH and water volumes, compares the recipe against the target style and suggests
quantified corrections.

> Fully offline: all data stays on the device (local Room database).
>
> The user interface is currently in French.

## Features

### Recipes
- Create, duplicate, favourite and delete recipes
- Scale a recipe to a new batch volume
- Tabbed editor: **Summary**, **Grain bill**, **Hops**, **Yeasts**, **Additives**, **Process**, **Log**
- Mash steps and fermentation steps
- Brew log: track brew sessions and actual measurements

### Calculations
| Metric | Method |
|---|---|
| Original and final gravity (OG / FG) | Extract points × equipment efficiency; only the fermentable share is attenuated |
| Alcohol (ABV) | From OG and FG |
| Bitterness (IBU) | **Tinseth**, **Rager** or **Garetz**, with hop form factors (pellets, cryo, whole cones…), first wort, temperature-dependent whirlpool, dry hopping |
| Colour | **Morey** formula, shown in EBC with a rendered colour swatch |
| Mash pH | Per-malt model: distilled-water pH and buffering capacity, estimated from malt type and colour when the ingredient sheet doesn't specify them |
| Water volumes | From milling to bottling, including equipment losses |
| Yeast | Pitch rate: cells required vs. cells pitched |
| Bottle conditioning | Priming sugar (sucrose or dextrose) for the target CO₂ volumes |
| Cost | Total cost and cost per litre |

### Recipe advisor
The advisor compares the calculated stats with the style ranges and turns every deviation into a
concrete action: how many grams of which malt, how many millilitres of acid, which volume or which
temperature. Suggested quantities are obtained by inverting the calculation engine's formulas.
It checks gravity, bitterness, colour, pH, fermentation, grain bill, equipment and water.

### Catalogue
Editable ingredient sheets, pre-filled on first launch:
- Malts, hops, yeasts and additives
- **BJCP 2021** style guidelines, colours converted to EBC
- Water profiles

### Equipment
Describe your brewing setup (volumes, losses, efficiency…), used by each recipe's calculations.

## Tech stack

- **Kotlin** · **Jetpack Compose** · **Material 3**
- **Room** (KSP) for persistence, schemas exported to `app/schemas/`
- **Navigation Compose**, **ViewModel** and **Coroutines / Flow**
- JUnit 4 for unit tests

| | Version |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 |
| Kotlin | 2.2.10 |
| `minSdk` / `targetSdk` | 36 / 37 |

## Getting started

### Requirements
- A recent Android Studio release supporting AGP 9.4
- JDK 17 or newer to run Gradle
- A device or emulator running Android 16 (API 36) or later

### Build and install

```bash
git clone https://github.com/Florianpal1/BeerRecipe.git
cd BeerRecipe
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # install on the connected device
```

Or open the folder in Android Studio and run the `app` configuration.

### Tests

```bash
./gradlew test                 # unit tests (calculation engine, advisor, catalogue)
```

## Architecture

```
app/src/main/java/fr/florianpal/beerrecipe/
├── data/
│   ├── dao/          # Room DAOs
│   ├── db/           # Database and type converters
│   ├── entity/       # Room entities and mappers
│   ├── repository/   # BrewRepository, single entry point to the data
│   └── seed/         # Initial catalogue (malts, hops, yeasts, styles…)
├── domain/
│   ├── advisor/      # Recipe advisor
│   ├── calc/         # Calculation engine: gravity, IBU, colour, pH…
│   └── model/        # Enums and units
└── ui/
    ├── common/       # Shared components
    ├── navigation/   # Navigation and bottom bar
    ├── screens/      # Recipes, catalogue, equipment, styles
    └── theme/
```

The calculation engine (`domain/calc`) depends on neither Room nor the UI: it takes a
`RecipeContext` and returns a `RecipeStats`, which makes it directly testable.

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) and the
[code of conduct](CODE_OF_CONDUCT.md). Release history is in [CHANGELOG.md](CHANGELOG.md).

## License

Copyright (C) 2026 Florianpal1

This program is free software, distributed under the terms of the
[GNU General Public License, version 3](LICENSE) (GPL-3.0): you may redistribute and modify it,
provided that any distributed derivative work stays under the same license and ships with its
source code. It comes **without any warranty**.
