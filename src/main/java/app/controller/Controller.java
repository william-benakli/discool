package app.controller;


import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.PublicTextChannel;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.Group;
import app.model.users.GroupMembers;
import app.model.users.Person;

import java.text.SimpleDateFormat;
import java.util.*;

public class Controller {

    private final PublicTextChannelRepository publicTextChannelRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final MoodlePageRepository moodlePageRepository;
    private final GroupRepository groupRepository;
    private final GroupMembersRepository groupMembersRepository;
    private final PrivateChatMessageRepository privateChatMessageRepository;

    public Controller(PersonRepository personRepository,
                      PublicTextChannelRepository publicTextChannelRepository,
                      PublicChatMessageRepository publicChatMessageRepository,
                      CourseRepository courseRepository,
                      MoodlePageRepository moodlePageRepository,
                      GroupRepository groupRepository,
                      GroupMembersRepository groupMembersRepository,
                      PrivateChatMessageRepository privateChatMessageRepository) {
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.publicTextChannelRepository = publicTextChannelRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.moodlePageRepository = moodlePageRepository;
        this.groupRepository = groupRepository;
        this.groupMembersRepository = groupMembersRepository;
        this.privateChatMessageRepository = privateChatMessageRepository;
    }

    public String getTitleCourse(long id) {
        Optional<Course> c = courseRepository.findById(id);
        return c.map(Course::getName).orElse(null);
    }

    public void deleteNullUsers() {
        personRepository.deleteNullUsers();
    }

    public void updateUserById(long id, String email, String username, String firstname, String lastname, String description, Person.Role role, String website) {
        personRepository.updateUserById(id, email, username, firstname, lastname, description, role, website);
    }

    public void addUser(String username, String password, Person.Role role, String firstname, String lastname, String email, String description, String website, long firstlogin, long lastlogin, long timecreated) {
        personRepository.addUser(username, password, role, firstname, lastname, email, description, website, firstlogin, lastlogin, timecreated);
    }

    public Person findByUsername(String name) {
        return personRepository.findByUsername(name);
    }

    public Person findUserById(long id) {
        return personRepository.findById(id);
    }

    public void saveUser(Person p) {
        personRepository.save(p);
    }

    public ArrayList<Person> findAllUsers() {
        return personRepository.findAll();
    }

    public List<Person> searchUser(String searchTerm) {
        return personRepository.search(searchTerm);
    }

    public void removeUserOnline(String name) {
        SecurityUtils.online.remove(name);
    }


    public List<Person> searchByUserName(String searchTerm) {
        return personRepository.searchByUserName(searchTerm);
    }

    public List<Person> searchByEmail(String searchTerm) {
        return personRepository.searchByEmail(searchTerm);
    }

    public Person getUsername(long id) {
        return personRepository.findById(id);
    }

    public boolean userExist(String username) {
        return findByUsername(username) != null;
    }

    public MoodlePage getLastMoodlePage(long courseId) {
        return moodlePageRepository.findFirstByCourseIdOrderByIdDesc(courseId);
    }

    public PublicTextChannel getLastTextChannelRepository(long courseId) {
        return publicTextChannelRepository.findFirstByCourseIdOrderByIdDesc(courseId);
    }

    public PublicTextChannel getTextChannel(long textChannelId) {
        return publicTextChannelRepository.findTextChannelById(textChannelId);
    }

    public MoodlePage getMoodlePage(long MoodlePageId) {
        return moodlePageRepository.findMoodlePageById(MoodlePageId);
    }

    public ArrayList<PublicTextChannel> getAllChannelsForCourse(long courseId) {
        return publicTextChannelRepository.findAllByCourseIdOrderByIdDesc(courseId);
    }

    public void deletePage(MoodlePage section) {
        moodlePageRepository.delete(section);
    }

    public String getUsernameOfSender(PublicChatMessage publicChatMessage) {
        return personRepository.findById(publicChatMessage.getSender()).getUsername();
    }

    public void updateTextChannel(PublicTextChannel courantChannel, String name, boolean mute, boolean visible) {
        courantChannel.setName(name);
        courantChannel.setMute(mute);
        courantChannel.setPrivateTeacher(visible);
        publicTextChannelRepository.save(courantChannel);
    }

    public ArrayList<Person> getAllUsers() {
        return personRepository.findAll();
    }

    public ArrayList<GroupMembers> findByUserId(long id) {
        return groupMembersRepository.findByUserId(id);
    }

    public void updateSection(MoodlePage section, String title, String content) {
        section.setTitle(title);
        section.setContent(content);
        moodlePageRepository.save(section);
    }

    public void clearMessageChat() {
        publicChatMessageRepository.updateDeletedAll();
    }

    public void clearMessageChat(int value, long channelid) {
        publicChatMessageRepository.updateDeleted(channelid, value);
    }

    public PublicChatMessage getMessageById(long id) {
        return publicChatMessageRepository.findById(id);
    }

    public ArrayList<Person> getAllStudentsForCourse(long courseId) {
        ArrayList<Group> groups = groupRepository.findAllByCourseId(courseId);
        ArrayList<GroupMembers> members = new ArrayList<>();
        groups.forEach(group -> members.addAll(groupMembersRepository.findByGroupId(group.getId())));
        Set<Person> students = new LinkedHashSet<>(); // Set doesn't allow duplicates
        members.forEach(m -> {
            Person p = personRepository.findById(m.getUserId());
            if (Person.Role.STUDENT.equals(p.getRole())) students.add(p);
        });
        return new ArrayList<>(students);
    }

    public ArrayList<Person> getAllUsersForCourse(long courseId) {
        ArrayList<Group> groups = groupRepository.findAllByCourseId(courseId);
        ArrayList<GroupMembers> members = new ArrayList<>();
        groups.forEach(group -> members.addAll(groupMembersRepository.findByGroupId(group.getId())));
        Set<Person> users = new LinkedHashSet<>(); // Set doesn't allow duplicates
        members.forEach(m -> users.add(personRepository.findById(m.getUserId())));
        return new ArrayList<>(users);
    }

    public ArrayList<Group> selectGroupeCourse(long courseId){
        return groupRepository.findGroupsByCourseId(courseId);
    }

    public void createChannel(String name, boolean mute, boolean prive, long courseId) {
        PublicTextChannel toSave = PublicTextChannel.builder().name(name).courseId(courseId).mute(mute).privateTeacher(prive).build();
        publicTextChannelRepository.save(toSave);
    }

    public void createMoodlePage(String title, long courseId) {
        MoodlePage toSave = MoodlePage.builder().courseId(courseId).title(title).content("").homePage(false).build();
        moodlePageRepository.save(toSave);
    }

    public String convertLongToDate(long dateLong) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat heure = new SimpleDateFormat("HH:mm");
        Date date = new Date(dateLong);
        if (formatter.format(date).equals(formatter.format(new Date(System.currentTimeMillis()))))
            return "Aujourd'hui à " + heure.format(date);
        return formatter.format(date) + " à " + heure.format(date);
    }

    /**
     * Returns the id of the moodle home page
     * @param courseId  the id of the course
     * @return the id of the homepage
     */
    public long findHomePageId(long courseId) {
        ArrayList<MoodlePage> pages = moodlePageRepository.findAllByCourseId(courseId);
        for (MoodlePage moodlePage : pages) {
            if (moodlePage.isHomePage()) {
                return moodlePage.getId();
            }
        }
        return 0; // no homepage for this course, should throw an error
    }

    public ArrayList<MoodlePage> getAllMoodlePagesForCourse(long courseId) {
        return moodlePageRepository.findAllByCourseId(courseId);
    }

    /**
     * Creates a server and the associated homepage
     * @param teacherId The id of the teacher
     * @param title     The name of the course
     * @param iconPath  The path to the course's picture
     */
    public Course createServer(long teacherId, String title, String iconPath) {
        Course toSave = Course.builder()
                .teacherId(teacherId)
                .name(title)
                .pathIcon(iconPath).build();
        courseRepository.save(toSave);
        MoodlePage moodlePage = MoodlePage.builder()
                .homePage(true)
                .content("")
                .title("Homepage")
                .courseId(toSave.getId()).build();
        moodlePageRepository.save(moodlePage);
        return toSave;
    }

    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    public List<MoodlePage> getAllMoodlePageForCourse(long courseID) {
        return moodlePageRepository.findAllByCourseId(courseID);
    }

    public void deleteUser(Person person) {
        groupMembersRepository.deleteByUserId(person.getId());
        privateChatMessageRepository.updateDeletedById(person.getId());
        publicChatMessageRepository.deletePublicChatMessageById(person.getId());
        personRepository.deleteUserById(person.getId());
    }

    public List<Group> findGroupAll() {
        return groupRepository.findAll();
    }

    public List<Person> findAllUserByRole(Person.Role r) {
        return personRepository.findAllByRole(r);
    }

    public List<Person> findAllUserByRoleifPresent(Person.Role r) {
        return personRepository.findAllByRole(r);
    }


    /**
     * @param users    the list of users to get the data from
     * @param getNames true to get the names (first+last), false to get the usernames
     * @return an ArrayList of names or usernames
     */
    public ArrayList<String> getAllNamesOrUsernames(ArrayList<Person> users, boolean getNames) {
        ArrayList<String> values = new ArrayList<>();
        if (getNames) {
            users.forEach(user -> values.add(user.getFirstName() + ", " + user.getLastName()));
        } else {
            users.forEach(user -> values.add(user.getUsername()));
        }
        return values;
    }
    public Course getLastCourse() {
        return courseRepository.findTopByOrderByIdDesc();
    }

    public void addPersonToCourse(Person p, Group group) {
        if (!existPersonIntoCourse(p, group)) {
            groupMembersRepository.save(
                    GroupMembers.builder()
                            .userId(p.getId())
                            .groupId(getLastGroup().getId())
                            .timeAdded(0)
                            .build());
        } else {
            System.out.println("L'utilisateur est déjà présent sur ce serveur");
        }
    }

    public boolean existPersonIntoCourse(Person p, Group group) {
        return (groupMembersRepository.findByUserIdAndGroupId(p.getId(), group.getId()) != null);
    }

    public Group getLastGroup() {
        return groupRepository.findTopByOrderByIdDesc();
    }

    public void createGroup(Course c, String name) {
        groupRepository.save(Group.builder().courseId(c.getId()).name(name).build());
    }

    public ArrayList<GroupMembers> getPersonByGroupId(long id) {
        return groupMembersRepository.findAllByGroupId(id);
    }

    public Person getPersonById(long id) {
        return personRepository.findById(id);
    }


    public void removeCourse(long course) {
        courseRepository.deleteById(course);
    }

    public void removeGroups(long course){
        groupRepository.deleteAllByCourseId(course);
    }

    public void removeGroupMembers(long id) {
        groupMembersRepository.deleteAllByGroupId(id);
    }

    public void removeChannels(long id) {
        publicTextChannelRepository.deleteAllByCourseId(id);
    }

    public void removeMoodlePage(long id) {
        moodlePageRepository.deleteAllByCourseId(id);
    }

    public void removePosts(long id) {
        publicChatMessageRepository.dropConstraint();
        publicChatMessageRepository.deleteById(id);
        publicChatMessageRepository.addConstraint();
    }

    public ArrayList<PublicChatMessage> listPosts(long id) {
        return publicChatMessageRepository.findAllByChannelidOrderByIdDesc(id);
    }

    public MoodlePage getHomePageCourse(long id, boolean bool){
        return moodlePageRepository.findByCourseIdAndHomePage(id, bool);
    }

    public void saveCourse(Course course) {
        this.courseRepository.save(course);
    }

    public void deleteUserInGroup(Person p) {
        groupMembersRepository.deleteByUserId(p.getId());
    }

    public Group getGroupByCourseId(long courseId) {
        return groupRepository.findGroupByCourseId(courseId);
    }

}
