package app.web.views;

import app.jpa_repo.TextChannelRepository;
import app.model.chat.TextChannel;
import app.web.components.ComponentBuilder;
import app.web.components.ComponentButton;
import app.web.layout.CourseLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "channels", layout = CourseLayout.class)
public class TextChannelView extends VerticalLayout implements HasDynamicTitle, HasUrlParameter<Long> {
    String textUser[] = {
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce semper sed ipsum nec vulputate. Duis gravida velit nec quam consequat semper. Vestibulum lobortis eros in dictum iaculis. Etiam mi est, rhoncus elementum leo a, maximus placerat odio. Nulla varius, lorem eleifend faucibus consequat, mi nulla placerat leo, nec suscipit nisl tellus vitae risus. Mauris a suscipit risus. Donec condimentum enim eu tortor euismod, eu porttitor libero mattis. Integer sodales, turpis vitae mollis mollis, felis mauris semper sem, in aliquet eros nisl et ante. Fusce vulputate tortor elit, et condimentum tortor dignissim vitae. Vivamus fermentum ultricies leo, ut maximus sem efficitur at. Vestibulum ultrices lacinia blandit. Mauris neque dui, varius ac magna non, tempus euismod erat. Aliquam laoreet pharetra faucibus. Proin consequat rhoncus diam, nec euismod felis gravida in.\n" +
                    "\n",
            "Quisque volutpat arcu vitae mi fringilla mattis. Cras eu massa neque. Morbi non dictum tellus. Nullam orci enim, gravida at felis sed, porttitor condimentum risus. Nam ac odio dolor. Donec sagittis sem ac ullamcorper aliquet. Aliquam erat volutpat. Mauris eros mauris, posuere vitae aliquet eu, hendrerit quis ex. Integer quam lorem, suscipit vitae ultrices sed, imperdiet non tortor.\n" +
                    "\n",
            "Ut sit amet massa mattis, auctor felis consectetur, molestie enim. Suspendisse sed malesuada turpis. Sed venenatis augue vel dolor placerat dapibus. Cras aliquam non est quis accumsan. Proin sed tincidunt odio. Curabitur eu condimentum metus. Mauris in arcu ut massa dignissim facilisis. Ut fringilla turpis mollis faucibus semper. Suspendisse ut dapibus sapien.\n" +
                    "\n",
            "Etiam mauris ipsum, tempus non placerat vitae, dignissim sit amet nunc. Sed in orci leo. Quisque eu tortor mi. Vivamus neque nibh, commodo eget purus quis, finibus ultricies lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Mauris feugiat placerat lobortis. Nunc ornare lacus non enim rhoncus aliquam.\n" +
                    "\n"
    };

    private final TextChannelRepository textChannelRepository;
    private TextChannel textChannel;
    //TODO: créer un objet Button img path(false/true) + statut

    private final ComponentButton muteMicrophone;
    private final ComponentButton muteHeadphone;
    private final Button exitButton;

    //TODO: send message enter/button
    //TODO: sur la meme ligne
    //TODO: Change color loader

    public TextChannelView(@Autowired TextChannelRepository textChannelRepository) {
        this.textChannelRepository = textChannelRepository;
        muteMicrophone = new ComponentButton("img/micOn.svg", "img/micOff.svg", "unmute microphone", "mute microphone", Key.DIGIT_1);
        muteMicrophone.addClickListener(muteMicrophone::changeStatus);

        muteHeadphone = new ComponentButton("img/headsetOn.svg", "img/headsetOff.svg", "unmute headphone", "mute headphone", Key.DIGIT_2);
        muteHeadphone.addClickListener(muteHeadphone::changeStatus);

        exitButton = ComponentBuilder.createButtonText("Quitter", "#F04747");
        exitButton.addClickListener(event -> {
            exitButton.getStyle().set("display", "none");
            muteHeadphone.getStyle().set("display", "none");
            muteMicrophone.getStyle().set("display", "none");
        });
        //exitButton.addClickListener(event -> ); hide mic and head
    }

    @Override
    public String getPageTitle() {
        return textChannel.getName();
    }

    @SneakyThrows // so that javac doesn't complain about dirty exception throwing
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<TextChannel> c = textChannelRepository.findById(parameter);
        if (c.isPresent()) {
            textChannel = c.get();
        } else {
            throw new Exception("There is no channel with this ID.");
            // TODO : take care of the exception
        }
        makeLayout();
    }

    private void makeLayout() {
        HorizontalLayout layout = ComponentBuilder.createLayout(
                ComponentBuilder.createSideBar("20%", textChannel.getCourseId(), textChannelRepository),
                ComponentBuilder.createMessageCard(ComponentBuilder.ColorHTML.GREY, "60%", "cardCenter", textUser, muteMicrophone, muteHeadphone, exitButton),
                ComponentBuilder.createMembersCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%")
        );
        this.add(layout);
    }
}
