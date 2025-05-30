package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.cli.question.BaseQuestion;
import com.agonyforge.mud.demo.model.constant.WearSlot;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;

@Component
public class CharacterNameQuestion extends BaseQuestion {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterNameQuestion.class);

    private final RoleRepository roleRepository;

    @Autowired
    public CharacterNameQuestion(ApplicationContext applicationContext,
                                 RepositoryBundle repositoryBundle,
                                 RoleRepository roleRepository) {
        super(applicationContext, repositoryBundle);

        this.roleRepository = roleRepository;
    }

    @Override
    public Output prompt(WebSocketContext wsContext) {
        return new Output("[default]By what name do you wish to be known? ");
    }

    @Override
    public Response answer(WebSocketContext wsContext, Input input) {
        if (input.getInput().isBlank() || input.getInput().length() < 2) {
            return new Response(this, new Output("[red]Names need to be at least two letters in length."));
        } else if (input.getInput().length() > 12) {
            return new Response(this, new Output("[red]Names need to be 12 or fewer letters in length."));
        } else if (!input.getInput().matches("[A-Za-z]+")) {
            return new Response(this, new Output("[red]Names may only have letters in them."));
        } else if (!input.getInput().matches("[A-Z][A-Za-z]+")) {
            return new Response(this, new Output("[red]Names must begin with a capital letter."));
        }

        Optional<MudCharacter> existing = getRepositoryBundle().getCharacterRepository().findByCharacterName(input.getInput());

        if (existing.isPresent()) {
            return new Response(this, new Output("[red]Somebody else is already using that name. Please try a different one."));
        }

        MudCharacter ch = new MudCharacter();
        Set<Role> roles = new HashSet<>();
        Role playerRole = roleRepository.findByName("Player").orElseThrow();

        if (getRepositoryBundle().getCharacterRepository().countMudCharacterByPlayerIsNotNull() == 0) {
            Role implRole = roleRepository.findByName("Implementor").orElseThrow();
            roles.add(implRole);
        }

        roles.add(playerRole);

        ch.setPlayer(new PlayerComponent());
        ch.getPlayer().setTitle("the Stranger");
        ch.getPlayer().setUsername(wsContext.getPrincipal().getName());
        ch.getPlayer().setRoles(roles);

        ch.setCharacter(new CharacterComponent());
        ch.getCharacter().setName(input.getInput());
        ch.getCharacter().setPronoun(Pronoun.THEY);
        ch.getCharacter().setWearSlots(EnumSet.allOf(WearSlot.class));

        ch = getRepositoryBundle().getCharacterRepository().save(ch);
        wsContext.getAttributes().put(MUD_CHARACTER, ch.getId());

        LOGGER.info("New character created: {}", ch.getCharacter().getName());

        Question nextQuestion = getQuestion("characterPronounQuestion");

        return new Response(nextQuestion, new Output("[default]Hello, [white]%s[default]!", ch.getCharacter().getName()));
    }
}
