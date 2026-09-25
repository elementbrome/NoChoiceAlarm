# Sécurité — QR Alarm

- **Pas d'accès Internet** : aucune permission `INTERNET`.
- **QR non prédictible** : jeton 144 bits généré avec `SecureRandom`.
- **Validation stricte** : seul le payload exact `QRAWAKE:<token>` de l'alarme active est accepté.
- **Pas de stockage global du son** : accès au fichier via URI SAF persistante en lecture seule.
- **Sauvegardes désactivées** : `android:allowBackup="false"`.
- **Surface d'attaque réduite** : activités, service et receiver d'alarme non exportés. Le receiver système de redémarrage est également non exporté.
- **Notification** : service au premier plan + `setOngoing(true)` + catégorie `ALARM` + full-screen intent.
- **Limite système** : Android conserve toujours des mécanismes d'arrêt utilisateur (Task Manager/Forcer l'arrêt). Une application normale ne peut pas les neutraliser.

## Risque à connaître

Le PNG du QR dans la galerie est une clé permettant d'arrêter l'alarme correspondante. Ne pas partager ce QR si ce comportement n'est pas souhaité.
