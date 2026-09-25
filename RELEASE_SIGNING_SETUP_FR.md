# NoChoiceAlarm — Signature Release permanente

## 1. Créer la clé sur ton PC Windows

Ouvre PowerShell dans un dossier sûr (pas dans le dépôt GitHub) et exécute :

```powershell
keytool -genkeypair -v -keystore nochoicealarm-release.jks -storetype JKS -keyalg RSA -keysize 4096 -validity 10000 -alias nochoicealarm -dname "CN=NoChoiceAlarm, OU=Mobile, O=ElementBrome, C=CH"
```

Choisis un mot de passe fort. Quand keytool demande le mot de passe de la clé, tu peux appuyer sur Entrée pour utiliser le même mot de passe.

Garde précieusement :
- `nochoicealarm-release.jks`
- le mot de passe du keystore
- l'alias `nochoicealarm`
- le mot de passe de la clé

Ne mets JAMAIS le fichier `.jks` directement dans GitHub.

## 2. Transformer le keystore en Base64

Dans le même dossier PowerShell :

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\nochoicealarm-release.jks")) | Set-Content -NoNewline keystore-base64.txt
```

Ouvre `keystore-base64.txt` et copie tout son contenu.

## 3. Ajouter les 4 secrets GitHub

Dans le dépôt GitHub :

Settings > Secrets and variables > Actions > New repository secret

Crée exactement :

1. `NOCHOICE_KEYSTORE_BASE64`
   - valeur : contenu complet de `keystore-base64.txt`

2. `NOCHOICE_KEYSTORE_PASSWORD`
   - valeur : mot de passe du keystore

3. `NOCHOICE_KEY_ALIAS`
   - valeur : `nochoicealarm`

4. `NOCHOICE_KEY_PASSWORD`
   - valeur : mot de passe de la clé
   - si tu as utilisé le même mot de passe, mets le même que le keystore

## 4. Copier les fichiers du pack dans le dépôt

Ce pack remplace :
- `app/build.gradle.kts`
- `.github/workflows/build-apk.yml`
- `.gitignore`

Il passe également la version à :
- versionCode 7
- versionName 1.4.1

## 5. Commit

GitHub Desktop :
- Summary : `Add permanent release signing`
- Commit to main
- Push origin

## 6. Télécharger l'APK

GitHub > Actions > Build signed release APK

Après le build vert :
Artifacts > `NoChoiceAlarm-release-apk`

L'APK à installer est `app-release.apk`.

## IMPORTANT — première transition debug -> release

L'APK actuellement installée sur ton téléphone est probablement une APK debug. Une APK release signée avec ta nouvelle clé n'aura donc pas la même signature.

Android peut refuser de l'installer par-dessus la version actuelle. Si c'est le cas, il faudra désinstaller une fois l'ancienne application avant d'installer la première release signée. Cette désinstallation supprimera les données locales.

À partir du moment où la première version release signée est installée, toutes les futures versions signées avec CE MÊME keystore pourront être installées par-dessus et conserveront normalement les données.

## Règles pour toutes les futures mises à jour

Toujours conserver :
- le même `applicationId`
- le même fichier keystore / même clé
- un `versionCode` strictement plus grand à chaque version

Exemple :
- 1.4.1 -> versionCode 7
- 1.5.0 -> versionCode 8
- 1.5.1 -> versionCode 9

## Sauvegarde

Fais au minimum deux sauvegardes du fichier `.jks` dans des endroits sûrs et distincts. Si tu distribues l'application hors Play Store et perds la clé privée, tu ne pourras plus publier une mise à jour compatible avec les installations existantes.
