# Utilisation de l’IA — TP4 RAG Web

**Étudiant :** Omar Zouitina  
**Outil utilisé :** ChatGPT (OpenAI)

## Assistance utilisée

J’ai utilisé ChatGPT pour adapter l’application du TP2 au TP4 : configuration Maven, génération de code Java pour le RAG avec deux PDF, reformulation des questions et routage entre les documents. L’assistance a aussi porté sur la correction d’erreurs, la rédaction de tests automatisés, la documentation et les commits sur GitHub.

Le fichier `rag.pdf` est le support de cours de Richard Grin. Le document touristique `autre.pdf` a été produit avec ChatGPT pendant les tests précédents.

## Travail et vérifications personnels

J’ai récupéré le projet sur mon poste, exécuté la compilation Maven, démarré Payara et déployé l’application. J’ai testé les échanges réels avec Gemini et transmis les résultats et les captures pour analyser les problèmes rencontrés, notamment la validation du rôle dans le formulaire.

J’ai posé des questions sur le fine-tuning puis sur la Colline du Château à Nice, ainsi qu’une question de suivi sur la vue du port. Les réponses obtenues citent respectivement `rag.pdf` et `autre.pdf`.

## Limites

Les tests automatisés utilisant un modèle simulé ont été exécutés par l’assistant ; les conversations réelles avec Gemini ont été testées sur mon poste. Une réponse qui tient compte du contexte ne suffit pas, à elle seule, à démontrer l’effet de la reformulation. La pertinence des réponses dépend aussi des passages récupérés.
