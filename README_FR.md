# NoChoice Alarm — v1.2

Application Android de réveil hors ligne conçue pour rendre l'arrêt d'une alarme volontairement exigeant.

## Fonctionnalités

- Interface sombre et moderne.
- Alarmes actives affichées en premier et visuellement mises en évidence.
- Notification silencieuse persistante résumant les alarmes actuellement activées.
- Liste des alarmes avec heure, label, répétition et pastilles d'informations.
- Modification par simple clic sur une alarme.
- Suppression avec bouton corbeille et confirmation.
- Activation/désactivation individuelle.
- Heure, label facultatif (`e.g. Running`) et répétition par jour de la semaine.
- Son système par défaut ou fichier audio choisi via le sélecteur Android.
- Nom du son mémorisé et affiché dans la liste.
- Volume de lecture de 0 à 100 %, avec pré-écoute pendant le déplacement du curseur.
- Montée progressive du volume : Off, 30, 60, 90 ou 120 secondes.
- Vibration activable, désactivée par défaut.
- Snooze : Off, 2, 5, 10 ou 15 minutes.

### Modes de réveil

Les modes sont combinables :

- **QR Code**
- **Math**
- **QR Code + Math** : le QR est toujours demandé en premier, puis le calcul.

Les boutons QR Code et Math disposent d'une icône dédiée.

### QR Code

- Un **QR commun** est utilisé par défaut pour toutes les alarmes.
- Une alarme peut être configurée avec son **QR individuel**.
- Le QR est exportable en PNG dans la galerie pour impression.
- Lors de la création d'une nouvelle alarme contenant le mode QR, l'enregistrement est bloqué tant que l'utilisateur n'a pas appuyé sur « Enregistrer le QR dans la galerie ».
- Les jetons QR sont générés avec `SecureRandom`.

### Math

Un nouveau problème est généré aléatoirement à chaque réveil. L'éditeur n'affiche que la description de la difficulté actuellement sélectionnée.

- **Easy** : addition ou soustraction de deux nombres à 2 chiffres.
- **Hard** : multiplication de deux nombres à 2 chiffres.
- **Brutal** : expression à 5 opérandes avec `+`, `−`, `×`, `÷`, priorité standard des opérations, divisions exactes et résultat final entre 1 et 200.

### Test et actions de l'éditeur

En bas de l'écran :

1. **Tester l'alarme** — bouton secondaire à fond transparent.
2. **Enregistrer** — bouton principal coloré.

Le test utilise les réglages actuellement affichés sans enregistrer ni replanifier l'alarme. Un bouton **Arrêter le test** est toujours disponible dans le mode test.

## Barres système Android

Les écrans principaux masquent la barre d'état supérieure pour une interface plus propre et gèrent correctement les zones système/encoches. Les écrans de sonnerie et de scan QR utilisent un mode immersif.

Android conserve néanmoins le contrôle de ses propres panneaux système : l'application ne peut pas empêcher l'utilisateur d'afficher manuellement le panneau de notifications/paramètres rapides.

## Fonctionnement Android

- Alarmes exactes via `AlarmManager.setAlarmClock()`.
- Replanification après redémarrage, changement d'heure et changement de fuseau.
- Service au premier plan pendant la sonnerie.
- Notification `ongoing` non balayable pendant la sonnerie.
- Notification silencieuse séparée pour les alarmes programmées actives.
- Full-screen intent pour le réveil réel.
- Caméra intégrée avec ZXing pour lire les QR.
- Aucune permission Internet, aucune télémétrie.

## Limite Android importante

Une application Android tierce ne peut pas être rendue totalement impossible à arrêter. Android conserve toujours des commandes système telles que « Forcer l'arrêt » et, selon la version, l'arrêt d'une application depuis le gestionnaire des applications actives.

## Volume

Le réglage 0–100 % contrôle le volume du `MediaPlayer` de NoChoice Alarm relativement au flux d'alarme Android. Il ne modifie pas le volume global du téléphone. La pré-écoute utilise le son actuellement sélectionné.

## Build GitHub Actions

Le workflow `.github/workflows/build-apk.yml` utilise :

- `ubuntu-24.04`
- `actions/checkout@v7`
- `actions/setup-java@v6`
- `android-actions/setup-android@v4`
- `gradle/actions/setup-gradle@v6` avec **Enhanced Caching**
- `actions/upload-artifact@v7`

Il compile un APK debug dans `app/build/outputs/apk/debug/app-debug.apk` et l'ajoute aux Artifacts sous le nom `NoChoiceAlarm-debug-apk`.

Configuration Android : minSdk 26, targetSdk/compileSdk 36, Java 17, ZXing Core 3.5.4.
