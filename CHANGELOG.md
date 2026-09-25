# Changelog

## 1.5.4 (versionCode 12)

- Ajout d’un bouton **Flash** dans le scanner QR. Il active/désactive la torche de la caméra lorsque l’appareil la prend en charge.
- Le cadran de sélection de l’heure n’est plus affiché en permanence dans l’éditeur.
- Toucher les heures ou les minutes ouvre désormais un sélecteur d’heure dans une fenêtre modale centrée avec **Annuler** et **OK**.
- Le cadran étant isolé dans cette fenêtre, le geste circulaire ne fait plus défiler la page d’édition.
- Version : `1.5.4`, `versionCode 12`.

## 1.5.3 (versionCode 11)

- Lorsqu’une alarme désactivée est ouverte dans l’éditeur puis enregistrée, elle est automatiquement réactivée.
- Le bouton ON/OFF de la liste conserve son comportement indépendant : désactiver une alarme depuis la liste ne la réactive pas.
- Version : `1.5.3`, `versionCode 11`.

## 1.5.2 (versionCode 10)

- L’écran de sonnerie reste utilisable au-dessus de l’écran verrouillé, sans déverrouiller le téléphone.
- Le scanner QR peut désormais être ouvert et utilisé directement pendant que le téléphone reste verrouillé.
- Le défi mathématique reste utilisable sur l’écran de sonnerie verrouillé.
- Le téléphone n’est jamais déverrouillé par l’application : après validation QR/math, l’alarme s’arrête et Android reste verrouillé.
- Compatibilité renforcée avec Android 8.0 (API 26) pour l’affichage au-dessus de l’écran verrouillé.
- Version : `1.5.2`, `versionCode 10`.

## 1.5.1 (versionCode 9)

- Suppression du blocage qui imposait d’enregistrer ou d’imprimer le QR avant de sauvegarder une nouvelle alarme.
- Ajout d’un bouton de duplication sur chaque alarme de la liste.
- Une copie est créée désactivée avec un nouvel identifiant.
- Une alarme utilisant un QR individuel reçoit un nouveau QR individuel lors de la duplication.

## 1.5.0 (versionCode 8)

- Ajout d’un bouton **Imprimer le QR sur A4** sous l’enregistrement dans la galerie.
- Le document d’impression est préparé en **A4 portrait**, avec un QR compact d’environ **35 mm**, placé à environ **10 mm du coin supérieur gauche**.
- La création d’une nouvelle alarme QR est désormais autorisée dès que l’utilisateur a soit :
  - enregistré le QR dans la galerie ;
  - ouvert l’impression A4.
- Le blocage est réactivé si le QR change (par exemple passage de QR commun à QR individuel).
- Internationalisation complète de l’interface utilisateur :
  - système en français → interface française ;
  - toute autre langue → interface anglaise par défaut.
- Localisation des écrans, alertes, notifications persistantes, notification de réveil et scanner QR.
- Conservation de la signature Release permanente et de l’icône PNG actuelle.
- Version : `1.5.0`, `versionCode 8`.

## 1.4.0

- Sélecteur d’heure modernisé.
- Dégradé turquoise → vert.
- Vibration et mode test améliorés.
- Buy me a coffee.
