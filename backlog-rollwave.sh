#!/usr/bin/env bash
# =============================================================================
# Crée les labels et le backlog (issues) du dépôt rollwave sur GitHub.
#
# Prérequis :
#   1. GitHub CLI installé : https://cli.github.com
#   2. Être connecté :       gh auth login
#   3. Lancer ce script DEPUIS le dossier de votre dépôt rollwave cloné
#      (gh détecte alors automatiquement le dépôt grâce au remote "origin").
#
# Lancement (Git Bash sous Windows, Terminal sous Mac/Linux) :
#   bash backlog-rollwave.sh
#
# Optionnel : ajouter chaque issue à votre projet Kanban GitHub.
#   - donnez d'abord à gh le droit de gérer les projets : gh auth refresh -s project
#   - puis lancez :  PROJECT="Rollwave" bash backlog-rollwave.sh
# =============================================================================
set -euo pipefail

REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
echo "Dépôt cible : $REPO"
read -r -p "Créer les labels et le backlog dans ce dépôt ? (o/N) " answer
[[ "$answer" =~ ^[oOyY]$ ]] || { echo "Annulé."; exit 0; }

# ----------------------------------------------------------------------------
# Labels (--force : met à jour un label s'il existe déjà, pas de doublon)
# ----------------------------------------------------------------------------
label() { gh label create "$1" --color "$2" --description "$3" --force >/dev/null && echo "  label : $1"; }

echo "Création des labels..."
label "feature"       "1D76DB" "Nouvelle fonctionnalité"
label "bug"           "D73A4A" "Quelque chose ne fonctionne pas"
label "tech"          "5319E7" "Tâche technique (infra, refactoring, configuration)"
label "test"          "0E8A16" "Tests automatisés"
label "doc"           "0075CA" "Documentation"
label "auth"          "C5DEF5" "Authentification et rôles"
label "skatepark"     "BFD4F2" "Skateparks et carte"
label "comment"       "D4C5F9" "Commentaires"
label "ci-cd"         "FEF2C0" "Intégration et déploiement continus"
label "prio: haute"   "B60205" "À traiter en priorité"
label "prio: moyenne" "FBCA04" "Priorité normale"
label "prio: basse"   "C2E0C6" "Peut attendre"

# ----------------------------------------------------------------------------
# Issues
# ----------------------------------------------------------------------------
issue() {
  local title="$1" labels="$2" state="$3" body="$4"
  local args=(--title "$title" --label "$labels" --body "$body")
  [[ -n "${PROJECT:-}" ]] && args+=(--project "$PROJECT")
  local url
  url=$(gh issue create "${args[@]}")
  if [[ "$state" == "done" ]]; then
    gh issue close "$url" --comment "Déjà réalisé avant la mise en place du backlog." >/dev/null
    echo "  fermée : $title"
  else
    echo "  ouverte : $title"
  fi
}

echo "Création des issues..."

# --- Authentification et rôles -------------------------------------------
issue "[Auth] Inscription d'un utilisateur" "feature,auth,prio: haute" done \
"**En tant que** visiteur, **je veux** créer un compte **afin de** pouvoir me connecter.

### Critères d'acceptation
- [x] \`POST /auth/signup\` crée un utilisateur et renvoie 201 avec un \`AppUserDto\` sans mot de passe
- [x] Le mot de passe est haché avec BCrypt
- [x] Un email déjà utilisé renvoie 409
- [x] Des données invalides renvoient 400 (\`@Valid\`)"

issue "[Auth] Connexion et obtention d'un token JWT" "feature,auth,prio: haute" done \
"**En tant qu'**utilisateur, **je veux** me connecter **afin d'**obtenir un token pour accéder aux routes protégées.

### Critères d'acceptation
- [x] \`POST /auth/login\` renvoie 200 avec \`{ token, expiresIn }\`
- [x] Des identifiants incorrects renvoient 401
- [x] Le token est valable une heure"

issue "[Auth] Ajouter les rôles USER et ADMIN" "feature,auth,prio: haute" open \
"**En tant qu'**administrateur, **je veux** disposer de droits étendus **afin de** gérer les skateparks et modérer les commentaires.

### Critères d'acceptation
- [ ] Enum \`Role\` (USER, ADMIN) et champ \`role\` dans \`AppUser\`
- [ ] \`getAuthorities()\` renvoie \`ROLE_USER\` ou \`ROLE_ADMIN\`
- [ ] L'inscription publique attribue toujours \`USER\`
- [ ] Règles \`hasRole(\"ADMIN\")\` dans \`SecurityConfiguration\`
- [ ] Création d'un compte administrateur au démarrage (\`DataInitializer\`)"

issue "[Auth] Réponses 401 et 403 au format ProblemDetail" "tech,auth,prio: moyenne" open \
"Les refus d'accès décidés par Spring Security renvoient aujourd'hui un 403 vide, y compris pour un visiteur sans token.

### Critères d'acceptation
- [ ] Un visiteur sans token reçoit 401
- [ ] Un utilisateur sans le bon rôle reçoit 403
- [ ] Les deux réponses ont le format \`ProblemDetail\` de l'\`ExceptionResolver\`
- [ ] \`authenticationEntryPoint\` et \`accessDeniedHandler\` configurés dans \`SecurityConfiguration\`"

issue "[Auth] Consulter mon profil" "feature,auth,prio: basse" open \
"**En tant qu'**utilisateur connecté, **je veux** consulter mon profil **afin d'**afficher mes informations.

### Critères d'acceptation
- [ ] \`GET /users/me\` renvoie l'utilisateur connecté (\`AppUserDto\`, sans mot de passe)
- [ ] L'utilisateur est déduit du token via \`getConnectedUser()\`
- [ ] Sans token : 401"

# --- Skateparks -----------------------------------------------------------
issue "[Skatepark] Rechercher un skatepark par adresse" "feature,skatepark,prio: haute" open \
"**En tant que** visiteur, **je veux** rechercher un skatepark par son adresse **afin de** trouver un spot près de chez moi.

### Critères d'acceptation
- [ ] Recherche partielle et insensible à la casse (\`findByAdressContainingIgnoreCase\`)
- [ ] Route publique, accessible sans token
- [ ] Renvoie une liste de DTO (jamais les entités)"

issue "[Skatepark] Afficher les spots de la zone visible de la carte" "feature,skatepark,prio: haute" open \
"**En tant que** visiteur, **je veux** voir les skateparks de la zone affichée sur la carte **afin d'**explorer les spots autour de moi.

### Critères d'acceptation
- [ ] Route publique recevant minLat, maxLat, minLng, maxLng
- [ ] Utilise \`findByLatitudeBetweenAndLongitudeBetween\`
- [ ] Les bornes sont remises dans l'ordre (min avant max) si besoin
- [ ] Renvoie une liste de DTO"

issue "[Skatepark] Proposer un nouveau spot" "feature,skatepark,prio: moyenne" open \
"**En tant qu'**utilisateur connecté, **je veux** proposer un skatepark **afin d'**enrichir la carte.

### Critères d'acceptation
- [ ] Route réservée aux utilisateurs connectés
- [ ] L'auteur du spot est déduit du token (\`getConnectedUser()\`), jamais du body
- [ ] Données validées (nom, adresse, latitude, longitude)
- [ ] Renvoie 201 avec le DTO du skatepark créé"

issue "[Skatepark] Lister les spots officiels" "feature,skatepark,prio: basse" open \
"**En tant que** visiteur, **je veux** voir les skateparks officiels **afin de** les distinguer des spots proposés par les utilisateurs.

### Critères d'acceptation
- [ ] Utilise \`findByUserIsNull\`
- [ ] Route publique
- [ ] Renvoie une liste de DTO"

issue "[Skatepark] Gérer les skateparks (administrateur)" "feature,skatepark,auth,prio: moyenne" open \
"**En tant qu'**administrateur, **je veux** créer, modifier et supprimer des skateparks **afin de** maintenir la carte à jour.

### Critères d'acceptation
- [ ] POST, PUT et DELETE réservés à \`ROLE_ADMIN\` (403 pour un USER)
- [ ] La suppression d'un skatepark supprime ses commentaires (cascade + orphanRemoval)
- [ ] Skatepark inexistant : 404"

# --- Commentaires ---------------------------------------------------------
issue "[Comment] Lire les commentaires d'un skatepark" "feature,comment,prio: haute" open \
"**En tant que** visiteur, **je veux** lire les commentaires d'un skatepark **afin de** connaître l'avis des autres riders.

### Critères d'acceptation
- [ ] Route publique
- [ ] Commentaires triés du plus récent au plus ancien (\`findAllBySkateparkIdOrderByDateDesc\`)
- [ ] Le DTO contient \`skateparkId\` et non le skatepark entier (pas de boucle JSON)"

issue "[Comment] Commenter un skatepark" "feature,comment,prio: haute" open \
"**En tant qu'**utilisateur connecté, **je veux** commenter un skatepark **afin de** partager mon avis.

### Critères d'acceptation
- [ ] Route réservée aux utilisateurs connectés (401 sans token)
- [ ] L'auteur est déduit du token, jamais du body
- [ ] Contenu vide : 400
- [ ] Skatepark inexistant : 404
- [ ] Renvoie 201 avec le DTO du commentaire"

issue "[Comment] Modifier et supprimer mes commentaires" "feature,comment,prio: moyenne" open \
"**En tant qu'**utilisateur connecté, **je veux** modifier ou supprimer mes propres commentaires.

### Critères d'acceptation
- [ ] Seul l'auteur peut modifier ou supprimer son commentaire (sinon 403)
- [ ] Commentaire inexistant : 404
- [ ] Suppression : 204"

issue "[Comment] Modérer les commentaires (administrateur)" "feature,comment,auth,prio: basse" open \
"**En tant qu'**administrateur, **je veux** supprimer n'importe quel commentaire **afin de** modérer les contenus.

### Critères d'acceptation
- [ ] Un ADMIN peut supprimer tout commentaire
- [ ] Un USER ne peut supprimer que les siens (403 sinon)"

# --- Technique --------------------------------------------------------------
issue "[Tech] Mettre en place la CI/CD GitHub Actions et Render" "tech,ci-cd,prio: haute" done \
"### Critères d'acceptation
- [x] Workflow \`ci.yml\` : build et tests à chaque push et pull request
- [x] Déploiement sur Render uniquement si les tests passent
- [x] Image Docker multi-étapes (JDK 21 puis JRE 21)
- [x] Clé JWT externalisée dans une variable d'environnement"

issue "[Tech] Passer de H2 à PostgreSQL sur Render" "tech,ci-cd,prio: haute" open \
"Avec H2 en mémoire, toutes les données sont perdues à chaque redéploiement et mise en veille de Render.

### Critères d'acceptation
- [ ] Base PostgreSQL créée sur Render
- [ ] Driver PostgreSQL ajouté, H2 conservé uniquement pour les tests
- [ ] URL, utilisateur et mot de passe passés par variables d'environnement (\`DB_PASSWORD\`...)
- [ ] PostgreSQL lancé en local avec Docker pour le développement"

issue "[Test] Tests unitaires des services" "test,prio: moyenne" open \
"### Critères d'acceptation
- [ ] Tests Mockito de \`CommentService\` (création, auteur déduit du token, 404)
- [ ] Tests Mockito de \`SkateparkService\`
- [ ] Tests de \`AuthenticationService\` (inscription, email déjà utilisé, login)
- [ ] Les tests tournent dans la CI"

issue "[Doc] Documenter l'API avec Swagger" "doc,prio: basse" open \
"### Critères d'acceptation
- [ ] Dépendance springdoc-openapi ajoutée
- [ ] Page Swagger UI accessible en local
- [ ] Authentification par token Bearer configurée dans Swagger"

issue "[Tech] Configurer CORS pour le front-end" "tech,prio: basse" open \
"### Critères d'acceptation
- [ ] Origines autorisées configurées dans \`SecurityConfiguration\` (localhost du front en développement, domaine de production)
- [ ] En-tête \`Authorization\` autorisé
- [ ] Origines lues depuis la configuration, pas écrites en dur"

echo
echo "Terminé. Consultez vos issues : gh issue list --state all"
