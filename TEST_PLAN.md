# NoChoice Alarm — plan de test v1.5.0

## Build / mise à jour
- GitHub Actions construit `app-release.apk` avec la clé Release permanente.
- Vérifier `versionName 1.5.0` et `versionCode 8`.
- Installer par-dessus la précédente Release signée et vérifier que les alarmes existantes sont conservées.

## QR — galerie / impression
1. Créer une nouvelle alarme avec QR Code activé.
2. Sans cliquer sur aucun bouton QR, appuyer sur Enregistrer : le dialogue de blocage doit apparaître.
3. Fermer le dialogue, cliquer sur **Enregistrer le QR dans la galerie**, puis Enregistrer : l’alarme doit être sauvegardée.
4. Refaire avec une nouvelle alarme, mais cliquer uniquement sur **Imprimer le QR sur A4** : le dialogue d’impression Android doit s’ouvrir et l’alarme doit ensuite pouvoir être sauvegardée.
5. Dans l’aperçu d’impression, vérifier : A4 portrait, une seule page, QR en haut à gauche, environ 35 mm, marge environ 10 mm.
6. Scanner le QR imprimé / PDF à distance raisonnable pour confirmer sa lisibilité.
7. Après avoir cliqué sur galerie ou impression, changer **QR commun / QR individuel** : l’obligation doit être réactivée.

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
