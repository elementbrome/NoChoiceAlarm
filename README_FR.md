# NoChoice Alarm 1.5.0

Application Android de réveil avec arrêt par QR code et/ou problème de mathématiques.

## Nouveautés 1.5.0

### Impression QR A4
Dans l’éditeur d’une alarme utilisant QR Code, deux actions sont disponibles :
- **Enregistrer le QR dans la galerie**
- **Imprimer le QR sur A4**

L’impression ouvre le dialogue d’impression Android avec une page A4 portrait. Le QR est placé en haut à gauche, à environ 10 mm des bords, avec une taille d’environ 35 mm afin d’être compact tout en restant facilement scannable.

Pour une nouvelle alarme QR, il suffit d’avoir utilisé l’une de ces deux actions avant de pouvoir enregistrer l’alarme. Si le QR est ensuite modifié, cette validation est réinitialisée.

### Langues
L’application utilise les ressources Android :
- système en **français** → interface française ;
- toute autre langue → **anglais** par défaut.

Cela s’applique aux écrans, boutons, dialogues, notifications et au scanner QR.

## Mise à jour et données
Le projet conserve :
- `applicationId = com.qralarm.app`
- signature Release permanente via les secrets GitHub
- `versionCode = 8`
- `versionName = 1.5.0`

Une mise à jour installée par-dessus une version Release signée avec la même clé conserve normalement les alarmes et préférences locales.
