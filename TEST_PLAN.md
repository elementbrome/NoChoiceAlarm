# Plan de test

1. Premier lancement : caméra + notifications + accès plein écran demandés.
2. Créer une alarme à +2 minutes, répétition désactivée, son par défaut, snooze Off.
3. Verrouiller l'écran : vérifier sonnerie, vibration, écran de réveil et notification foreground.
4. Scanner un mauvais QR : l'alarme doit continuer.
5. Scanner le bon QR imprimé : son/vibration/service/notification doivent s'arrêter.
6. Tester snooze 2, 5, 10, 15 min et vérifier le nouveau déclenchement.
7. Tester un fichier audio choisi, puis redémarrer le téléphone et vérifier que l'URI reste lisible.
8. Tester une alarme répétée sur plusieurs jours et un passage de fuseau/heure d'été.
9. Redémarrer le téléphone avant le déclenchement : vérifier la replanification.
10. Balayer l'activité de réveil dans les récents : le service de sonnerie doit continuer.
11. Vérifier le cas Android 13+ « Applications actives > Arrêter » : l'OS doit pouvoir stopper l'app (limitation Android attendue).
12. Révoquer la caméra : le scanner doit redemander l'autorisation et ne jamais accepter un fallback non sécurisé.
