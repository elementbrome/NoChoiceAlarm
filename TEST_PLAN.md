# Plan de test — NoChoice Alarm v1.2

1. Premier lancement : caméra, notifications et accès plein écran.
2. Vérifier que l'écran principal est propre, sans chevauchement avec les zones système Android.
3. Vérifier que la barre d'état supérieure est masquée dans l'application et que les écrans sonnerie/scanner sont immersifs.
4. Créer une alarme QR : vérifier qu'« Enregistrer » est refusé avant l'enregistrement du QR dans la galerie.
5. Enregistrer le QR commun, créer une deuxième alarme et vérifier que le QR affiché est identique.
6. Créer une alarme avec « QR individuel » et vérifier que son QR diffère du QR commun.
7. Vérifier la carte dans la liste : heure, label, jours, pastilles Mode/Snooze/Son, switch et corbeille.
8. Vérifier que les alarmes actives sont en haut de la liste et possèdent la mise en évidence ACTIVE.
9. Désactiver une alarme, l'éditer puis l'enregistrer : elle doit rester désactivée.
10. Vérifier la notification silencieuse « alarme(s) active(s) » ; désactiver toutes les alarmes doit la supprimer.
11. Cliquer sur une carte, modifier ses réglages et vérifier leur persistance.
12. Supprimer une alarme via la corbeille et confirmer l'annulation de sa planification.
13. Vérifier le placeholder `e.g. Running`.
14. Tester son système et fichier audio personnalisé.
15. Déplacer le curseur de volume : le son doit être pré-écouté et suivre le niveau choisi, puis s'arrêter après relâchement.
16. Tester volume 0, 25, 50, 100 %.
17. Tester les rampes 30/60/90/120 s.
18. Vérifier vibration OFF par défaut ; activer puis vérifier la vibration lors de l'alarme/test.
19. Vérifier les icônes des boutons QR Code et Math.
20. Vérifier que seule la description de la difficulté Math sélectionnée est affichée.
21. Easy : uniquement `NN + NN` ou `NN − NN` avec deux nombres à 2 chiffres.
22. Hard : uniquement `NN × NN` avec deux nombres à 2 chiffres.
23. Brutal : cinq opérandes, priorité ×/÷, résultat entier de 1 à 200.
24. Tester le bouton « Tester l'alarme » et vérifier que « Arrêter le test » coupe immédiatement son, vibration et notification.
25. Vérifier l'ordre visuel : Tester (contour) puis Enregistrer (fond coloré).
26. Tester QR uniquement : mauvais QR = refus ; bon QR = arrêt.
27. Tester Math uniquement : mauvaise réponse = sonnerie continue ; bonne réponse = arrêt.
28. Tester QR + Math : le QR doit obligatoirement précéder le problème de math.
29. Tester snooze 2/5/10/15 min.
30. Verrouiller l'écran avant un déclenchement : vérifier sonnerie, vibration selon réglage, full-screen et notification ongoing.
31. Redémarrer avant le déclenchement : vérifier replanification et restauration de la notification des alarmes actives.
32. Tester changement manuel d'heure et de fuseau.
33. Révoquer la caméra : le scanner doit demander à nouveau l'autorisation.
34. Vérifier Android « Forcer l'arrêt » / Applications actives : le système reste capable d'arrêter l'application, limitation Android attendue.
