# Utilisation de l’IA — TP4 RAG Web

Étudiant : Omar Zouitina. Outil : ChatGPT (OpenAI).

## Base et aide apportée

Le projet reprend l’application du TP2 du dépôt `tp2-web-zouitina` (base `f7f9c68`). À ma demande, ChatGPT a préparé le nouveau projet Jakarta EE 11 pour Payara 7, adapté les packages et les dépendances, et rédigé les ajouts du TP4. L’aide comprend la génération de classes complètes, des tests automatisés et de la documentation, puis leur ajout sur GitHub.

Les documents `rag.pdf` et `autre.pdf` sont repris du dépôt des tests du TP4. `rag.pdf` est le support de Richard Grin. `autre.pdf` est un document touristique sur Nice produit avec ChatGPT lors des tests précédents.

## Transformation des questions

ChatGPT a ajouté CompressingQueryTransformer au pipeline RAG, en utilisant l’historique propre à la vue. Il a rédigé un test du client qui vérifie que la question de suivi reformulée est transmise au récupérateur. Le modèle de ce test est simulé ; le test valide le branchement des composants, pas la qualité linguistique de Gemini.

## Vérifications

L’assistant a compilé le projet et exécuté des tests sur les deux vrais PDF. Des tests avec un modèle simulé vérifient le maintien d’une conversation, l’absence de partage de l’historique entre deux clients et le rétablissement de l’historique après un appel refusé. Ces réponses simulées ne sont pas des réponses de Gemini.

La page JSF a été chargée sur Payara Micro 7.2026.10 pour vérifier le déploiement. Le script de vérification du formulaire teste aussi les questions vides, l’absence de clé et deux tours de conversation avec les vrais PDF et un modèle simulé. Aucun appel à Gemini avec ma clé personnelle n’a été effectué par l’assistant. La validation de la qualité des réponses réelles reste à effectuer dans mon environnement Payara.

## Commits

J’ai demandé un commit par étape avec au moins cinq minutes réelles entre les commits. Les dates des commits ne sont pas antidatées.

## Limites

Ce document est une synthèse, pas une transcription intégrale de la conversation. Aucun pourcentage d’assistance n’a été mesuré. Les secrets doivent être fournis au serveur par des variables d’environnement ; ils ne sont pas inclus dans le dépôt.
