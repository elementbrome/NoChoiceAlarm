# Plan de test — NoChoice Alarm v1.1

1. Premier lancement : caméra, notifications et accès plein écran.
2. Vérifier le thème sombre, la liste vide et le bouton +.
3. Créer une alarme QR : vérifier qu'« Enregistrer » est refusé avant l'enregistrement du QR dans la galerie.
4. Enregistrer le QR commun, créer l'alarme, puis créer une deuxième alarme et vérifier que le QR affiché est identique.
5. Créer une alarme avec « QR individuel » et vérifier que son QR diffère du QR commun.
6. Vérifier la carte dans la liste : heure, label, jours, pastilles Mode/Snooze/Son, switch et corbeille.
7. Cliquer sur une carte, modifier ses réglages et vérifier leur persistance.
8. Supprimer une alarme via la corbeille et confirmer l'annulation de sa planification.
9. Tester son système et fichier audio personnalisé.
10. Tester volume 0, 25, 50, 100 %.
11. Tester les rampes 30/60/90/120 s et vérifier que le volume atteint progressivement la cible.
12. Tester le bouton « Tester l'alarme » et vérifier que « Arrêter le test » coupe immédiatement son, vibration et notification.
13. Tester QR uniquement : mauvais QR = refus ; bon QR = arrêt.
14. Tester Math uniquement : mauvaise réponse = sonnerie continue ; bonne réponse = arrêt.
15. Tester Easy et Hard sur plusieurs réveils et vérifier que les calculs changent.
16. Tester Brutal sur plusieurs réveils : cinq opérandes, priorité ×/÷, résultat entier de 1 à 200.
17. Tester QR + Math : le QR doit obligatoirement précéder le problème de math.
18. Tester snooze 2/5/10/15 min.
19. Verrouiller l'écran avant un déclenchement : vérifier sonnerie, vibration, full-screen et notification ongoing.
20. Redémarrer avant le déclenchement : vérifier la replanification.
21. Tester changement manuel d'heure et de fuseau.
22. Révoquer la caméra : le scanner doit demander à nouveau l'autorisation.
23. Vérifier Android « Forcer l'arrêt » / Applications actives : le système reste capable d'arrêter l'application, ce qui est une limitation Android attendue.
