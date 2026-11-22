# TP4 RAG Web — Omar Zouitina

Application Jakarta EE 11 / Jakarta Faces, reprise du TP2 pour Payara 7.
Java 21 minimum. Le projet fonctionne aussi avec un JDK 25.

## Configuration

Définir GEMINI_KEY dans l’environnement du processus Payara.
Le modèle par défaut est gemini-3.5-flash-lite. Sa disponibilité et ses quotas dépendent du compte Google : utiliser GEMINI_MODEL pour choisir un autre modèle disponible. Aucune clé Tavily n’est nécessaire pour ces deux fonctionnalités.

## Fonctionnement

Conversation du TP2 enrichie par les deux PDF. Deux fonctionnalités des tests du TP4 sont intégrées :

1. **Transformation des questions (test 2)** : `CompressingQueryTransformer` utilise l’historique pour reformuler les questions de suivi.
2. **Routage (test 3)** : `LanguageModelQueryRouter` choisit le ou les PDF adaptés à la question reformulée.

La chaîne est : question + historique → reformulation → routage → recherche dans le ou les magasins sélectionnés → prompt augmenté → réponse. Le nom des PDF réellement fournis est affiché sous la réponse. Le modèle peut sélectionner les deux sources si la question couvre les deux thèmes.

## Documents

- `rag.pdf` : support de Richard Grin, Fine-tuning et RAG.
- `autre.pdf` : repères touristiques sur Nice, repris du dépôt des tests ; document produit avec ChatGPT.

Les deux documents sont indexés séparément, une seule fois par déploiement.
L'index en mémoire est partagé ; l'historique est propre à chaque vue JSF.

## Compilation et déploiement

```powershell
.\mvnw.cmd clean package
$asadmin = 'C:\autresprogrammes\payara-7.2026.10\payara7\bin\asadmin.bat'
& $asadmin deploy --force=true --name tp4-rag-web-zouitina --contextroot tp4-rag-web-zouitina .\target\tp4-rag-web-zouitina.war
```

Ouvrir http://localhost:8080/tp4-rag-web-zouitina/.
Payara doit hériter de GEMINI_KEY ; la définir seulement dans la configuration d'une classe Java ne suffit pas.
GEMINI_MODEL permet de remplacer le modèle sans modifier le code.
Définir RAG_LOG_REQUESTS=true avant le lancement de Payara pour voir les prompts dans ses logs.
Les logs contiennent les questions et les extraits documentaires : les garder localement.

## Sources et attribution

- Énoncé : http://richard.grin.free.fr/emsi/casablanca-ia/tp/tp4-rag/index.html
- Base reprise : https://github.com/omar0043/tp2-web-zouitina
- PDF repris du dépôt des tests : https://github.com/omar0043/tp4-rag-tests-zouitina

Les fonctions existantes du TP2 (rôles, historique, nouveau chat, copie) sont conservées.
La liste des rôles utilise les composants Jakarta Faces standards.

## Première fonctionnalité : transformation des questions

Dans une même conversation, poser :

1. « Qu’est-ce que le fine-tuning ? »
2. « Dans le support fourni, à quoi c’est comparé ? »

Avec RAG_LOG_REQUESTS=true, vérifier le prompt du transformateur contenant l’historique, sa question autonome puis le prompt augmenté envoyé pour générer la réponse. Ce scénario est à exécuter avec Gemini ; une reformulation correcte ne garantit pas que les passages du PDF contiennent la comparaison attendue.

## Tests sans clé API

`./mvnw clean verify` exécute les tests JUnit avec les vrais PDF et des modèles simulés.

Pour vérifier le formulaire sur Payara Micro 7 (JAR à télécharger séparément) :

```text
python scripts/smoke_payara.py /chemin/payara-micro-7.2026.10.jar
python scripts/smoke_payara.py /chemin/payara-micro-7.2026.10.jar --mock
```

Le second test construit un WAR distinct dans `target/smoke/` avec un modèle simulé. Ne pas déployer ce WAR comme application finale. Le WAR normal ne contient pas ce modèle de test.

## Deuxième fonctionnalité : routage entre les documents

Après les deux questions sur le fine-tuning, poser :

3. « Que peut-on voir depuis la Colline du Château à Nice ? »

Dans les logs, vérifier le prompt qui décrit les deux sources et le choix du routeur. La ligne `Sources sélectionnées par le routeur` indique le ou les fichiers choisis. La page affiche séparément les PDF dont des passages ont réellement été fournis pour générer la dernière réponse. Un fichier sélectionné peut ne retourner aucun passage au-dessus du seuil 0,5.

Les noms des documents s’appuient sur les métadonnées de `Result<String>.sources()`, et non sur les citations rédigées par le modèle.

## Vérifications et limites

- Compilation et WAR vérifiés avec un JDK 21 ; cible Java 21, compatible avec le JDK 25 du poste de travail.
- Tests JUnit sur les vrais PDF et sur l’assemblage mémoire / reformulation / routage avec des modèles simulés.
- Déploiement et formulaire testés sur Payara Micro 7.2026.10. Le serveur Payara 7 de l’étudiant reste à configurer et à tester sur son poste.
- Aucun appel réel à Gemini n’a été exécuté par l’assistant pendant ce développement. Les tests simulés ne permettent pas de conclure sur la qualité des réponses de Gemini.
- Magasins vectoriels en mémoire : l’index est reconstruit après un redémarrage ou un redéploiement.
- Le seuil 0,5 et le modèle d’embeddings peuvent limiter les réponses en français. Le cours n’a pas fait l’objet d’une préparation documentaire approfondie.
- La première question initialise les embeddings ; elle peut prendre plus de temps.
- La reformulation, le routage et la réponse peuvent consommer plusieurs appels au modèle par question.

## Rendu et bonus Web

Dans le mail de rendu, indiquer les fonctionnalités des tests 2 et 3 et copier une conversation réellement obtenue avec Gemini depuis cette application Web. Les scénarios ci-dessus sont des suggestions de test, pas une conversation exécutée avec Gemini.
Consulter `IA.md` pour la déclaration d’assistance.

## Récupérer le projet

Ce projet est dans un nouveau dépôt, distinct des tests console :

```powershell
cd C:\projets
git clone https://github.com/omar0043/tp4-rag-web-zouitina.git
cd tp4-rag-web-zouitina
.\mvnw.cmd clean package
```

Dans IntelliJ, ouvrir le `pom.xml` comme projet Maven, choisir le JDK 21 ou 25 et Payara 7 comme serveur. Ajouter l’artefact WAR au déploiement et donner `/tp4-rag-web-zouitina` comme contexte. Si le port 4848 est occupé par ton serveur déjà lancé, déployer avec `asadmin` sur cette instance au lieu de démarrer une seconde instance.
