20210205102621
#l2
#pi4

# Compte-rendu de la réunion du 05 février 2021

Présents : 
- tous le groupe
- les autres groupes
- M. Degorre

## Objectifs

Globalement la forme de discord (channels txt et vocaux/vidéos)

Il manque des "pages" : pouvoir écrire des longs textes facilement (pour le prof) => page structurée éditable
-> comme dans les pages moodle

Dans les salons chat, on pourrait poster des références vers les "pages"

On pourrait faire une synchronisation entre page et salon web : une modif de la page lance un msg dans le salon

## Point de départ

Rechercher un tuto pour écrire un client chat web en pur java

## Archi

- des clients ↔ un serveur (un seul, genre machine dans un garage => pas la peine de faire des archi répliquées, redondance, etc)

- serveur -> java
- client : pur java ? client web en javascript ?
- ou **Kotlin multi-plateforme ?** pour avoir un serveur et un client dans le même langage, avec qd même un client web (peut se compiler vers du javascript)

=> **Tout faire en java, sans paufiner le côté graphique** (client et serveur)
    - puis si on se sent, faire du Kotlin pour passer sur un client web
    - ce sera bcp moins risqué

- une bdd pour stocker les données (users, msg, ...)

- dans le client : séparer vue et modèle

## Websockets

Communication = **websocket** => **trouver une lib** : Ktor ? **Spring (plus complet)** ?
    
Socket par dessus HTTP, comme TCP
- on a une page web unique qui communique avec les websockets
- (pas on clique sur un lien et une nouvelle page s'affiche)

Intérêt : dans le client, réagir à des events du serveur de façon asynchrone, et vice-versa

au contraire du HTTP où le client est roi : il fait une requête et le serveur répond
Mais pour le chat, on veut les derniers messages sans avoir à raffraichir la page

Websocket = tuyau pour passer des obj sérialisés
=> le standard est JSON

### JSON
- standard pour sérialiser des obj sur le Web (se dit "jaisone")
- à l'origine, format de sérialisation pour les obj javascript

- **GSON** (par google) => pour sérialiser les obj java

### Protocole réseau
 protocole ↔ hiérarchie de classes de messages sérialisables

-> déterminer les événements pertinents

## Markdown

- pour formatter les msg
- trouver une lib : **Common Markdown (github commonmark)** ?

## Salons vocaux/vidéos
PAS LA PRIORITE
- il y a bcp de pb de gestion de flux, surtout si ça passe chez nous avec nos connections persos
sinon le standard est Web RTC
trouver une lib qui fait ça si on a bcp de temps

## JavaFX ou client web ?

- Si client lourd => JavaFX
    - faire les tutos openjfk.io pour faire un prototype

- sinon apprendre le javascript ?
- ou faire du Kotlin

## Base de données
- mysql ?
- mariadb !!
- sqlite => sans serveur en plus, basé sur des fichiers. Mais ça ne passe pas à l'échelle…

- penser à cacher la partie bdd derrière des DBO (un module gère toues les accès à la bdd et fourni les données au modèle sous une forme abstraite - des objets - suivant les interfaces exposées)

**lib java : JDBC**

## Gradle
- ajouter les imports vers les libs
- créer des projets différents pour client et serveur


## TODO

- [x] : make sure we have the same versions of java, gradle, …
    - [x] java 15
    - [x] envoyer un mail au prof -> William
- [x] : trouver une lib pour le websocket : Spring ?
- [x] : décider si on veut faire tout en java ? ~~ou client web~~
- [ ] : look at GSON
- [x] : choisir une bdd : mariadb
- [ ] : look at JBDC
- [ ] : look at Common Markdown (github)
- [ ] : écrire un doc précis spécifiant ce qu'on veut faire -> Laure
- [x] : dessiner des fausses captures d'écran (prototype sans programmation)
- [x] : Rechercher un tuto pour écrire un client chat web en pur java
- [ ] : est ce que l'API u-paris permet d'envoyer un token si la personne est bien authentifiée -> William

**IMPORTANT** : 
- [ ] DETERMINER LES PREMIERES TACHES SUR GITLAB
- [ ] mise en place des projets avec gradle (client ET serveur)
- [x] trouver le tuto de tchat websocket sous Spring
- [ ] faire des comptes sur serveur pour que les autres puissent y accéder -> Laure

Prochaine réunion : mardi 9/02 18h
