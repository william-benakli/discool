20210202144253
#l2
#pi4
Back to L2S4 index : [[20210120112857]]
(ignore the stuff at the top)

# Database

This file presents the database we will use to create Discoodle.
We mostly copied the [existing database from Moodle](https://docs.moodle.org/dev/Database_schema_introduction).


## Config

This table allows us to set some general  config options.

| FIELD | TYPE         | OPTIONS  | MEANING                                           |
|-------|--------------|----------|---------------------------------------------------|
| id    | BIG INT      | NOT NULL | the id of the row                                 |
| name  | VARCHAR(255) | NOT NULL | the name of the option                            |
| value | VARCHAR(-1)  | NOT NULL | the value of the option (the -1 means max length) |

If needed, we can make a similar one but adding a "userid" field to set preferences for each user.

## Users and groups

### User

This table logs the users' data.

| FIELD       | TYPE         | OPTIONS  | MEANING                              |
|-------------|--------------|----------|--------------------------------------|
| id          | BIG INT      | NOT NULL | the id of the row                    |
| username    | VARCHAR(100) | NOT NULL |                                      |
| role        | ??           | NOT NULL | the role : student, teacher or admin |
| firstname   | VARCHAR(100) | NOT NULL |                                      |
| lastname    | VARCHAR(100) | NOT NULL |                                      |
| email       | VARCHAR(100) | NOT NULL |                                      |
| lang        | VARCHAR(30)  | NOT NULL | the preferred language               |
| timezone    | VARCHAR(100) | NOT NULL |                                      |
| firstlogin  | BIG INT      | NOT NULL | for monitoring purposes              |
| lastlogin   | BIG INT      | NOT NULL | for monitoring purposes              |
| description | VARCHAR(-1)  | NULL     | the person's bio                     |
| website     | VARCHAR(50)  | NULL     | the person's personal website        |

### Groups

This table contains the info about groups. A group is a list of students who belong to a certain course.

| FIELD        | TYPE         | OPTION   | MEANING                                        |
|--------------|--------------|----------|------------------------------------------------|
| id           | BIG INT      | NOT NULL | the row's id                                   |
| courseid     | BIG INT      | NOT NULL | foreign key to course.id                       |
| name         | VARCHAR(255) | NOT NULL |                                                |
| description  | VARCHAR(-1)  | NOT NULL | a description of who the ppl in this group are |
| enrolmentkey | VARCHAR(50)  | NOT NULL | the password needed to get into the group      |

### Group_members

This logs all the members of each group.

| FIELD     | TYPE    | OPTION   | MEANING                  |
|-----------|---------|----------|--------------------------|
| id        | BIG INT | NOT NULL | the row's id             |
| groupid   | BIG INT | NOT NULL | foreign key to groups.id |
| userid    | BIG INT | NOT NULL | foreign key to user.id   |
| timeadded | BIG INT | NOT NULL |                          |

## Courses

###  Courses

This table contains general info about the courses.

| FIELD   | TYPE         | OPTION   | MEANING                |
|---------|--------------|----------|------------------------|
| id      | BIG INT      | NOT NULL | the row's id           |
| name    | VARCHAR(255) | NOT NULL |                        |
| teacher | BIG INT      | NOT NULL | foreign key to user.id |

### Course_sections

This contains all the info and the content for sections in a course's homepage.

| FIELD    | TYPE      | OPTION   | MEANING                                                                                                                         |
|----------|-----------|----------|---------------------------------------------------------------------------------------------------------------------------------|
| id       | BIG INT   | NOT NULL | the row's id                                                                                                                    |
| courseid | BIG INT   | NOT NULL | foreign key to course.id                                                                                                        |
| parent   | BIG INT   | NOT NULL | foreign key to course_sections.id This is the id of the section that comes right before this one. If this is the top section, parentId = id |
| title    | VARCHAR   | NOT NULL | the title of this section                                                                                                       |
| content  | LONG TEXT | NOT NULL | the content of the section                                                                                                      |

## Assignments
The table contains the info about assignments.

| FIELD                     | TYPE         | OPTION   | MEANING                                                                           |
|---------------------------|--------------|----------|-----------------------------------------------------------------------------------|
| id                        | BIG INT      | NOT NULL | the row's id                                                                      |
| courseid                  | BIG INT      | NOT NULL | foreign key to courses.id                                                         |
| name                      | VARCHAR(255) | NOT NULL | the name of the assignment                                                        |
| intro                     | LONGTEXT     | NULL     | the description of the assignment                                                 |
| duedate                   | BIG INT      | NOT NULL | the due date of the assignment                                                    |
| allowsubmissionsafterdate | TINY INT     | NULL     | 1 if you allow assignments to be uploaded after the due date, 0 otherwise         |
| cutoffdate                | BIG INT      | NOT NULL | the final date after which you don't allow submissions                            |
| grade                     | BIG INT      | NOT NULL | the max grade for this assignment                                                 |
| maxattempts               | MEDIUM INT   | NOT NULL | the number of time the student can try to submit the assignment. -1 for infinite. |


## Messaging system : one-to-one

### Personal messages that haven't been read

| FIELD       | TYPE        | OPTION   | MEANING                       |
|-------------|-------------|----------|-------------------------------|
| id          | BIG INT     | NOT NULL | the row's id                  |
| useridfrom  | BIG INT     | NOT NULL | foreign key to user.id        |
| useridto    | BIG INT     | NOT NULL | foreign key to user.id        |
| subject     | VARCHAR(-1) | NOT NULL |                               |
| fullmessage | VARCHAR(-1) | NOT NULL |                               |
| timecreated | BIG INT     | NOT NULL | the time the message was sent |

### Personal messages that have been read

| FIELD       | TYPE        | OPTION   | MEANING                       |
|-------------|-------------|----------|-------------------------------|
| id          | BIG INT     | NOT NULL | the row's id                  |
| useridfrom  | BIG INT     | NOT NULL | foreign key to user.id        |
| useridto    | BIG INT     | NOT NULL | foreign key to user.id        |
| subject     | VARCHAR(-1) | NOT NULL |                               |
| fullmessage | VARCHAR(-1) | NOT NULL |                               |
| timecreated | BIG INT     | NOT NULL | the time the message was sent |
| timeread    | BIG INT     | NOT NULL | the time the message was read |

## Messaging system : public channels

### Channels

Moodle calls this "discussions" and puts each of them in a "forum".
Since each of our "channels" is going to be associated to a "course", I didn't include the "forums". 

We could include "forums" if we wanted to make categories of channels like in discord.

| FIELD    | TYPE         | OPTION   | MEANING                  |
|----------|--------------|----------|--------------------------|
| id       | BIG INT      | NOT NULL | the row's id             |
| courseid | BIG INT      | NOT NULL | foreign key to course.id |
| name     | VARCHAR(255) | NOT NULL | the name of the channel  |

### Posts

This is the table that contains all the posts that are written in the general channels (not the personal messagess => see Messaging system : one-to-one)

| FIELD       | TYPE      | OPTION              | MEANING                                                                                                                  |
|-------------|-----------|---------------------|--------------------------------------------------------------------------------------------------------------------------|
| id          | BIG INT   | NOT NULL            | the row's id                                                                                                             |
| channelid   | BIG INT   | NOT NULL            | foreign key to channels.id                                                                                               |
| parent      | BIG INT   | NOT NULL            | foreign key to posts.id This is the id of the post that comes right before that one.                                     |
| userid      | BIG INT   | NOT NULL            | foreign key to user.id This is the user who wrote the post                                                               |
| timecreated | BIG INT   | NOT NULL            | the time the user created the post                                                                                       |
| message     | LONG TEXT | NOT NULL            | the message                                                                                                              |
| deleted     | BIT       | NOT NULL, DEFAULT 0 | 1 if the message is deleted. It is not actually deleted from the database so that the admin & teachers can still see it. |


## Other stuff they do that are interesting

### config_log

To log who made what change when in the config


### Quizz

These tables contain all the info to implement quizzes. See there if we want to implement : https://docs.moodle.org/dev/Quiz_database_structure
The detail of all the columns : https://www.examulator.com/er/output/tables/quiz.html


And here the questions and answers tables : https://docs.moodle.org/dev/Question_database_structure

