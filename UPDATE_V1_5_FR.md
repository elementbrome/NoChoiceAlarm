# Passage à NoChoice Alarm 1.5.0

1. Décompresser `NoChoiceAlarm-v1.5.0-source.zip`.
2. Dans GitHub Desktop, ouvrir NoChoiceAlarm puis `Repository > Show in Explorer`.
3. Copier tout le contenu du dossier `NoChoiceAlarm` fourni dans le dépôt local.
4. Accepter le remplacement des fichiers existants.
5. Ne pas supprimer le dossier caché `.git` du dépôt local.
6. Vérifier que les secrets GitHub de signature Release existent toujours :
   - `NOCHOICE_KEYSTORE_BASE64`
   - `NOCHOICE_KEYSTORE_PASSWORD`
   - `NOCHOICE_KEY_ALIAS`
   - `NOCHOICE_KEY_PASSWORD`
7. Dans GitHub Desktop :
   - Summary : `NoChoice Alarm v1.5.0`
   - `Commit to main`
   - `Push origin`
8. Sur GitHub : Actions > Build signed release APK.
9. Télécharger `NoChoiceAlarm-release-apk` puis `app-release.apk`.
10. Installer la nouvelle APK par-dessus la précédente Release signée. Ne pas désinstaller l’application si vous voulez conserver les données locales.

## Impression QR
Le bouton `Imprimer le QR sur A4 / Print QR on A4` ouvre le dialogue d’impression Android. Le document est préparé en A4 portrait avec un QR d’environ 35 mm à environ 10 mm du coin supérieur gauche.

## Langues
- Android en français : français.
- Android en anglais : anglais.
- Toute autre langue système : anglais par défaut.
- Les écrans du système Android lui-même (dialogue d’impression, permissions, etc.) suivent la langue du système.
