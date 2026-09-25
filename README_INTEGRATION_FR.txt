PACK ICÔNE PNG – NoChoiceAlarm

Ce pack utilise ton PNG comme icône de l’application.

FICHIERS À AJOUTER / REMPLACER
- app/src/main/res/mipmap-mdpi/ic_launcher.png
- app/src/main/res/mipmap-mdpi/ic_launcher_round.png
- app/src/main/res/mipmap-hdpi/ic_launcher.png
- app/src/main/res/mipmap-hdpi/ic_launcher_round.png
- app/src/main/res/mipmap-xhdpi/ic_launcher.png
- app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
- app/src/main/res/mipmap-xxhdpi/ic_launcher.png
- app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
- app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
- app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png
- app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png
- app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png
- app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png
- app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png
- app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

ANDROIDMANIFEST.XML
Dans app/src/main/AndroidManifest.xml, vérifie que l’élément <application> contient :
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"

ÉTAPES PAS-À-PAS
1. Dézippe ce pack.
2. Ouvre GitHub Desktop.
3. Ouvre le dépôt local NoChoiceAlarm.
4. Clique sur Repository > Show in Explorer.
5. Copie le dossier app du pack dans ton projet local NoChoiceAlarm.
6. Accepte Remplacer les fichiers dans la destination.
7. Ouvre app/src/main/AndroidManifest.xml et vérifie les 2 lignes ci-dessus.
8. Retourne dans GitHub Desktop.
9. Commit message conseillé : Change launcher icon from PNG.
10. Clique sur Commit to main puis Push origin.
11. GitHub Actions reconstruira l’APK.

CONSERVATION DES DONNÉES
Oui, les données sont conservées pendant une mise à jour si :
- le même applicationId est gardé ;
- la nouvelle APK est signée avec la même clé ;
- tu installes la nouvelle APK par-dessus l’ancienne sans désinstaller.

Changer uniquement l’icône ne supprime pas les données.
