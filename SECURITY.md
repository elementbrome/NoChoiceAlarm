# Sécurité — NoChoice Alarm

- **Aucun accès Internet** : aucune permission `INTERNET`.
- **QR non prédictibles** : jetons générés avec `SecureRandom`.
- **QR commun ou individuel** : le QR commun reste local au téléphone ; un QR individuel peut être activé par alarme.
- **Validation stricte** : seul le payload exact `QRAWAKE:<token>` attendu est accepté.
- **Fichiers audio** : accès via le Storage Access Framework Android et URI persistante en lecture seule.
- **Sauvegardes Android désactivées** : `android:allowBackup="false"`.
- **Trafic clair désactivé** : `android:usesCleartextTraffic="false"`.
- **Composants internes non exportés** : activités de réveil/scanner, service et receivers ne sont pas accessibles arbitrairement par d'autres applications.
- **Notification de réveil** : service au premier plan, catégorie `ALARM`, `setOngoing(true)` et full-screen intent pour une vraie alarme.
- **Mode test distinct** : ne crée pas d'alarme planifiée et expose volontairement un bouton d'arrêt manuel.

## Risques à connaître

Le QR est une clé d'arrêt. Toute personne qui possède une copie lisible du QR commun peut valider toutes les alarmes utilisant ce QR. Une alarme sensible peut donc être configurée avec un QR individuel.

Un appareil rooté ou compromis peut modifier les données locales de l'application. Ce scénario n'est pas considéré comme protégeable par une application Android normale.

Android conserve toujours ses mécanismes système d'arrêt utilisateur. L'application ne tente pas de les contourner.
