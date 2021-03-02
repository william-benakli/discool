INSERT INTO users VALUES (1, "teacher1", 1, "t1firstname", "t1lastname", "teacher1email", "description teacher 1", "website teacher1", 0, 0, 0);
INSERT INTO courses VALUES 
    (NULL, "course1", 1, "course_pic/course1"),
    (NULL, "course 2", 1, "course_pic/course2"),
    (NULL, "course 3", 1, "course_pic/course3");

INSERT INTO channels VALUES 
    (NULL, 1, "text chat 1"),
    (NULL, 1, "text chat 2"),
    (NULL, 1, "text chat 3"),
    (NULL, 2, "text chat cours 2 n.1"),
    (NULL, 2, "text chat cours 2, n.2"),
    (NULL, 1, "text chat 4");

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

