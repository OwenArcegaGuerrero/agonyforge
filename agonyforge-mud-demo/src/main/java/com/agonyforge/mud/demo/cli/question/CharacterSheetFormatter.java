package com.agonyforge.mud.demo.cli.question;

import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.MudSpecies;
import com.agonyforge.mud.demo.model.repository.MudSpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.agonyforge.mud.demo.config.SpeciesLoader.DEFAULT_SPECIES_ID;

@Component
public final class CharacterSheetFormatter {
    private final MudSpeciesRepository speciesRepository;

    @Autowired
    public CharacterSheetFormatter(MudSpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    public void format(MudCharacter ch, Output output) {
        MudSpecies species = speciesRepository.getById(ch.getSpeciesId() != null ? ch.getSpeciesId() : DEFAULT_SPECIES_ID).orElseThrow();

        output.append("[dcyan]CHARACTER SHEET");
        output.append("[default]Name: [cyan]%s", ch.getName());
        output.append("[default]Pronouns: [cyan]%s/%s", ch.getPronoun().getSubject(), ch.getPronoun().getObject());
        output.append("[default]Species: [cyan]%s", species.getName());
        output.append("");
        output.append("[cyan]Stats   [magenta]Efforts");
        for (int i = 0; i < Math.max(Stat.values().length, Effort.values().length); i++) {
            Stat stat = Stat.values().length > i ? Stat.values()[i] : null;
            Effort effort = Effort.values().length > i ? Effort.values()[i] : null;

            String statString = stat != null ? String.format("[default]%s: [cyan]%d", stat.getAbbreviation(), ch.getStat(stat)) : "";
            String effortString = effort != null ? String.format("[default](d%-2d) %-15s: [magenta]%d", effort.getDie(), effort.getName(), ch.getEffort(effort)) : "";

            output.append("%15s\t%15s", statString, effortString);
        }

        output.append("");
        output.append("[default]Health: [red]❤");
        output.append("[default]DEF: [green]%d", ch.getDefense());
    }
}
