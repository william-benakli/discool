CREATE TABLE IF NOT EXISTS config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,

    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    username VARCHAR(100) NOT NULL,
    role TINYINT NOT NULL,
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

    CONSTRAINT fk_teacherid
        FOREIGN KEY(teacherid) REFERENCES users(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courseSections (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    parentid BIGINT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,

    CONSTRAINT fk_courseid
        FOREIGN KEY(courseid) REFERENCES courses(id),
    CONSTRAINT fk_parentid
        FOREIGN KEY(parentid) REFERENCES courseSections(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS groups (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    enrolmentkey VARCHAR(50) NOT NULL,
    CONSTRAINT fk_courseid2
        FOREIGN KEY(courseid) REFERENCES courses(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS groupMembers (
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

CREATE TABLE IF NOT EXISTS unreadDMs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    useridfrom BIGINT UNSIGNED NOT NULL,
    useridto BIGINT UNSIGNED NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    timecreated BIGINT NOT NULL,

    CONSTRAINT fk_useridfrom 
	FOREIGN KEY (useridfrom) REFERENCES users(id),
    CONSTRAINT fk_useridto 
	FOREIGN KEY(useridto) REFERENCES users(id),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS readDMs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    useridfrom BIGINT UNSIGNED NOT NULL,
    useridto BIGINT UNSIGNED NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    timecreated BIGINT NOT NULL,

    CONSTRAINT fk_useridfrom_read 
	FOREIGN KEY (useridfrom) REFERENCES users(id),
    CONSTRAINT fk_useridto_read 
	FOREIGN KEY(useridto) REFERENCES users(id),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS channels (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    courseid BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255),
    CONSTRAINT fk_courseid_channels 
	FOREIGN KEY (courseid) REFERENCES users(id),
    PRIMARY KEY(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    channelid BIGINT UNSIGNED NOT NULL,
    parentid BIGINT UNSIGNED NOT NULL,
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
