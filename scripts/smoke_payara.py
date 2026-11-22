"""Vérifie le vrai formulaire JSF sans appel externe ni clé API.
Usage : python scripts/smoke_payara.py /chemin/payara-micro-7.2026.10.jar
Prérequis : java sur PATH et .\u002fmvnw clean package exécuté.
"""
from html.parser import HTMLParser
from html import unescape
from http.cookiejar import CookieJar
from pathlib import Path
import os
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import zipfile

class Formulaire(HTMLParser):
    def __init__(self, texte):
        super().__init__()
        self.champs = {}
        self.action = ""
        self.feed(texte)

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "form":
            self.action = a["action"]
        if tag == "input" and a.get("type") == "hidden":
            self.champs[a["name"]] = a.get("value", "")
        if tag == "option" and "form:rolesysteme" not in self.champs:
            self.champs["form:rolesysteme"] = a["value"]

root = Path(__file__).resolve().parents[1]
war = root / "target/tp4-rag-web-zouitina.war"
mode_simule = "--mock" in sys.argv
if mode_simule:
    original = war
    war = root / "target/smoke/tp4-rag-web-zouitina.war"
    war.parent.mkdir(exist_ok=True)
    package = "ma/emsi/zouitina/tp4ragwebzouitina/llm"
    classes = list((root / "target/test-classes" / package).glob("MockModelProvider*.class"))
    if not classes:
        raise SystemExit("Compiler les tests avant de construire le WAR simulé.")
    with zipfile.ZipFile(original) as source, zipfile.ZipFile(war, "w", zipfile.ZIP_DEFLATED) as cible:
        for entree in source.infolist():
            cible.writestr(entree, source.read(entree.filename))
        for classe in classes:
            cible.write(classe, "WEB-INF/classes/" + package + "/" + classe.name)
if not war.is_file():
    raise SystemExit("Compiler le WAR avant ce test.")
url = "http://127.0.0.1:8180/tp4-rag-web-zouitina/"
opener = urllib.request.build_opener(
    urllib.request.ProxyHandler({}), urllib.request.HTTPCookieProcessor(CookieJar()))
env = os.environ.copy()
env.pop("GEMINI_KEY", None)
log = root / "target/payara-smoke.log"
with log.open("w") as sortie:
    serveur = subprocess.Popen(["java", "-jar", sys.argv[1], "--port", "8180", "--nocluster",
                                "--deploy", str(war)], env=env, stdout=sortie, stderr=subprocess.STDOUT)
    try:
        limite = time.monotonic() + 55
        while True:
            try:
                page = opener.open(url, timeout=2).read().decode()
                break
            except OSError:
                if time.monotonic() > limite or serveur.poll() is not None:
                    raise RuntimeError("Payara n'a pas démarré ; consulter " + str(log))
                time.sleep(1)
        assert "TP4 RAG" in page and "jakarta.faces.ViewState" in page
        print("OK : page JSF et formulaire chargés")

        def envoyer(page, question):
            f = Formulaire(page)
            f.champs.update({"form:question": question, "form:envoyer": "Envoyer la question"})
            data = urllib.parse.urlencode(f.champs).encode()
            return opener.open(urllib.parse.urljoin(url, f.action), data=data, timeout=20).read().decode()

        page = envoyer(page, "   ")
        assert "Il manque le texte de la question" in page
        print("OK : question vide refusée")
        if mode_simule:
            page = envoyer(page, "Qu’est-ce que le fine-tuning ?")
            (root / "target/smoke-page.html").write_text(page)
            assert "SIMULATION : réponse de test" in unescape(page)
            page = envoyer(page, "Dans le cours, à quoi c’est comparé ?")
            assert "SIMULATION : réponse de test" in unescape(page)
            assert "TEST_REWRITE=Dans le cours fourni, à quoi le fine-tuning" in log.read_text()
            print("OK : deux tours JSF, reformulation et récupération des vrais PDF (LM simulé)")
        else:
            page = envoyer(page, "Bonjour")
            assert "Impossible d’obtenir une réponse de Gemini." in page
            assert "Bonjour" in page
            print("OK : erreur contrôlée sans GEMINI_KEY, question conservée")
    finally:
        serveur.terminate()
        try:
            serveur.wait(timeout=5)
        except subprocess.TimeoutExpired:
            serveur.kill()
