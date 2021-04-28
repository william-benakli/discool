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
    password TEXT NOT NULL,
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


CREATE TABLE IF NOT EXISTS moodle_pages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    homepage BIT NOT NULL,

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

CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duedate BIGINT NOT NULL,
    cutoffdate BIGINT,
    allowlate SMALLINT NOT NULL,
    maxgrade SMALLINT,
    maxattempts SMALLINT NOT NULL,

    CONSTRAINT fk_courseid_assignments
        FOREIGN KEY (courseid) REFERENCES courses(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students_assignments_uploads (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    assignmentid BIGINT UNSIGNED NOT NULL,
    courseid BIGINT UNSIGNED NOT NULL,
    studentid BIGINT UNSIGNED NOT NULL,
    grade INT,
    teachercomments TEXT,
    dateupload BIGINT NOT NULL,

    CONSTRAINT fk_courseid_assignments2
        FOREIGN KEY (courseid) REFERENCES courses(id),
    CONSTRAINT fk_studentid_assigments
        FOREIGN KEY (studentid) REFERENCES users(id),
    CONSTRAINT fk_assignmentsid
        FOREIGN KEY (assignmentid) REFERENCES assignments(id),

    PRIMARY KEY(id)
) ENGINE=InnoDB;

-- the passwords are as follows :
-- admin : password
-- teacher1 : t1
-- teacher2 : t2
-- teacher3 : t3
-- student1 : s1
-- student2 : s2
-- student3 : s3
INSERT INTO users VALUES (1, "admin", "$2a$10$xEJmnRr.0TdrUJANERV73eii9hgKzS7mkaECxbHiZ6JPIt5lVN1NG", 0, "admin_fn", "admin_ln", "adminemail", "description admin", "website admin", 0, 0, 0);
INSERT INTO users VALUES (2, "teacher1", "$2a$10$cyHZi9oHM50SGG.P00hT5utYa0CGAVj0boZ5vdOfsk7r2kPN6aQwa", 1, "teacher1_fn", "teacher1_ln", "teacher1email", "description teacher 1", "website teacher1", 0, 0, 0);
INSERT INTO users VALUES (3, "teacher2", "$2a$10$OVpL9EEtJt8Ity1ebpcVoOM7wRWzCCQDL8x/9ulVsYpCURPYbUM5K", 1, "teacher2_fn", "teacher2_ln", "teacher2email", "description teacher 2", "website teacher2", 0, 0, 0);
INSERT INTO users VALUES (4, "teacher3", "$2a$10$jFBAvugjpUAyQ0nOsaMBqOkp7KLM525gW6QLIUsN2tccSGqrIu33G", 1, "teacher3_fn", "teacher3_ln", "teacher3email", "description teacher 3", "website teacher3", 0, 0, 0);
INSERT INTO users VALUES (5, "student1", "$2a$10$fSSMggiuJz/g1rQBUtAO0OXwMmGSAIUUIdplyjY7hr7iFXtQsVNiC", 2, "student1_fn", "student1_ln", "student1email", "description student 1", "", 0, 0, 0);
INSERT INTO users VALUES (6, "student2", "$2a$10$o3NawTTPYqS3ZSaaMryK5eaYbq8spNfNeHtxh6oSZZdGbfRLtmYSy", 2, "student2_fn", "student2_ln", "student2email", "description student 2", "", 0, 0, 0);
INSERT INTO users VALUES (7, "student3", "$2a$10$EaQJlro9r.ATqKjcNyh3ZefSTGsaHs8p/L5YM8.rHCzHHNaE1iume", 2, "student3_fn", "student3_ln", "student3email", "description student 3", "", 0, 0, 0);

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

INSERT INTO moodle_pages VALUES
    (NULL, 1, "course 1 page 1", "course 1 **page** 1 content", 1),
    (NULL, 1, "course 1 page 2", "course 1 ~~page 2~~ content", 0),
    (NULL, 1, "course 1 page 3", "link : [link to google.fr](http://google.fr)", 0),
    (NULL, 1, "course 1 page 4", "course 1 page 4 content", 0),
    (NULL, 2, "course 2 page 1", "course 2 page 1 content", 1),
    (NULL, 2, "course 2 page 2", "course 2 page 2 content", 0),
    (NULL, 2, "course 2 page 3", "course 2 page 3 content", 0),
    (NULL, 2, "course 2 page 4", "course 2 page 4 content", 0),
    (NULL, 3, "course 3 page 1", "course 3 page 1 content", 1),
    (NULL, 3, "course 3 page 2", "course 3 page 2 content", 0),
    (NULL, 3, "course 3 page 3", "course 3 page 3 content", 0),
    (NULL, 3, "course 3 page 4", "course 3 page 4 content", 0);

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

INSERT INTO assignments VALUES
    (NULL, 1, "test assignment", "this is a test !", 0, 0, 1, 20, 3),
    (NULL, 2, "test2 assignment", "this is a test2 !", 0, 0, 1, 20, 3),
    (NULL, 3, "test3 assignment", "this is a test3 !", 0, 0, 1, 20, 3),
    (NULL, 1, "test4 assignment", "this is a test4 !", 0, 0, 1, 20, 3);
