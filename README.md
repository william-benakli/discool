# Discool

Projet de PI4, S4 de L2 d'informatique à l'Université de Paris (2021)
Il s'agissait de créer un programme hybride entre Discord et Moodle qui permette aux enseignants de faire cours. Les professeurs ont à leur disposition des pages éditables pour écrire leurs cours, des salons de chat textuels pour parler aux étudiants et des pages de dépôts de devoir pour récupérer et noter le travail de leurs élèves.

Membres du groupe  :
- [Runser Laure](https://github.com/laurerunser)
- [Benakli William](https://github.com/WilliamTmtc)
- [Saad David](https://github.com/Zouliwood)
- [Bouanem Yani Akli](https://github.com/yaniakli)

Vous trouverez les comptes-rendus de réunions dans le dossier `Dvtp`.

# Pré-requis
- Maven
- MariaDB, avec un utilisateur root, sur le port 8006
- Java 15

# Installation
- Lancez le script `install.sh` depuis la racine du projet. Entrez le nom d'utilisateur root de mariadb et son mot de passe.
Le script crée un utilisateur, une base de données, et la remplit avec des valeurs par défaut. Puis il lance `mvn install`.

Si vous souhaitez entrer vos propres données, vous pouvez modifier le fichier `build_db.sh` dans `src/main/resources`.
Si vous voulez changer le port, modifiez la ligne correspondante dans `src/main/resources/application.properties`.

Si le programme vous dit que votre utilisateur distant ne peut pas se connecter à la base de données, essayez de changer la `bind-address` dans le configuration de votre serveur mariadb (dans `50-server.cnf`) en `0.0.0.0` (au lieu de `127.0.0.1`) .

# Lancement

Pour lancer le projet : `mvn spring-boot:run`.
Le projet sera lancé sur le port `8080`. Pour changer le port, modifiez la ligne correspondante dans `src/main/resources/application.properties`.

Vous pouvez ensuite vous connecter au site avec les identifiants et mots de passe suivants :

| Identifiant | Mot de passe | Rôle       |
|-------------|--------------|------------|
| admin       | password     | ADMIN      |
| teacher1    | t1           | PROFESSEUR |
| teacher2    | t2           | PROFESSEUR |
| teacher3    | t3           | PROFESSEUR |
| student1    | s1           | ETUDIANT   |
| student2    | s2           | ETUDIANT   |
| student3    | s3           | ETUDIANT   |

# Fonctionnalités

## Cours

- Les utilisateurs ont accès à un certain nombre de cours (équivalent des serveurs de Discord, ou des cours de Moodle). Ils peuvent naviguer entre les cours dans le menu en haut au milieu. Les professeurs et les admins peuvent ajouter des cours avec le bouton "+" à la droite de la liste des cours.
- Dans chaque cours, les utilisateurs ont accès à 3 types de pages (visibles sur la partie de gauche) :
    - les pages Moodle
    - les dépôts de devoirs
    - les salons textuels
- Les professeurs peuvent créer ces trois types de pages avec le bouton "+" en haut du menu de gauche.
- Sur le menu de droite, les utilisateurs peuvent voir les autres utilisateurs du groupe et savoir s'ils sont en ligne ou non.

### Pages Moodle
Ce sont des pages éditables en Markdown par les professeurs, et visibles par les étudiants. Les professeurs peuvent y mettre du texte, des images, des fichiers et des liens.
Nous avons créé un utilitaire de génération de liens Markdown pour aider les professeurs avec les syntaxes. Les professeurs peuvent ainsi renvoyer les élèves vers des pages web externes, mais aussi faire des liens vers les divers salons de chat, les dépôts de devoirs et les autres pages Moodle.

### Dépôts de devoirs
Les élèves peuvent rendre des fichiers sur le même principe de les dépôts Moodle. 
Les professeurs ont en plus accès à une page pour télécharger les rendus (sous format .tar.gz) et noter les élèves. Les notes et commentaires sont ensuite visibles pour les élèves.

### Salons textuels
Les élèves et les professeurs peuvent échanger dans des salons textuels. On peut y envoyer du texte, des images et des fichiers.
Les professeurs ont aussi la possibilité de restreindre l'accès des salons (pour en faire des salons privés entre professeurs) ou d'empêcher les élèves d'y poster (pour faire des salons 'annonces' ou 'read-only').
Tous les utilisateurs peuvent éditer et supprimer leurs propres messages. Les messages supportent la syntaxe Markdown.

## Paramètres des utilisateurs
En haut à droite, le bouton "engrenages" permet à chaque utilisateur d'accéder à ses paramètres. 
On peut changer de mot de passe, d'email, de pseudo et de photo de profil.
Il y a un onglet pour paramétrer les périphériques d'audio et de vidéo, qui est inutile puique nous n'avons pas eu le temps d'implémenter un système de chat vocal.

## Panel d'administration
Le panel d'administration est disponible pour les utilisateurs qui ont le rôle d'ADMIN. On peut y accéder en cliquant sur le bouton "engrenages" en haut à droite, puis sur le bouton "Panel admin".

Dans ce panel, les admins peuvent voir :
- la liste de tous les cours : ils peuvent être modifiés et supprimés. On peut aussi en créer de nouveaux.
- la liste de tous les utilisateurs : on peut modifier leurs informations personnelles et les supprimer. On peut aussi en ajouter de nouveaux à la main ou avec un fichier .csv 
- dans la liste des utilisateurs, il y a une colonne qui permet d'avoir accès à tous les messages envoyés par la personne, et de les supprimer directement.

## Messages privés entre utilisateurs
En haut à droite, le bouton "enveloppe" permet d'accéder aux messages privés. Chaque utilisateur peut créer des conversations avec quelqu'un d'autre. On peut ensuite naviguer entre les conversations dans le menu de gauche.
Pour créer une conversation, il est possible de rechercher les utilisateurs par pseudo ou par nom.

# Fonctionnalités à implémenter
Quelques fonctionnalités que nous n'avons pas eu le temps d'implémenter :
- un chat vocal/vidéo, avec partage d'écran : nous avons choisi le framwork Vaadin pour coder en Java. Il est cependant très difficile d'exécuter du javascript et nous n'avons pas réussi à intégrer webRTC.
- un système de notifications : les utilisateurs ne sont pas prévenus lorqu'il y a un nouveau message ou des changements sur une page Moodle.
- un système de signalement : les utilisateurs devraient pouvoir signaler à l'administrateur et à leurs professeurs les contenus déplacés.
- une façon plus simple de créer des conversations privées : comme sur Discord, cliquer sur l'avatar de la personne dans la barre de droite devrait permettre de créer une conversation privée avec cette personne
- un système de mention : avec le @ comme sur Discord