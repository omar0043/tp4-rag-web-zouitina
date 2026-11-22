# Vérifications du TP4 Web

## Environnement utilisé par l’assistant

- JDK 21 (Zulu).
- Maven 3.9.16.
- Payara Micro 7.2026.10.
- Jakarta EE Web Profile 11, Jakarta Faces 4.1.

## Compilation et tests Java

Commande Maven exécutée : `clean verify`. Résultat : **BUILD SUCCESS**, **8 tests, 0 échec, 0 erreur**.

| Vérification | Résultat | Modèle |
| --- | --- | --- |
| Indexer les deux vrais PDF une seule fois | Réussi | Embeddings locaux réels |
| Retrouver un passage sur Nice et son origine | Réussi | Embeddings locaux réels |
| Retrouver un passage du cours | Réussi | Embeddings locaux réels |
| Conserver une conversation sans la partager entre deux clients | Réussi | ChatModel simulé |
| Restaurer l’historique après une erreur | Réussi | ChatModel simulé |
| Rechercher avec une question de suivi reformulée | Réussi | ChatModel simulé |
| Router vers Nice, exclure le cours et remonter le fichier source | Réussi | ChatModel simulé |

## Formulaire JSF sur Payara

Commandes exécutées après compilation :

```text
python scripts/smoke_payara.py <payara-micro-7.2026.10.jar>
python scripts/smoke_payara.py <payara-micro-7.2026.10.jar> --mock
```

Vérifications réussies : page et état JSF chargés, question vide refusée, message d’erreur contrôlé sans clé API, deux tours de conversation avec reformulation, routage vers le guide de Nice, affichage du PDF utilisé et nouveau chat sans ancien historique. Les scénarios de conversation utilisent un WAR de test avec un modèle simulé et les vrais PDF. Le modèle simulé n’est pas inclus dans le WAR normal.

## À vérifier sur le poste de l’étudiant

Configurer GEMINI_KEY pour le processus Payara, choisir un modèle disponible dans le compte, déployer le WAR normal et exécuter les scénarios du README. Vérifier les réponses réelles et les choix du modèle dans les logs. Aucun résultat de conversation simulée ne doit être présenté comme un échange réel avec Gemini dans le rendu.

## Correction du menu des rôles

Les valeurs du menu sont désormais des identifiants simples (`documentaire`, `traducteur`, `guide`) ; les prompts restent sur le serveur. Cela évite les différences de fins de ligne entre une valeur HTML multiligne et la valeur attendue par JSF. Le test du formulaire normalise désormais les champs en CRLF comme un navigateur. Un test Java supplémentaire vérifie les identifiants et leur association aux prompts.
