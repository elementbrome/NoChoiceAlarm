# Passage à NoChoice Alarm 1.5.1

## Modifications
- Une nouvelle alarme utilisant un QR peut maintenant être enregistrée immédiatement, sans obligation d'enregistrer le QR dans la galerie ni d'ouvrir l'impression A4.
- Les fonctions d'enregistrement et d'impression du QR restent disponibles et facultatives.
- Un bouton de duplication est ajouté à chaque alarme dans la liste.
- Une alarme dupliquée reprend les mêmes réglages mais est créée désactivée, avec un nouvel identifiant.
- Si l'alarme d'origine utilise un QR individuel, la copie reçoit un nouveau QR individuel.

## Installation / mise à jour
La version est maintenant `1.5.1` avec `versionCode 9`.

Si vous utilisez le workflow GitHub Release déjà configuré :
1. Remplacez le contenu du dépôt par celui de ce dossier, sans supprimer `.git`.
2. Commit + Push.
3. Ouvrez GitHub > Actions > **Build signed release APK**.
4. Téléchargez l'artifact Release et installez `app-release.apk` par-dessus la version 1.5.0.

Pour conserver les alarmes existantes, gardez le même `applicationId` et la même clé de signature Release, et ne désinstallez pas l'application avant la mise à jour.
