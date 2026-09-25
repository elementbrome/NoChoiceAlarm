# QR Alarm

Application Android de réveil hors ligne où l'alarme ne peut être arrêtée définitivement qu'en scannant le QR code associé.

## Fonctionnalités

- Liste des alarmes, activation/désactivation, modification et suppression.
- Heure, nom facultatif, répétition par jour de la semaine.
- Son système par défaut ou fichier audio choisi avec le sélecteur Android.
- Mode de réveil QR code avec jeton aléatoire propre à chaque alarme.
- QR exportable en PNG dans la galerie pour impression.
- Snooze : Off, 2, 5, 10 ou 15 minutes.
- Alarmes exactes via `AlarmManager.setAlarmClock()`.
- Relance des alarmes après redémarrage du téléphone, avec récupération d’une alarme qui sonnait encore avant un redémarrage brutal.
- Écran de réveil plein écran et service au premier plan pendant la sonnerie.
- Notification persistante/ongoing pendant la sonnerie.
- Caméra intégrée pour scanner le QR, sans dépendre d'une autre application.
- Aucune permission Internet, aucune télémétrie, aucune donnée envoyée.

## Sécurité

Le contenu du QR est `QRAWAKE:<jeton aléatoire>` avec un jeton généré via `SecureRandom`. L'application compare exactement le jeton lu au jeton local de l'alarme active. Les sauvegardes Android sont désactivées (`allowBackup=false`). Les composants internes ne sont pas exportés, sauf le launcher et le receiver système de redémarrage.

Le QR enregistré dans la galerie doit être considéré comme une « clé d'arrêt » : toute personne qui possède une copie imprimée ou numérique du QR correspondant peut arrêter cette alarme en le présentant à la caméra.

## Limite Android importante

Une application Android tierce ne peut pas être rendue totalement « impossible à tuer ». Android 13+ laisse toujours l'utilisateur arrêter une application utilisant un service au premier plan depuis le gestionnaire des applications actives, et « Forcer l'arrêt » reste une commande système. Le projet utilise néanmoins le maximum prévu pour un vrai réveil : alarme exacte, service au premier plan, notification ongoing, full-screen intent et redémarrage/replanification.

## Build

Ouvrir ce dossier avec Android Studio récent, laisser Gradle télécharger les dépendances, puis :

1. `Build > Build APK(s)` pour un APK debug ; ou
2. `Build > Generate Signed App Bundle / APK` pour une APK signée de distribution.

Configuration : minSdk 26, targetSdk 36, Java, dépendance ZXing Core 3.5.4.

## Publication Google Play

`USE_EXACT_ALARM` et `USE_FULL_SCREEN_INTENT` sont appropriés ici car le réveil est la fonctionnalité principale de l'application, mais leur utilisation doit être déclarée correctement dans la Play Console lors d'une publication.
