DROP DATABASE discool;
CREATE DATABASE discool;
USE discool;
CREATE TABLE IF NOT EXISTS config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,

    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    username VARCHAR(100) NOT NULL,
    role INT NOT NULL,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    website VARCHAR(100),
    firstlogin BIGINT NOT NULL,
    lastlogin BIGINT NOT NULL,
    timecreated BIGINT NOT NULL,

    PRIMARY KEY(id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS courses (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    name VARCHAR(255) NOT NULL,
    teacherid BIGINT UNSIGNED NOT NULL,
    pathicon VARCHAR(255) NOT NULL,
    CONSTRAINT fk_teacherid
        FOREIGN KEY(teacherid) REFERENCES users(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS course_sections (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    parentid BIGINT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,

    FOREIGN KEY (parentid) REFERENCES course_sections(id),

    CONSTRAINT fk_courseid_course
        FOREIGN KEY(courseid) REFERENCES courses(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS groups (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    enrolmentkey VARCHAR(50),
    CONSTRAINT fk_courseid2
        FOREIGN KEY(courseid) REFERENCES courses(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    groupid BIGINT UNSIGNED NOT NULL,
    userid BIGINT UNSIGNED NOT NULL,
    timeadded BIGINT NOT NULL,

    CONSTRAINT fk_groupid
        FOREIGN KEY(groupid) REFERENCES groups(id),
    CONSTRAINT fk_userid
        FOREIGN KEY(userid) REFERENCES users(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS direct_messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    useridfrom BIGINT UNSIGNED NOT NULL,
    useridto BIGINT UNSIGNED NOT NULL,
    subject VARCHAR(255),
    parentid BIGINT UNSIGNED,
    message TEXT NOT NULL,
    timecreated BIGINT NOT NULL,
    deleted BIT NOT NULL,

    CONSTRAINT fk_parentid_direct
    FOREIGN KEY(parentid) REFERENCES direct_messages(id),
    CONSTRAINT fk_useridfrom
	FOREIGN KEY (useridfrom) REFERENCES users(id),
    CONSTRAINT fk_useridto
	FOREIGN KEY(useridto) REFERENCES users(id),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS channels (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255),
    CONSTRAINT fk_courseid_channels
	FOREIGN KEY (courseid) REFERENCES courses(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    channelid BIGINT UNSIGNED NOT NULL,
    parentid BIGINT UNSIGNED,
    userid BIGINT UNSIGNED NOT NULL,
    timecreated BIGINT NOT NULL,
    message TEXT NOT NULL,
    deleted BIT NOT NULL,

    CONSTRAINT fk_parentid_posts
        FOREIGN KEY(parentid) REFERENCES posts(id),
    CONSTRAINT fk_userid_posts
        FOREIGN KEY(userid) REFERENCES users(id),
    CONSTRAINT fk_channelid
	FOREIGN KEY (channelid) REFERENCES channels(id),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO users VALUES (1, "admin", 1, "admin_fn", "admin_ln", "adminemail", "description admin", "website admin", 0, 0, 0);
INSERT INTO users VALUES (2, "teacher1", 1, "teacher1_fn", "teacher1_ln", "teacher1email", "description teacher 1", "website teacher1", 0, 0, 0);
INSERT INTO users VALUES (3, "teacher2", 1, "teacher2_fn", "teacher2_ln", "teacher2email", "description teacher 2", "website teacher2", 0, 0, 0);
INSERT INTO users VALUES (4, "teacher3", 1, "teacher3_fn", "teacher3_ln", "teacher3email", "description teacher 3", "website teacher3", 0, 0, 0);
INSERT INTO users VALUES (5, "student1", 1, "student1_fn", "student1_ln", "student1email", "description student 1", "", 0, 0, 0);
INSERT INTO users VALUES (6, "student2", 1, "student2_fn", "student2_ln", "student1email", "description student 2", "", 0, 0, 0);
INSERT INTO users VALUES (7, "student3", 1, "student3_fn", "student3_ln", "student1email", "description student 3", "", 0, 0, 0);

INSERT INTO courses VALUES
    (NULL, "course1", 1, "course_pic/course1.png"),
    (NULL, "course 2", 2, "course_pic/course2.png"),
    (NULL, "course 3", 3, "course_pic/course3.png");

INSERT INTO channels VALUES
    (NULL, 1, "text chat 1"),
    (NULL, 1, "text chat 2"),
    (NULL, 1, "text chat 3"),
    (NULL, 2, "text chat cours 2 n.1"),
    (NULL, 2, "text chat cours 2, n.2"),
    (NULL, 1, "text chat 4"),
    (NULL, 3, "chat course 3");

INSERT INTO course_sections VALUES
    (NULL, 1, 1, "course 1 *section* 1", "course 1 **section** 1 content"),
    (NULL, 1, 1, "course 1 ~section~ 2", "course 1 ~~section 2~~ content"),
    (NULL, 1, 2, "course 1 section 3", "link : [link to google.fr](http://google.fr)"),
    (NULL, 1, 3, "course 1 section 4", "course 1 section 4 content"),
    (NULL, 2, 5, "course 2 section 1", "course 2 section 1 content"),
    (NULL, 2, 5, "course 2 section 2", "course 2 section 2 content"),
    (NULL, 2, 6, "course 2 section 3", "course 2 section 3 content"),
    (NULL, 2, 7, "course 2 section 4", "course 2 section 4 content"),
    (NULL, 3, 9, "course 3 section 1", "course 3 section 1 content"),
    (NULL, 3, 9, "course 3 section 2", "course 3 section 2 content"),
    (NULL, 3, 10, "course 3 section 3", "course 3 section 3 content"),
    (NULL, 3, 11, "course 3 section 4", "course 3 section 4 content");

INSERT INTO groups VALUES
    (NULL, 1, "group dans le course 1", "", ""),
    (NULL, 2, "group dans le course 2", "", ""),
    (NULL, 3, "group dans le course 3", "", ""),
    (NULL, 1, "enseignants supp dans le course 1", "", ""),
    (NULL, 2, "enseignants dans le course 2", "", "");

INSERT INTO group_members VALUES
    (NULL, 1, 5, 0),
    (NULL, 1, 6, 0),
    (NULL, 1, 7, 0),
    (NULL, 2, 5, 0),
    (NULL, 2, 6, 0),
    (NULL, 3, 6, 0),
    (NULL, 3, 7, 0);
