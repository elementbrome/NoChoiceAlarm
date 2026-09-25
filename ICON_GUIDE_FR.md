# Changer facilement l’icône de NoChoice Alarm

Le projet utilise actuellement :

`app/src/main/res/drawable/ic_launcher.xml`

Le manifeste référence cette ressource avec :

`android:icon="@drawable/ic_launcher"`

## Option 1 — la plus simple sans Android Studio

Si tu as une nouvelle icône au format **Vector Drawable Android (.xml)** :

1. Renomme-la `ic_launcher.xml`.
2. Remplace `app/src/main/res/drawable/ic_launcher.xml` par ce nouveau fichier.
3. Commit + Push sur GitHub.
4. Relance le workflow Build APK.

Aucun autre fichier n’est à modifier.

## Option 2 — avec une image PNG

1. Prépare une image carrée, idéalement **1024 × 1024 px**.
2. Supprime `app/src/main/res/drawable/ic_launcher.xml`.
3. Place l’image ici : `app/src/main/res/drawable/ic_launcher.png`.
4. Commit + Push puis reconstruis l’APK.

Cette méthode fonctionne pour tester, mais elle est moins optimale pour une publication officielle.

## Option 3 — recommandée pour Google Play

Avec Android Studio :

1. Clic droit sur `app` → **New → Image Asset**.
2. Choisis **Launcher Icons (Adaptive and Legacy)**.
3. Sélectionne ton image dans **Foreground Layer** et choisis le fond.
4. Garde le nom `ic_launcher` puis **Next → Finish**.
5. Android Studio créera les fichiers `mipmap-*`.
6. Dans `AndroidManifest.xml`, remplace :
   `android:icon="@drawable/ic_launcher"`
   par :
   `android:icon="@mipmap/ic_launcher"`
7. Si Android Studio crée aussi `ic_launcher_round`, tu peux ajouter :
   `android:roundIcon="@mipmap/ic_launcher_round"`

Pour le Play Store, cette méthode adaptative est la meilleure.
