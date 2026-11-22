# TP4 RAG Web — Omar Zouitina

Application Jakarta EE 11 / Jakarta Faces, reprise du TP2 pour Payara 7.
Java 21 minimum. Le projet fonctionne aussi avec un JDK 25.

## Configuration

Définir GEMINI_KEY dans l’environnement du processus Payara.
Le modèle par défaut est gemini-3.5-flash-lite.

## État de cette étape

Conversation du TP2 enrichie par les deux PDF. Les deux récupérateurs sont encore interrogés à chaque question.

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
