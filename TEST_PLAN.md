# NoChoice Alarm — plan de test v1.5.3

## Build / mise à jour
- GitHub Actions construit `app-release.apk` avec la clé Release permanente.
- Vérifier `versionName 1.5.3` et `versionCode 11`.
- Installer par-dessus la précédente Release signée et vérifier que les alarmes existantes sont conservées.

## QR — galerie / impression
1. Créer une nouvelle alarme avec QR Code activé.
2. Sans enregistrer ni imprimer le QR, appuyer sur **Enregistrer** : l’alarme doit être sauvegardée sans dialogue bloquant.
3. Vérifier que **Enregistrer le QR dans la galerie** fonctionne toujours.
4. Vérifier que **Imprimer le QR sur A4** ouvre toujours le dialogue d’impression Android.
5. Vérifier l’aperçu : A4 portrait, une seule page, QR en haut à gauche, environ 35 mm, marge environ 10 mm.
6. Scanner le QR imprimé / PDF à distance raisonnable pour confirmer sa lisibilité.
7. Changer **QR commun / QR individuel** puis enregistrer directement : aucun blocage ne doit apparaître.

## Duplication d’alarme
1. Dupliquer une alarme active depuis la liste.
2. Vérifier qu’une nouvelle alarme apparaît avec les mêmes réglages mais désactivée.
3. Vérifier que l’originale reste inchangée.
4. Pour une alarme avec QR individuel, vérifier que la copie possède un QR différent.
5. Modifier puis activer la copie et vérifier qu’elle peut être planifiée normalement.

## Langues
### Français
- Régler Android en français, relancer l’app.
- Vérifier la liste, l’éditeur, les dialogues, le scanner, l’écran de sonnerie et les notifications.

### Anglais
- Régler Android en anglais, relancer l’app.
- Vérifier les mêmes écrans en anglais.

### Langue non supportée
- Régler Android en allemand, espagnol ou italien.
- Vérifier que les textes appartenant à NoChoice Alarm s’affichent en anglais.
- Les écrans appartenant au système Android (permissions, impression) peuvent rester dans la langue du système.

## Réveil
- QR seul.
- Math seul.
- QR + Math.
- Snooze / rappel.
- Vibration ON/OFF.
- Test manuel de l’alarme.
- Volume progressif.

## Écran verrouillé
1. Verrouiller le téléphone avec PIN/mot de passe/biométrie actif.
2. Déclencher une alarme QR uniquement : l’écran de réveil doit apparaître sans déverrouillage.
3. Appuyer sur **Scanner le QR code** : la caméra doit s’ouvrir au-dessus de l’écran verrouillé, sans demander le PIN.
4. Scanner le QR valide : l’alarme doit s’arrêter et le téléphone doit rester verrouillé.
5. Refaire avec une alarme Math uniquement : saisir la bonne réponse sans déverrouiller ; l’alarme doit s’arrêter et le téléphone doit rester verrouillé.
6. Refaire avec QR + Math : scanner le QR, résoudre le calcul, puis vérifier que l’alarme s’arrête en restant verrouillé.
7. Vérifier également sur écran éteint : l’alarme doit allumer l’écran et afficher l’interface de réveil.


## Réactivation après modification
1. Désactiver une alarme depuis la liste.
2. Ouvrir cette alarme dans l’éditeur.
3. Modifier un réglage, puis appuyer sur **Enregistrer**.
4. Vérifier que l’alarme apparaît à nouveau comme active dans la liste.
5. Désactiver ensuite l’alarme directement depuis la liste et vérifier qu’elle reste désactivée tant qu’elle n’est pas enregistrée à nouveau depuis l’éditeur.
