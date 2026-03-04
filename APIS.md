# Configuration des APIs externes (HUMA)

L'application fonctionne sans ces APIs. Pour activer chaque service, configurez les variables d'environnement ou les propriétés système (`-D`).

## Slack (notification nouveau feedback)

- **Variable :** `slack.webhook.url`
- **Exemple :** `-Dslack.webhook.url=https://hooks.slack.com/services/XXX/YYY/ZZZ`
- Créer un "Incoming Webhook" dans l’app Slack pour obtenir l’URL. À chaque nouveau feedback, un message est envoyé sur le canal.

## Sentiment (analyse du texte des feedbacks)

- **Variable :** `sentiment.api.url` (défaut : `http://localhost:8001`)
- Démarrer l’API locale :
  - **Windows** (si `pip` n’est pas reconnu) :  
    `py -m pip install -r requirements.txt`  
    puis `py -m uvicorn main:app --host 0.0.0.0 --port 8001`
  - **Linux/macOS** :  
    `pip install -r requirements.txt` puis `uvicorn main:app --host 0.0.0.0 --port 8001`
- Le bouton "Voir sentiment" sur chaque carte feedback appelle `POST {url}/sentiment` avec `{"text":"..."}` et attend `{"sentiment":"positive|negative|neutral", "score":0.85}`.

## Trello (créer une carte depuis un feedback)

- **Variables :** `trello.api.key`, `trello.token`, `trello.list.id`
- Obtenir la clé et le token sur https://trello.com/power-ups/admin (connecté). Créez un Power-Up → onglet API Key → Generate a new API Key. Pour le token : ouvrez (remplacez VOTRE_CLE) : https://trello.com/1/authorize?expiration=30days&scope=read,write&response_type=token&key=VOTRE_CLE → Allow. List id : voir les listes d’un board avec https://api.trello.com/1/boards/BOARD_ID/lists?key=CLE&token=TOKEN. ; le "list id" est l’ID de la liste du tableau où créer les cartes.
- Exemple : `-Dtrello.api.key=xxx -Dtrello.token=yyy -Dtrello.list.id=zzz`
- En tant qu’admin RH, le bouton "Créer ticket Trello" crée une carte avec le contenu du feedback.

## Email (SMTP – mot de passe oublié, email de bienvenue)

- **Variables :** `smtp.user`, `smtp.password`
- Ou configurer directement dans `ForgotPasswordController` (SMTP Gmail avec mot de passe d’application).
- Si `smtp.user` et `smtp.password` sont renseignés, l’email de bienvenue est envoyé après inscription (`MailSender.sendWelcomeEmail`).

## Twilio (SMS – OTP mot de passe oublié)

- **Variables :** `twilio.sid`, `twilio.token`, `twilio.from` (numéro d’envoi au format E.164, ex: +1234567890)
- **Où les trouver :** https://console.twilio.com → Account Info (SID et Auth Token) ; numéro d’envoi : Phone Numbers → Manage → Active Numbers (format E.164).
- À renseigner dans `api-keys.local.properties` (ou `-D`). Une fois configuré, l’écran « Mot de passe oublié » propose **Envoyer le code par email** et **Envoyer le code par SMS**. Pour le SMS, l’utilisateur saisit son email + son numéro (ex: +33612345678 ou 0612345678).

## Face ID

- Démarrer l’API : `cd face-api` puis `py -m pip install -r requirements.txt` et `py -m uvicorn main:app --host 0.0.0.0 --port 8000` (sous Windows si `pip`/`uvicorn` ne sont pas dans le PATH).
- L’app envoie les requêtes à `http://localhost:8000` pour l’enrollment (inscription) et la vérification (connexion par visage).
