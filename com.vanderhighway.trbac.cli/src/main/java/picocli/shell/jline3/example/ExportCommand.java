package picocli.shell.jline3.example;

import com.vanderhighway.trbac.aggregators.Scenario;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.Scenarios;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import picocli.CommandLine;
import picocli.shell.jline3.example.Exporter.Exporter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(sortOptions = false, name="export",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
        description = "Export data")
public class ExportCommand implements Runnable {

    @Override
    public void run() {

    }

    @CommandLine.Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void reachability(@CommandLine.Option(names = {"-user"}, required = true) String userName,
                             @CommandLine.Option(names = {"-scenario"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                     required = true) List<String> contextNames) throws ModelManipulationException, IOException {

        User user = (User) CLIContainer.getInstance().getElement(userName, User.class);
        Scenario scenario = null;
        if(contextNames != null) {
            Scenario matchScenario = new Scenario(new HashSet<>());
            for (String contextName: contextNames) {
                matchScenario.add((TemporalContext) CLIContainer.getInstance().getElement(contextName, TemporalContext.class));
            }
            if(CLIContainer.getInstance().getEngine().getMatcher(Scenarios.instance()).getAllMatches(matchScenario).stream().map(Scenarios.Match::getScenario).collect(Collectors.toSet())
                    .contains(matchScenario)) {
                scenario = matchScenario;
            }
            else {
                System.out.println("Given scenario: " + contextNames.toString() + " does not correspond to an  actual scenario!");
                return;
            }
        }
        Exporter.exportReachability(user, scenario, new File("./reachability.graphml"), CLIContainer.getInstance().getSystem());
    }

    @CommandLine.Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void topology() throws ModelManipulationException, IOException {
        Exporter.exportTopology( new File("./topology.graphml"), CLIContainer.getInstance().getSystem());
    }

    @CommandLine.Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void topologysmells() throws ModelManipulationException, IOException {
        Exporter.exportTopologySmells( new File("./topologysmells.graphml"), CLIContainer.getInstance().getSystem());
    }

    @CommandLine.Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void authorizations() throws ModelManipulationException, IOException {
        Exporter.exportAuthorizationPolicy( new File("./authorizations.graphml"), CLIContainer.getInstance().getModifier());
    }

    @CommandLine.Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void staticscenarios() throws ModelManipulationException, IOException {
        Exporter.exportStaticScenarios( new File("./staticscenarios.trbac"), CLIContainer.getInstance().getModifier());
    }

}
