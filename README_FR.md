# NoChoice Alarm 1.5.4

Application Android de réveil avec arrêt par QR code et/ou problème de mathématiques.

## Nouveautés 1.5.4

### Sélecteur d’heure en fenêtre modale
L’éditeur n’affiche plus le cadran en permanence. Touchez les heures ou les minutes pour ouvrir un sélecteur centré avec **Annuler** et **OK**. Le cadran est isolé du défilement de la page, ce qui évite que l’écran bouge pendant le réglage de l’heure.

### Flash du scanner QR
Le scanner QR propose un bouton **Flash** lorsque la caméra possède une torche compatible. Le flash peut être activé ou désactivé pendant le scan, y compris lorsque le scanner est utilisé au-dessus de l’écran verrouillé.

### Réactivation après édition
Une alarme désactivée est automatiquement réactivée lorsqu’elle est enregistrée depuis l’écran d’édition. Le bouton ON/OFF de la liste reste indépendant.

### Impression QR A4
Dans l’éditeur d’une alarme utilisant QR Code, deux actions sont disponibles :
- **Enregistrer le QR dans la galerie**
- **Imprimer le QR sur A4**

L’impression ouvre le dialogue d’impression Android avec une page A4 portrait. Le QR est placé en haut à gauche, à environ 10 mm des bords, avec une taille d’environ 35 mm afin d’être compact tout en restant facilement scannable.

L’enregistrement ou l’impression du QR est désormais facultatif : une alarme peut être enregistrée immédiatement, même si le QR n’a pas encore été exporté.

### Duplication d’alarme
Chaque alarme de la liste possède un bouton de duplication. La copie reprend les réglages de l’alarme, reçoit un nouvel identifiant et est créée désactivée. Si l’alarme utilise un QR individuel, la copie reçoit son propre QR individuel.

### Utilisation depuis l’écran verrouillé
Lorsqu’une alarme sonne, l’écran du réveil s’affiche au-dessus de l’écran verrouillé. Le QR peut être scanné et le problème de mathématiques peut être résolu sans saisir le PIN, le mot de passe ou utiliser l’empreinte. L’application ne déverrouille pas Android : après validation, l’alarme s’arrête mais le téléphone reste verrouillé.

### Langues
L’application utilise les ressources Android :
- système en **français** → interface française ;
- toute autre langue → **anglais** par défaut.

Cela s’applique aux écrans, boutons, dialogues, notifications et au scanner QR.

## Mise à jour et données
Le projet conserve :
- `applicationId = com.qralarm.app`
- signature Release permanente via les secrets GitHub
- `versionCode = 12`
- `versionName = 1.5.4`

Une mise à jour installée par-dessus une version Release signée avec la même clé conserve normalement les alarmes et préférences locales.
