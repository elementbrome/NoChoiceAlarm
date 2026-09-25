PACK ICÔNE ANDROID – NoChoiceAlarm

CONTENU
- app/src/main/res/drawable/ic_launcher_background.xml
- app/src/main/res/drawable/ic_launcher_foreground.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

COMMENT L’INTÉGRER DANS TON PROJET VIA GITHUB DESKTOP
1. Dézippe ce pack.
2. Ouvre ton dépôt local NoChoiceAlarm avec GitHub Desktop.
3. Clique sur "Repository > Show in Explorer".
4. Copie le dossier "app" de ce pack dans ton projet local.
5. Accepte de remplacer les fichiers existants.
6. Vérifie que ces fichiers existent maintenant dans ton projet :
   - app/src/main/res/drawable/ic_launcher_background.xml
   - app/src/main/res/drawable/ic_launcher_foreground.xml
   - app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
   - app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
7. Dans AndroidManifest.xml, vérifie que l’application pointe bien vers :
   android:icon="@mipmap/ic_launcher"
   android:roundIcon="@mipmap/ic_launcher_round"
8. Dans GitHub Desktop :
   - Summary : Change app icon
   - Commit to main
   - Push origin
9. GitHub Actions va reconstruire l’APK.
10. Installe la nouvelle APK sur ton téléphone.

EST-CE QUE LES MISES À JOUR CONSERVENT LES DONNÉES ?
Oui, en général OUI, si :
- le package/applicationId reste le même ;
- la nouvelle APK est signée avec la même clé que l’ancienne.

Dans ce cas, Android considère cela comme une mise à jour et conserve normalement :
- alarmes
- préférences
- QR global
- paramètres
- données locales de l’application

ATTENTION
- Si tu désinstalles l’application, les données locales sont supprimées.
- Si la clé de signature change, Android peut refuser la mise à jour par-dessus l’ancienne version.
- Si tu installes une nouvelle version après désinstallation, les données ne seront pas conservées.

RECOMMANDATION IMPORTANTE
Pour conserver les données de façon fiable au fil des mises à jour, utilise toujours :
- le même applicationId
- la même clé de signature (idéalement un keystore release stable)

NOTE
Ce pack utilise une adaptive icon Android moderne :
- background = dégradé plein écran
- foreground = icône vectorielle blanche
Le launcher Android appliquera automatiquement le masque rond selon le téléphone.
