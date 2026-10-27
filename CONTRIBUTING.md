# Contribuer à Beer Recipe

Merci de l'intérêt que vous portez au projet ! Ce guide explique comment proposer une
modification.

## Signaler un bug ou proposer une idée

Ouvrez une [issue](https://github.com/Florianpal1/BeerRecipe/issues) en utilisant le modèle
adapté. Pour un bug, précisez la version d'Android, l'appareil, les étapes pour le reproduire et,
pour une erreur de calcul, les valeurs attendues ainsi que leur source (logiciel de brassage,
formule publiée…).

## Proposer une modification

1. Forkez le dépôt et créez une branche depuis `master` :
   `git checkout -b feat/ma-fonctionnalite`
2. Faites vos modifications.
3. Vérifiez que le projet compile et que les tests passent :
   ```bash
   ./gradlew assembleDebug test
   ```
4. Ouvrez une pull request en décrivant le changement et la façon dont vous l'avez testé.

## Conventions de code

- Kotlin, en suivant le style configuré dans `.idea/codeStyles` (formatage Android Studio).
- Commentaires et textes de l'interface en **français**.
- Le moteur de calcul (`domain/calc`) et le conseiller (`domain/advisor`) restent indépendants de
  Room et d'Android : toute nouvelle formule doit être accompagnée de tests unitaires dans
  `app/src/test`, avec la source de la formule en commentaire.
- Toute modification d'une entité Room passe par une montée de version de `AppDatabase`, une
  migration, et le schéma généré dans `app/schemas/` doit être commité.

## Messages de commit

Messages en français, à l'impératif ou au nom, avec une première ligne courte (≈ 50 caractères)
décrivant le changement, suivie si besoin d'un paragraphe expliquant le pourquoi.

```
Ajoute la formule IBU de Daniels

Certains brasseurs comparent leurs résultats avec des logiciels
qui l'utilisent par défaut.
```

## Licence

Le projet est distribué sous [GPL-3.0](LICENSE). En proposant une contribution, vous acceptez
qu'elle soit publiée sous cette même licence.

## Code de conduite

En participant, vous vous engagez à respecter le [code de conduite](CODE_OF_CONDUCT.md).
