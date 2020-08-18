package picocli.shell.jline3.example;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.aggregators.Scenario;
import com.vanderhighway.trbac.core.CoreUtils;
import com.vanderhighway.trbac.core.modifier.PolicyAutomaticModifier;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.core.validator.PolicyValidator;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.AccessRelation;
import com.vanderhighway.trbac.patterns.DateScheduleTimeRange_To_Scenario;
import com.vanderhighway.trbac.patterns.Scenarios;
import org.apache.log4j.Level;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngineOptions;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.localsearch.matcher.integration.LocalSearchEMFBackendFactory;
import org.eclipse.viatra.query.runtime.rete.matcher.DRedReteBackendFactory;
import org.eclipse.viatra.query.runtime.util.ViatraQueryLoggingUtil;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.Builtins;
import org.jline.builtins.Widgets.TailTipWidgets;
import org.jline.builtins.Widgets.TailTipWidgets.TipType;
import org.jline.keymap.KeyMap;
import org.jline.builtins.SystemRegistry;
import org.jline.builtins.SystemRegistryImpl;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline3.PicocliCommands;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * @since 4.4.0
 */
public class PolicyValidatorCLI {

    /**
     * Top-level command that just prints help.
     */
    @Command(name = "",
            description = {
                    "Example interactive shell with completion and autosuggestions. " +
                            "Hit @|magenta <TAB>|@ to see available commands.",
                    "Hit @|magenta ALT-S|@ to toggle tailtips.",
                    ""},
            footer = {"", "Press Ctl-D to exit."},
            subcommands = {MyCommand.class, ClearScreen.class, CommandLine.HelpCommand.class})
    static class CliCommands implements Runnable {
        LineReaderImpl reader;
        PrintWriter out;

        CliCommands() {}

        public void setReader(LineReader reader){
            this.reader = (LineReaderImpl)reader;
            out = reader.getTerminal().writer();
        }

        public void run() {
            System.out.println(new CommandLine(this).getUsageMessage());
        }
    }

    /**
     * A command with some options to demonstrate completion.
     */
    @Command(name = "cmd", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Command with some options to demonstrate TAB-completion.",
                    " (Note that enum values also get completed.)"},
            subcommands = {CommandLine.HelpCommand.class})
    static class MyCommand implements Runnable {
        @Option(names = {"-v", "--verbose"},
                description = { "Specify multiple -v options to increase verbosity.",
                        "For example, `-v -v -v` or `-vvv`"})
        private boolean[] verbosity = {};

        @ArgGroup(exclusive = false)
        private MyDuration myDuration = new MyDuration();

        static class MyDuration {
            @Option(names = {"-d", "--duration"},
                    description = "The duration quantity.",
                    required = true)
            private int amount;

            @Option(names = {"-u", "--timeUnit"},
                    description = "The duration time unit.",
                    required = true)
            private TimeUnit unit;
        }

        @ParentCommand CliCommands parent;

        public void run() {
            if (verbosity.length > 0) {
                parent.out.printf("Hi there. You asked for %d %s.%n",
                        myDuration.amount, myDuration.unit);
            } else {
                parent.out.println("hi!");
            }
        }
    }

//    @Command(name = "nested", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
//            description = "Hosts more sub-subcommands")
//    static class Nested implements Runnable {
//        public void run() {
//            System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
//        }
//
//        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
//                description = "Multiplies two numbers.")
//        public void multiply(@Option(names = {"-l", "--left"}, required = true) int left,
//                             @Option(names = {"-r", "--right"}, required = true) int right) {
//            System.out.printf("%d * %d = %d%n", left, right, left * right);
//        }
//
//        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
//                description = "Adds two numbers.")
//        public void add(@Option(names = {"-l", "--left"}, required = true) int left,
//                        @Option(names = {"-r", "--right"}, required = true) int right) {
//            System.out.printf("%d + %d = %d%n", left, right, left + right);
//        }
//
//        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
//                description = "Subtracts two numbers.")
//        public void subtract(@Option(names = {"-l", "--left"}, required = true) int left,
//                             @Option(names = {"-r", "--right"}, required = true) int right) {
//            System.out.printf("%d - %d = %d%n", left, right, left - right);
//        }
//    }

    /**
     * Command that clears the screen.
     */
    @Command(name = "cls", aliases = "clear", mixinStandardHelpOptions = true,
            description = "Clears the screen", version = "1.0")
    static class ClearScreen implements Callable<Void> {

        @ParentCommand CliCommands parent;

        public Void call() throws IOException {
            parent.reader.clearScreen();
            return null;
        }
    }

    private static Path workDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        // Dirty Hack to turn off warnings!
        // see https://stackoverflow.com/questions/46454995/how-to-hide-warning-illegal-reflective-access-in-java-9-without-jvm-argument
        System.err.close();
        System.setErr(System.out);

        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.BasicConfigurator.resetConfiguration();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);

        try {
            // set up JLine built-in commands
            Builtins builtins = new Builtins(PolicyValidatorCLI::workDir, null, null);
            builtins.rename(org.jline.builtins.Builtins.Command.TTOP, "top");
            builtins.alias("zle", "widget");
            builtins.alias("bindkey", "keymap");
            // set up picocli commands
            CliCommands commands = new CliCommands();
            EntityCommands entityCommands = new EntityCommands();
            CommandLine cmd = new CommandLine(commands);
            CommandLine cmd2 = new CommandLine(entityCommands);
            PicocliCommands picocliCommands = new PicocliCommands(PolicyValidatorCLI::workDir, cmd);
            ModelCommands myCommands = new ModelCommands(PolicyValidatorCLI::workDir, cmd2);

            Parser parser = new DefaultParser();
            Terminal terminal = TerminalBuilder.builder().build();

            SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, PolicyValidatorCLI::workDir, null);
            systemRegistry.setCommandRegistries(builtins, picocliCommands, myCommands);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(systemRegistry.completer())
                    .parser(parser)
                    .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                    .build();
            builtins.setLineReader(reader);
            commands.setReader(reader);
            new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TipType.COMPLETER);
            KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
            keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"));

            String prompt = "> ";
            String rightPrompt = null;

            // Print Header Message
            System.out.println("  ____       _ _             ____ _               _             \n" +
                    " |  _ \\ ___ | (_) ___ _   _ / ___| |__   ___  ___| | _____ _ __ \n" +
                    " | |_) / _ \\| | |/ __| | | | |   | '_ \\ / _ \\/ __| |/ / _ \\ '__|\n" +
                    " |  __/ (_) | | | (__| |_| | |___| | | |  __/ (__|   <  __/ |   \n" +
                    " |_|   \\___/|_|_|\\___|\\__, |\\____|_| |_|\\___|\\___|_|\\_\\___|_|   \n" +
                    "                      |___/                                     ");
            System.out.println("version 0.0.1");

            //URI uri = URI.createFileURI("models/basic/intervals.trbac");
            //URI uri = URI.createFileURI("empty_policy_all_schedules.trbac");
            //URI uri = URI.createFileURI("empty_policy_trebla.trbac");
            URI uri = URI.createFileURI("simple_company.trbac");

            Spinner fileLoadSpinner = new Spinner("Loading Policy Model... (" + uri.toString() + ") ");
            new Thread(fileLoadSpinner).start();

            // Initializing the EMF package
            TRBACPackage.eINSTANCE.getName();
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
            Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

            ResourceSet set = new ResourceSetImpl();
            Resource resource = set.getResource(uri, true);

            fileLoadSpinner.stop();

            Spinner xSpinner = new Spinner("Adding Missing Schedules and Temporal Context Instances... ");
            new Thread(xSpinner).start();
            CoreUtils coteUtils = new CoreUtils();
            coteUtils.addMissingDaySchedules(resource, (SecurityPolicy) resource.getContents().get(0), "2020-01-01", "2030-01-01");
            xSpinner.stop();

            Spinner queryEngineSpinner = new Spinner("Initializing Query Engine... ");
            new Thread(queryEngineSpinner).start();

            ViatraQueryEngineOptions options = ViatraQueryEngineOptions.defineOptions().withDefaultBackend(DRedReteBackendFactory.INSTANCE)
                    .withDefaultCachingBackend(DRedReteBackendFactory.INSTANCE)
                    .withDefaultSearchBackend(LocalSearchEMFBackendFactory.INSTANCE)
                    .build();
            final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set), options);
            ViatraQueryLoggingUtil.getDefaultLogger().setLevel(Level.OFF);
            CLIContainer.getInstance().setEngine(engine);
            CLIContainer.getInstance().setModel(resource);

            queryEngineSpinner.stop();

            Spinner modelModifiersSpinner = new Spinner("Initializing Policy Model Modifiers... ");
            new Thread(modelModifiersSpinner).start();

            PolicyModifier modifier = new PolicyModifier(engine, (SecurityPolicy) resource.getContents().get(0), resource);
            modifier.setInstanceIDCounter(coteUtils.getInstanceIDCounter());
            CLIContainer.getInstance().setModifier(modifier);
            PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (SecurityPolicy) resource.getContents().get(0));
            CLIContainer.getInstance().setAutomaticModifier(automaticModifier);

            automaticModifier.initialize();
            automaticModifier.execute();

            modelModifiersSpinner.stop();

            //System.out.println("Initializing Policy Validator... ");
            Spinner checkerSpinner = new Spinner("Initializing Policy Validator... ");
            new Thread(checkerSpinner).start();

            PolicyValidator validator = new PolicyValidator(engine);
            validator.addChangeListeners(engine, false);
            CLIContainer.getInstance().setValidator(validator);

            checkerSpinner.stop();
            System.out.printf("\u0008");

            System.out.println("Policy Validator fully initialized! Please enter a command. ");

            //System.out.printf("\u0008");
            //LocalDateTime now4 =  LocalDateTime.now();
            //System.out.println(now3.until(now4, ChronoUnit.SECONDS));

            // start the shell and process input until the user quits with Ctrl-D
            String line;
            while (true) {
                try {
                    systemRegistry.cleanUp();
                    line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    systemRegistry.execute(line);
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                } catch (Exception e) {
                    systemRegistry.trace(e);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Command(name = "name of entity commands",
            description = {
                    "description of entity commands"},
            footer = {"", "Press Ctl-D to exit."},
            subcommands = {
                    AddCommand.class, RemoveCommand.class, AssignCommand.class, DeassignCommand.class, ShowCommand.class})
    static class EntityCommands implements Runnable {
        LineReaderImpl reader;
        PrintWriter out;

        EntityCommands() {}

        public void setReader(LineReader reader){
            this.reader = (LineReaderImpl)reader;
            out = reader.getTerminal().writer();
        }

        public void run() {
            System.out.println(new CommandLine(this).getUsageMessage());
        }
    }

    @Command(name = "add", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class, AddCommand.Constraint.class},
            description = "add a model entity")
    static class AddCommand implements Runnable {

        public void run() {
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a user")
        public void user(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addUser(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a role")
        public void role(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addRole(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a demarcation")
        public void demarcation(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addDemarcation(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a permission")
        public void permission(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addPermission(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a building")
        public void building(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addBuilding(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a security zone")
        public void securityzone(@Option(names = {"-building"}, required = true) String buildingName,
                                 @Option(names = {"-name"}, required = true) String name,
                                 @Option(names = {"-public"}, required = false) boolean isPublic) throws ModelManipulationException {
            Building building = (Building) CLIContainer.getInstance().getModel().getEObject(buildingName);
            CLIContainer.getInstance().getModifier().addSecurityZone(building, name, isPublic);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal context")
        public void temporalcontext(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            CLIContainer.getInstance().getModifier().addTemporalContext(name);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal context instance")
        public void temporalcontextinstance(@Option(names = {"-context"}, required = true) String temporalContextName,
                                             @Option(names = {"-day"}, required = true) String dayScheduleName,
                                             @Option(names = {"-start"}, required = true) String startTime,
                                             @Option(names = {"-end"}, required = true) String endTime
                                             ) throws ModelManipulationException, ParseException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getModel().getEObject(temporalContextName);
            if(context == null) {System.out.println("Unkown temporal context: " + context); return;}

            if(dayScheduleName.split("_").length == 3) {
                dayScheduleName = addDayToDateString(dayScheduleName);
            }
            DaySchedule daySchedule = (DaySchedule) CLIContainer.getInstance().getModel().getEObject(dayScheduleName);

            CLIContainer.getInstance().getModifier().addTemporalContextInstance(context, daySchedule, new IntegerInterval(toMinutes(startTime), toMinutes(endTime)));
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal grant rule")
        public void temporalgrantrule(
                @Option(names = {"-name"}, required = true) String ruleName,
                @Option(names = {"-context"}, required = true) String temporalContextName,
                @Option(names = {"-role"}, required = true) String roleName,
                @Option(names = {"-demarcation"}, required = true) String demarcationName,
                @Option(names = {"-command"}, required = true) String commandName,
                @Option(names = {"-priority"}, required = true) int priority
        ) throws ModelManipulationException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getModel().getEObject(temporalContextName);
            Role role = (Role) CLIContainer.getInstance().getModel().getEObject(roleName);
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcationName);
            if(commandName.toLowerCase().equals("grant")) {
                CLIContainer.getInstance().getModifier().addTemporalGrantRule(context, ruleName, role, demarcation, true, priority);
            } else if(commandName.toLowerCase().equals("revoke")) {
                CLIContainer.getInstance().getModifier().addTemporalGrantRule(context, ruleName, role, demarcation, false, priority);
            } else {
                System.out.println("Command should either be \"grant\" or \"revoke\", not" + commandName);
            }
        }

        @Command(name="constraint", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add an authorization constraint")
        public static class Constraint implements Runnable {

            @Override
            public void run() {

            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Users / Roles")
            public void SoDUR(@Option(names = {"-name"}, required = true) String name,
                                @Option(names = {"-role1"}, required = true) String role1Name,
                                @Option(names = {"-role2"}, required = true) String role2Name) throws ModelManipulationException {
                Role role1 = (Role) CLIContainer.getInstance().getModel().getEObject(role1Name);
                Role role2 = (Role) CLIContainer.getInstance().getModel().getEObject(role2Name);
                CLIContainer.getInstance().getModifier().addSoDURConstraint(name, role1, role2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Users / Demarcations ")
            public void SoDUD(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-dem1"}, required = true) String demarcation1Name,
                              @Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
                Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
                Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
                CLIContainer.getInstance().getModifier().addSoDUDConstraint(name, dem1, dem2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Users / Permissions ")
            public void SoDUP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addSoDUPConstraint(name, perm1, perm2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Roles / Demarcations")
            public void SoDRD(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-dem1"}, required = true) String demarcation1Name,
                              @Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
                Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
                Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
                CLIContainer.getInstance().getModifier().addSoDRDConstraint(name, dem1, dem2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Roles / Permissions")
            public void SoDRP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addSoDRPConstraint(name, perm1, perm2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a Separation of Duty on the level of Demarcations / Permissions")
            public void SoDDP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addSoDDPConstraint(name, perm1, perm2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Users / Roles")
            public void PreReqUR(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-role1"}, required = true) String role1Name,
                              @Option(names = {"-role2"}, required = true) String role2Name) throws ModelManipulationException {
                Role role1 = (Role) CLIContainer.getInstance().getModel().getEObject(role1Name);
                Role role2 = (Role) CLIContainer.getInstance().getModel().getEObject(role2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteURConstraint(name, role1, role2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Users / Demarcations ")
            public void PreReqUD(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-dem1"}, required = true) String demarcation1Name,
                              @Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
                Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
                Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteUDConstraint(name, dem1, dem2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Users / Permissions ")
            public void PreReqUP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteUPConstraint(name, perm1, perm2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Roles / Demarcations")
            public void PreReqRD(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-dem1"}, required = true) String demarcation1Name,
                              @Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
                Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
                Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteRDConstraint(name, dem1, dem2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Roles / Permissions")
            public void PreReqRP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteRPConstraint(name, perm1, perm2);
            }

            @Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                    description = "Add a prerequisite on the level of Demarcations / Permissions")
            public void PreReqDP(@Option(names = {"-name"}, required = true) String name,
                              @Option(names = {"-perm1"}, required = true) String permission1Name,
                              @Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
                Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
                Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
                CLIContainer.getInstance().getModifier().addPrerequisiteDPConstraint(name, perm1, perm2);
            }
        }
    }

    @Command(name = "remove", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "remove a model entity")
    static class RemoveCommand implements Runnable {

        public void run() {
            System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a user")
        public void user(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            User user = (User) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeUser(user);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a role")
        public void role(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            Role role = (Role) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeRole(role);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a demarcation")
        public void demarcation(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeDemarcation(demarcation);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a permission")
        public void permission(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removePermission(permission);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a building")
        public void building(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            Building building = (Building) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeBuilding(building);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a security zone")
        public void securityzone(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeSecurityZone(securityZone);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal context")
        public void temporalcontext(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeTemporalContext(context);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal context instance")
        public void temporalcontextinstance(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            TimeRange timeRange = (TimeRange) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeTemporalContextInstance(timeRange);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal grant rule")
        public void temporalgrantrule(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            TemporalGrantRule rule = (TemporalGrantRule) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeTemporalGrantRule(rule);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal grant rule")
        public void constraint(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException {
            AuthorizationConstraint constraint = (AuthorizationConstraint) CLIContainer.getInstance().getModel().getEObject(name);
            CLIContainer.getInstance().getModifier().removeAuthorizationConstraint(constraint);
        }
    }

    @Command(name = "assign", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "assign entities")
    static class AssignCommand implements Runnable {

        public void run() {
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a role to a user")
        public void UR(@Option(names = {"-user"}, required = true) String userName,
                       @Option(names = {"-role"}, required = true) String roleName) throws ModelManipulationException {
            User user = (User) CLIContainer.getInstance().getModel().getEObject(userName);
            Role role = (Role) CLIContainer.getInstance().getModel().getEObject(roleName);
            CLIContainer.getInstance().getModifier().assignRoleToUser(user, role);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a permission to a demarcation")
        public void DP(@Option(names = {"-demarcation"}, required = true) String demarcationName,
                       @Option(names = {"-permission"}, required = true) String permissionName) throws ModelManipulationException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcationName);
            Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
            CLIContainer.getInstance().getModifier().assignPermissionToDemarcation(demarcation, permission);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign an object to a permission")
        public void PO(@Option(names = {"-permission"}, required = true) String permissionName,
                       @Option(names = {"-object"}, required = true) String objectName) throws ModelManipulationException {
            Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(objectName);
            CLIContainer.getInstance().getModifier().assignObjectToPermission(permission, securityZone);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set reachability between two zones")
        public void reachability(@Option(names = {"-from"}, required = true) String fromZoneName,
                                 @Option(names = {"-to"}, required = true) String toZoneName,
                                 @Option(names = {"-bidirectional"}, required = false) boolean isBirectional) throws ModelManipulationException {
            SecurityZone fromSecurityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(fromZoneName);
            SecurityZone toSecurityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(toZoneName);
            if (isBirectional) {
                CLIContainer.getInstance().getModifier().setBidirectionalReachability(fromSecurityZone, toSecurityZone);
            } else {
                CLIContainer.getInstance().getModifier().setReachability(fromSecurityZone, toSecurityZone);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two roles")
        public void role_inheritance(@Option(names = {"-junior"}, required = true) String juniorRoleName,
                                     @Option(names = {"-senior"}, required = true) String seniorRoleName) throws ModelManipulationException {
            Role juniorRole = (Role) CLIContainer.getInstance().getModel().getEObject(juniorRoleName);
            Role seniorRole = (Role) CLIContainer.getInstance().getModel().getEObject(seniorRoleName);
            CLIContainer.getInstance().getModifier().addRoleInheritance(juniorRole, seniorRole);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two demarcations")
        public void demarcation_inheritance(@Option(names = {"-sub"}, required = true) String subDemarcationName,
                                     @Option(names = {"-sup"}, required = true) String supDemarcationName) throws ModelManipulationException {
            Demarcation subDemarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(subDemarcationName);
            Demarcation supDemarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(supDemarcationName);
            CLIContainer.getInstance().getModifier().addDemarcationInheritance(subDemarcation, supDemarcation);
        }
    }

    @Command(name = "deassign", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "deassign entities")
    static class DeassignCommand implements Runnable {

        public void run() { }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a role to a user")
        public void UR(@Option(names = {"-user"}, required = true) String userName,
                       @Option(names = {"-role"}, required = true) String roleName) throws ModelManipulationException {
            User user = (User) CLIContainer.getInstance().getModel().getEObject(userName);
            Role role = (Role) CLIContainer.getInstance().getModel().getEObject(roleName);
            CLIContainer.getInstance().getModifier().deassignRoleFromUser(user, role);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a permission to a demarcation")
        public void DP(@Option(names = {"-demarcation"}, required = true) String demarcationName,
                       @Option(names = {"-permission"}, required = true) String permissionName) throws ModelManipulationException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcationName);
            Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
            CLIContainer.getInstance().getModifier().deassignPermissionFromDemarcation(demarcation, permission);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign an object to a permission")
        public void PO(@Option(names = {"-permission"}, required = true) String permissionName,
                       @Option(names = {"-object"}, required = true) String objectName) throws ModelManipulationException {
            Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(objectName);
            CLIContainer.getInstance().getModifier().deassignObjectFromPermission(permission, securityZone);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove reachability between two zones")
        public void reachability(@Option(names = {"-from"}, required = true) String fromZoneName,
                                 @Option(names = {"-to"}, required = true) String toZoneName,
                                 @Option(names = {"-bidirectional"}, required = false) boolean isBirectional) throws ModelManipulationException {
            SecurityZone fromSecurityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(fromZoneName);
            SecurityZone toSecurityZone = (SecurityZone) CLIContainer.getInstance().getModel().getEObject(toZoneName);
            if(isBirectional) {
                CLIContainer.getInstance().getModifier().removeBidirectionalReachability(fromSecurityZone, toSecurityZone);
            } else {
                CLIContainer.getInstance().getModifier().removeReachability(fromSecurityZone, toSecurityZone);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two roles")
        public void role_inheritance(@Option(names = {"-junior"}, required = true) String juniorRoleName,
                                     @Option(names = {"-senior"}, required = true) String seniorRoleName) throws ModelManipulationException {
            Role juniorRole = (Role) CLIContainer.getInstance().getModel().getEObject(juniorRoleName);
            Role seniorRole = (Role) CLIContainer.getInstance().getModel().getEObject(seniorRoleName);
            CLIContainer.getInstance().getModifier().removeRoleInheritance(juniorRole, seniorRole);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two demarcations")
        public void demarcation_inheritance(@Option(names = {"-sub"}, required = true) String subDemarcationName,
                                            @Option(names = {"-sup"}, required = true) String supDemarcationName) throws ModelManipulationException {
            Demarcation subDemarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(subDemarcationName);
            Demarcation supDemarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(supDemarcationName);
            CLIContainer.getInstance().getModifier().removeDemarcationInheritance(subDemarcation, supDemarcation);
        }
    }

    @Command(name = "show", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "show stuff")
    static class ShowCommand implements Runnable {

        public void run() {
            //System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all basic entities and relations (Users, Roles, Demarcations, Permissions, UR, DP)")
        public void basic() throws ModelManipulationException {
            users();
            roles();
            UR();
            demarcations();
            permissions();
            DP();
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the computed access relation")
        public void access() throws ModelManipulationException {
            Set<AccessRelation.Match> matches = CLIContainer.getInstance().getEngine().getMatcher(AccessRelation.instance()).getAllMatches().stream().collect(Collectors.toSet());
            Map<String, Map<String, Set<String>>> accessRelation = new HashMap<>();
            for (AccessRelation.Match match: matches){
                accessRelation.putIfAbsent(match.getUser().getName(), new HashMap<>());
                List<String> groupNamesList = match.getScenario().stream().map(x -> x.getName()).collect(Collectors.toList());
                Collections.sort(groupNamesList);
                String groupNames = groupNamesList.toString();
                accessRelation.get(match.getUser().getName()).putIfAbsent(groupNames, new HashSet<>());
                accessRelation.get(match.getUser().getName()).get(groupNames).add(match.getPermission().getName());
            }
            List<String> sortedUserNames = accessRelation.keySet().stream().sorted().collect(Collectors.toList());
            for (String userName: sortedUserNames) {
                System.out.println(userName);
                List<String> sortedGroupNames = accessRelation.get(userName).keySet().stream().sorted().collect(Collectors.toList());
                for (String groupName : sortedGroupNames) {
                    List<String> sortedPermissionNames = accessRelation.get(userName).get(groupName).stream().sorted().collect(Collectors.toList());
                    System.out.println("\t" + groupName + " -> " + sortedPermissionNames.toString());
                }
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all users")
        public void users() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> users = policy.getAuthorizationPolicy().getUsers().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(users);
            System.out.println("Users:");
            for(String user: users) {
                System.out.println("\t" + user);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show UR")
        public void UR() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Map<String, Set<String>> UR = new HashMap<>();
            for (User user: policy.getAuthorizationPolicy().getUsers()) {
                UR.put(user.getName(), new HashSet<>());
                for(Role role: user.getUR()) {
                    UR.get(user.getName()).add(role.getName());
                }
            }
            List<String> sortedUserNames = UR.keySet().stream().sorted().collect(Collectors.toList());
            System.out.println("UR:");
            for(String userName: sortedUserNames) {
                System.out.println("\t" + userName + "->" + UR.get(userName).stream().sorted().collect(Collectors.toList()));
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all roles")
        public void roles() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> roles = policy.getAuthorizationPolicy().getRoles().stream().map(PolicyValidatorCLI::rolePrettyString).
                    sorted().collect(Collectors.toList());
            System.out.println("Roles:");
            for (String role: roles) {
                System.out.println("\t" + role);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show DP")
        public void DP() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Map<String, Set<String>> DP = new HashMap<>();
            for (Demarcation demarcation: policy.getAuthorizationPolicy().getDemarcations()) {
                DP.put(demarcation.getName(), new HashSet<>());
                for(Permission permission: demarcation.getDP()) {
                    DP.get(demarcation.getName()).add(permission.getName());
                }
            }
            List<String> sortedDemarcationNames = DP.keySet().stream().sorted().collect(Collectors.toList());
            System.out.println("DP:");
            for(String demarcationName: sortedDemarcationNames) {
                System.out.println("\t" + demarcationName + "->" + DP.get(demarcationName).stream().sorted().collect(Collectors.toList()));
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all demarcations")
        public void demarcations() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> demarcations = policy.getAuthorizationPolicy().getDemarcations().stream().map(PolicyValidatorCLI::demarcationPrettyString)
                    .collect(Collectors.toList());
            Collections.sort(demarcations);
            System.out.println("Demarcations:");
            for(String demarcationName: demarcations) {
                System.out.println("\t" + demarcationName);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all permissions")
        public void permissions() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> permissions = policy.getAuthorizationPolicy().getPermissions().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(permissions);
            System.out.println("Permissions:");
            for(String permission: permissions) {
                System.out.println("\t" + permission);
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all buildings")
        public void buildings() throws ModelManipulationException {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> buildings = policy.getBuildings().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(buildings);
            System.out.println(buildings);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all security zones")
        public void securityzones() {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Map<String, Map<String, List<String>>> buildings = new HashMap<>();
            for (Building building: policy.getBuildings()) {
                buildings.put(building.getName(), new HashMap<>());
                for(SecurityZone zone: building.getSecurityzones()) {
                    buildings.get(building.getName()).put(zone.getName(), zone.getReachable().stream().map(x -> x.getName()).sorted().collect(Collectors.toList()));
                }
            }
            List<String> sortedBuildingNames = buildings.keySet().stream().sorted().collect(Collectors.toList());
            for(String buildingName: sortedBuildingNames) {
                System.out.println(buildingName + ":");
                List<String> sortedSecurityZoneNames = buildings.get(buildingName).keySet().stream().sorted().collect(Collectors.toList());
                for (String securityZoneName: sortedSecurityZoneNames) {
                    System.out.println("\t" + securityZoneName + " -> " + buildings.get(buildingName).get(securityZoneName));
                }
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all computed scenarios")
        public void scenarios( @Option(names = {"-example"}, description = "Show an example instance.", required = false) boolean example,
                @Option(names = {"-date"}, description = "Show all scenarios for a specific date", required = false) String dateString) throws ParseException {
            if(dateString == null) {
                Set<Scenarios.Match> matches = CLIContainer.getInstance().getEngine().getMatcher(Scenarios.instance()).getAllMatches().stream().collect(Collectors.toSet());
                List<String> groups = new LinkedList<>();
                for (Scenarios.Match match : matches) {
                    String scenarioString = scenarioPrettyString(match.getScenario());
                    // Please forgive me, for I have written horrible code.
                    if (example) {
                        scenarioString += " (ex: " + dateScheduleTimeRange_To_TimeRangeGroupCombinationMatchPrettyString(
                                getExampleDateScheduleTimeRange(match.getScenario())
                        ) + ")";
                    }
                    groups.add(scenarioString);
                }

                Collections.sort(groups);
                for (String group : groups) {
                    System.out.println(group);
                }
            } else {
                if (dateString.split("_").length == 3) {
                    dateString = addDayToDateString(dateString);
                }
                DayOfYearSchedule schedule = (DayOfYearSchedule) CLIContainer.getInstance().getModel().getEObject(dateString);
                DateScheduleTimeRange_To_Scenario.Match partialMatch = DateScheduleTimeRange_To_Scenario.Matcher.create().newMatch(schedule, null, null, null);
                DateScheduleTimeRange_To_Scenario.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(DateScheduleTimeRange_To_Scenario.instance());
                List<DateScheduleTimeRange_To_Scenario.Match> matches = matcher.getAllMatches(partialMatch).stream().sorted((m1, m2) -> m1.getStarttime().compareTo(m2.getStarttime())).collect(Collectors.toList());
                for (DateScheduleTimeRange_To_Scenario.Match match : matches) {
                    System.out.println( "[" + fromMinutesToHHmm(match.getStarttime()) + "-" + fromMinutesToHHmm(match.getEndtime()) + "]"
                            + " " + scenarioPrettyString(match.getScenario()));
                }
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all temporal contexts")
        public void temporalcontexts() {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = policy.getAuthorizationPolicy().getSchedule();
            List<TemporalContext> temporalContexts = schedule.getTemporalContexts().stream().sorted(
                    Comparator.comparing(TemporalContext::getName)
            ).collect(Collectors.toList());
            System.out.println(temporalContexts);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show n temporal context instances (default=5)")
        public void temporalcontextinstances(@Option(names = {"-n"}, required = false) int instanceCount) {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = policy.getAuthorizationPolicy().getSchedule();
            List<TemporalContext> temporalContexts = schedule.getTemporalContexts().stream().sorted(
                    Comparator.comparing(TemporalContext::getName)
            ).collect(Collectors.toList());

            if(instanceCount == 0) {
                instanceCount = 5;
            }
            for(TemporalContext context: temporalContexts) {
                System.out.println(context.getName() + "->" +
                    context.getInstances().stream().limit(instanceCount)
                            .map(PolicyValidatorCLI::timeRangePrettyString).collect(Collectors.toList())
                );
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all temporal grant rules")
        public void temporalgrantrules() {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = policy.getAuthorizationPolicy().getSchedule();
            List<TemporalGrantRule> temporalGrantRules = schedule.getTemporalGrantRules().stream().sorted(
                    Comparator.comparing(TemporalGrantRule::getName)
            ).collect(Collectors.toList());
            for (TemporalGrantRule rule: temporalGrantRules) {
                String command = rule.isEnable() ? "grant" : "revoke";
                System.out.println( rule.getName() + " : " + command + " " + rule.getRole().getName() + " access to "
                        + rule.getDemarcation().getName() + " during " + rule.getTemporalContext().getName() + " with priority " + rule.getPriority());
            }
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all constraints")
        public void constraints() {
            SecurityPolicy policy = (SecurityPolicy) CLIContainer.getInstance().getModel().getContents().get(0);
            List<AuthorizationConstraint> authorizationConstraints = policy.getAuthorizationConstraints().stream().sorted(
                    Comparator.comparing(AuthorizationConstraint::getName)
            ).collect(Collectors.toList());
            for (AuthorizationConstraint constraint: authorizationConstraints) {
                System.out.println(constraint.getName() + ": " + ConstraintHelper.toString(constraint));
            }
        }
    }


    public static String rolePrettyString(Role role){
        String prettyString = role.getName();
        if(role.getJuniors().size() > 0) {
            List<String> juniors = role.getJuniors().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(juniors);
            prettyString += " (inherits " + juniors + ")";
        }
        return prettyString;
    }

    public static String demarcationPrettyString(Demarcation demarcation){
        String prettyString = demarcation.getName();
        if(demarcation.getSubdemarcations().size() > 0) {
            List<String> juniors = demarcation.getSubdemarcations().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(juniors);
            prettyString += " (subs: " + juniors + ")";
        }
        return prettyString;
    }

    public static String scenarioPrettyString(Scenario scenario) {
        List<String> contextList = scenario.stream().map(x -> x.getName()).collect(Collectors.toList());
        Collections.sort(contextList);
        String prettyString = contextList.toString();
        return prettyString;
    }

    public static String fromMinutesToHHmm(int minutes) {
        long hours = TimeUnit.MINUTES.toHours(Long.valueOf(minutes));
        long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%02d:%02d", hours, remainMinutes);
    }

    public static DateScheduleTimeRange_To_Scenario.Match getExampleDateScheduleTimeRange(Scenario set) {
        DateScheduleTimeRange_To_Scenario.Match partialMatch = DateScheduleTimeRange_To_Scenario.Matcher.create().newMatch(null, null, null, set);
        DateScheduleTimeRange_To_Scenario.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(
                DateScheduleTimeRange_To_Scenario.instance());
            Optional<DateScheduleTimeRange_To_Scenario.Match>
                    match = matcher.getOneArbitraryMatch(partialMatch);
        return match.<DateScheduleTimeRange_To_Scenario.Match>map(value -> match.get()).orElse(null);
    }

    public static String addDayToDateString(String dateString) throws ParseException {
        Calendar cal = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("d_MMMM_yyyy", Locale.ENGLISH);
        cal.setTime(format.parse(dateString));
        int day = cal.get(Calendar.DAY_OF_WEEK);
        String newDateString = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").get(day - 1);
        newDateString = newDateString + "_" + dateString;
        return newDateString;
    }
    public static int toMinutes(String time) {
        return (int) ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, LocalTime.parse(time));
    }

    public static String dateScheduleTimeRange_To_TimeRangeGroupCombinationMatchPrettyString(DateScheduleTimeRange_To_Scenario.Match match) {
        return match.getYearDaySchedule().getName() + " " +
                fromMinutesToHHmm(match.getStarttime()) + "-" +
                fromMinutesToHHmm(match.getEndtime());
    }

    public static String timeRangePrettyString(TimeRange timeRange) {
        return timeRange.getDaySchedule().getName() + " " +
                fromMinutesToHHmm(timeRange.getStart()) + "-" +
                fromMinutesToHHmm(timeRange.getEnd());
    }
}