package picocli.shell.jline3.example;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.aggregators.Scenario;
import com.vanderhighway.trbac.core.CoreUtils;
import com.vanderhighway.trbac.core.modifier.PolicyAutomaticModifier;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.core.validator.PolicyValidator;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.*;
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
import org.eclipse.viatra.query.runtime.rete.matcher.TimelyReteBackendFactory;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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

    @CommandLine.Parameters(index = "0", description = "The policy file (.trbac)")
    private File file;

    /**
     * Top-level command that just prints help.
     */
    @Command(sortOptions = false, name = "",
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
    @Command(sortOptions = false, name = "cmd",mixinStandardHelpOptions = true, version = "0.2",
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
    /**
     * Command that clears the screen.
     */
    @Command(sortOptions = false, name = "cls", aliases = "clear",mixinStandardHelpOptions = true,
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
        //ViatraQueryLoggingUtil.getDefaultLogger().setLevel(Level.OFF);
        //org.apache.log4j.BasicConfigurator.resetConfiguration();
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
            System.out.println("version 1.0.4");

            //URI uri = URI.createFileURI("models/basic/intervals.trbac");
            //URI uri = URI.createFileURI("empty_policy_all_schedules.trbac");
            //URI uri = URI.createFileURI("performance_case.trbac");
            //URI uri = URI.createFileURI("simple_company.trbac");
            URI uri = URI.createFileURI(args[0]);

            Spinner fileLoadSpinner = new Spinner("Loading Policy Model... (" + uri.toString() + ") ");
            new Thread(fileLoadSpinner).start();

            // Initializing the EMF package
            TRBACPackage.eINSTANCE.getName();
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
            Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

            ResourceSet set = new ResourceSetImpl();
            Resource resource = set.getResource(uri, true);

            fileLoadSpinner.stop();

            Spinner xSpinner = new Spinner("Adding Omitted Schedules and Temporal Context Instances... ");
            new Thread(xSpinner).start();
            CoreUtils coteUtils = new CoreUtils();
            coteUtils.addMissingDaySchedules(resource, (SiteAccessControlSystem) resource.getContents().get(0));
            xSpinner.stop();

            Spinner queryEngineSpinner = new Spinner("Initializing Query Engine... ");
            new Thread(queryEngineSpinner).start();

            ViatraQueryEngineOptions options = ViatraQueryEngineOptions.defineOptions()
                    .withDefaultBackend(DRedReteBackendFactory.INSTANCE)
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

            PolicyModifier modifier = new PolicyModifier(engine, (SiteAccessControlSystem) resource.getContents().get(0), resource);
            modifier.setInstanceIDCounter(coteUtils.getInstanceIDCounter());
            CLIContainer.getInstance().setModifier(modifier);
            PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (SiteAccessControlSystem) resource.getContents().get(0));
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

            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();

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

    @Command(sortOptions = false, name = "name of entity commands",
            description = {
                    "description of entity commands"},
            footer = {"", "Press Ctl-D to exit."},
            subcommands = {
                    AddCommand.class, RemoveCommand.class, AssignCommand.class, DeassignCommand.class, ShowCommand.class, ExportCommand.class})
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

    @Command(sortOptions = false, name = "add",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class, AddConstraint.class},
            description = "add a model entity")
    static class AddCommand implements Runnable {

        public void run() {
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a user")
        public void user(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addUser(name);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a role")
        public void role(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addRole(name);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a demarcation")
        public void demarcation(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addDemarcation(name);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a permission")
        public void permission(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addPermission(name);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a security zone")
        public void securityzone(@Option(names = {"-building"}, required = true) String buildingName,
                                 @Option(names = {"-name"}, required = true) String name,
                                 @Option(names = {"-public"}, required = false) boolean isPublic) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addSecurityZone(name, isPublic);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal context")
        public void temporalcontext(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            CLIContainer.getInstance().getModifier().addTemporalContext(name);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal context instance")
        public void temporalcontextinstance(@Option(names = {"-context"}, required = true) String temporalContextName,
                                             @Option(names = {"-day"}, required = true) String dayScheduleName,
                                             @Option(names = {"-start"}, required = true) String startTime,
                                             @Option(names = {"-end"}, required = true) String endTime
                                             ) throws ModelManipulationException, ParseException, InvocationTargetException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class);
            if(context == null) {System.out.println("Unkown temporal context: " + context); return;}

            if(dayScheduleName.split("_").length == 3) {
                DateFormat format = new SimpleDateFormat("EEEE_d_MMMM", Locale.ENGLISH);
                try {
                    format.parse(dayScheduleName);

                } catch (ParseException pe) {
                    dayScheduleName = addDayToDateString(dayScheduleName);
                    // Do nothing. Apparently, there is no cleaner way to check if the format is respected.
                }
            }

            DaySchedule daySchedule = (DaySchedule) CLIContainer.getInstance().getModel().getEObject(dayScheduleName);
            if(daySchedule == null) {
                if(dayScheduleName.split("_").length == 4) {
                    DayOfWeekMonthSchedule wmSchedule = (DayOfWeekMonthSchedule) CLIContainer.getInstance().getElement(
                            dayScheduleName.split("_")[0]
                                    + "_" + dayScheduleName.split("_")[1]
                                    + "_" + dayScheduleName.split("_")[2],
                            DayOfWeekMonthSchedule.class
                    );
                    daySchedule = CLIContainer.getInstance().getModifier().addDayOfYearSchedule(wmSchedule, dayScheduleName);
                    CLIContainer.getInstance().getModifier().addDayScheduleTimeRange(daySchedule, new IntegerInterval(0, 1439));
                    CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
                }
            }

            CLIContainer.getInstance().getModifier().addTemporalContextInstance(context, daySchedule, new IntegerInterval(toMinutes(startTime), toMinutes(endTime)));

            //To prevent spurious scenarios appearing and dissapearing
            CLIContainer.getInstance().getEngine().delayUpdatePropagation( () -> {
                CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
                return null;
            });
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Add a temporal grant rule")
        public void temporalgrantrule(
                @Option(names = {"-name"}, required = true) String ruleName,
                @Option(names = {"-context"}, required = true) String temporalContextName,
                @Option(names = {"-role"}, required = true) String roleName,
                @Option(names = {"-dem"}, required = true) String demarcationName,
                @Option(names = {"-command"}, required = true) String commandName,
                @Option(names = {"-priority"}, required = true) int priority
        ) throws ModelManipulationException, InvocationTargetException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class);
            Role role = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class);
            if(commandName.toLowerCase().equals("grant")) {
                CLIContainer.getInstance().getModifier().addTemporalGrantRule(context, ruleName, role, demarcation, true, priority);
            } else if(commandName.toLowerCase().equals("revoke")) {
                CLIContainer.getInstance().getModifier().addTemporalGrantRule(context, ruleName, role, demarcation, false, priority);
            } else {
                System.out.println("Command should either be \"grant\" or \"revoke\", not " + commandName);
            }
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }
    }

    @Command(sortOptions = false, name = "remove",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "remove a model entity")
    static class RemoveCommand implements Runnable {

        public void run() {
            System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a user")
        public void user(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            User user = (User) CLIContainer.getInstance().getElement(name, User.class);
            CLIContainer.getInstance().getModifier().removeUser(user);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a role")
        public void role(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            Role role = (Role) CLIContainer.getInstance().getElement(name, Role.class);
            CLIContainer.getInstance().getModifier().removeRole(role);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a demarcation")
        public void demarcation(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getElement(name, Demarcation.class);
            CLIContainer.getInstance().getModifier().removeDemarcation(demarcation);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a permission")
        public void permission(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            Permission permission = (Permission) CLIContainer.getInstance().getElement(name, Permission.class);
            CLIContainer.getInstance().getModifier().removePermission(permission);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a security zone")
        public void securityzone(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getElement(name, SecurityZone.class);
            CLIContainer.getInstance().getModifier().removeSecurityZone(securityZone);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal context")
        public void temporalcontext(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            TemporalContext context = (TemporalContext) CLIContainer.getInstance().getElement(name, TemporalContext.class);
            CLIContainer.getInstance().getModifier().removeTemporalContext(context);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal context instance")
        public void temporalcontextinstance(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            TimeRange timeRange = (TimeRange) CLIContainer.getInstance().getElement(name, TimeRange.class);
            CLIContainer.getInstance().getModifier().removeTemporalContextInstance(timeRange);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal grant rule")
        public void temporalgrantrule(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            TemporalGrantRule rule = (TemporalGrantRule) CLIContainer.getInstance().getElement(name, TemporalGrantRule.class);
            CLIContainer.getInstance().getModifier().removeTemporalGrantRule(rule);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal grant rule")
        public void temporalauthenticationrule(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            TemporalAuthenticationRule rule = (TemporalAuthenticationRule) CLIContainer.getInstance().getElement(name, TemporalAuthenticationRule.class);
            CLIContainer.getInstance().getModifier().removeTemporalAuthenticationRule(rule);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove a temporal grant rule")
        public void constraint(@Option(names = {"-name"}, required = true) String name) throws ModelManipulationException, InvocationTargetException {
            AuthorizationConstraint constraint = (AuthorizationConstraint) CLIContainer.getInstance().getElement(name, AuthorizationConstraint.class);
            CLIContainer.getInstance().getModifier().removeAuthorizationConstraint(constraint);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }
    }

    @Command(sortOptions = false, name = "assign",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "assign entities")
    static class AssignCommand implements Runnable {

        public void run() {
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a role to a user")
        public void UR(@Option(names = {"-user"}, required = true) String userName,
                       @Option(names = {"-role"}, required = true) String roleName) throws ModelManipulationException, InvocationTargetException {
            User user = (User) CLIContainer.getInstance().getElement(userName, User.class);
            Role role = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            CLIContainer.getInstance().getModifier().assignRoleToUser(user, role);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a permission to a demarcation")
        public void DP(@Option(names = {"-dem"}, required = true) String demarcationName,
                       @Option(names = {"-permission"}, required = true) String permissionName) throws ModelManipulationException, InvocationTargetException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class);
            Permission permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            CLIContainer.getInstance().getModifier().assignPermissionToDemarcation(demarcation, permission);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign an object to a permission")
        public void PO(@Option(names = {"-permission"}, required = true) String permissionName,
                       @Option(names = {"-object"}, required = true) String objectName) throws ModelManipulationException, InvocationTargetException {
            Permission permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getElement(objectName, SecurityZone.class);
            CLIContainer.getInstance().getModifier().assignObjectToPermission(permission, securityZone);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set reachability between two zones")
        public void reachability(@Option(names = {"-from"}, required = true) String fromZoneName,
                                 @Option(names = {"-to"}, required = true) String toZoneName,
                                 @Option(names = {"-bidirectional"}, required = false) boolean isBirectional) throws ModelManipulationException, InvocationTargetException {
            SecurityZone fromSecurityZone = (SecurityZone) CLIContainer.getInstance().getElement(fromZoneName, SecurityZone.class);
            SecurityZone toSecurityZone = (SecurityZone) CLIContainer.getInstance().getElement(toZoneName, SecurityZone.class);
            if (isBirectional) {
                CLIContainer.getInstance().getModifier().setBidirectionalReachability(fromSecurityZone, toSecurityZone);
                CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
            } else {
                CLIContainer.getInstance().getModifier().setReachability(fromSecurityZone, toSecurityZone);
                CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two roles")
        public void role_inheritance(@Option(names = {"-junior"}, required = true) String juniorRoleName,
                                     @Option(names = {"-senior"}, required = true) String seniorRoleName) throws ModelManipulationException, InvocationTargetException {
            Role juniorRole = (Role) CLIContainer.getInstance().getElement(juniorRoleName, Role.class);
            Role seniorRole = (Role) CLIContainer.getInstance().getElement(seniorRoleName, Role.class);
            CLIContainer.getInstance().getModifier().addRoleInheritance(juniorRole, seniorRole);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two demarcations")
        public void demarcation_inheritance(@Option(names = {"-sub"}, required = true) String subDemarcationName,
                                     @Option(names = {"-sup"}, required = true) String supDemarcationName) throws ModelManipulationException, InvocationTargetException {
            Demarcation subDemarcation = (Demarcation) CLIContainer.getInstance().getElement(subDemarcationName, Demarcation.class);
            Demarcation supDemarcation = (Demarcation) CLIContainer.getInstance().getElement(supDemarcationName, Demarcation.class);
            CLIContainer.getInstance().getModifier().addDemarcationInheritance(subDemarcation, supDemarcation);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }
    }

    @Command(sortOptions = false, name = "deassign",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "deassign entities")
    static class DeassignCommand implements Runnable {

        public void run() { }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a role to a user")
        public void UR(@Option(names = {"-user"}, required = true) String userName,
                       @Option(names = {"-role"}, required = true) String roleName) throws ModelManipulationException, InvocationTargetException {
            User user = (User) CLIContainer.getInstance().getElement(userName, User.class);
            Role role = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            CLIContainer.getInstance().getModifier().deassignRoleFromUser(user, role);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign a permission to a demarcation")
        public void DP(@Option(names = {"-dem"}, required = true) String demarcationName,
                       @Option(names = {"-permission"}, required = true) String permissionName) throws ModelManipulationException, InvocationTargetException {
            Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class);
            Permission permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            CLIContainer.getInstance().getModifier().deassignPermissionFromDemarcation(demarcation, permission);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Assign an object to a permission")
        public void PO(@Option(names = {"-permission"}, required = true) String permissionName,
                       @Option(names = {"-object"}, required = true) String objectName) throws ModelManipulationException, InvocationTargetException {
            Permission permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            SecurityZone securityZone = (SecurityZone) CLIContainer.getInstance().getElement(objectName, SecurityZone.class);
            CLIContainer.getInstance().getModifier().deassignObjectFromPermission(permission, securityZone);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Remove reachability between two zones")
        public void reachability(@Option(names = {"-from"}, required = true) String fromZoneName,
                                 @Option(names = {"-to"}, required = true) String toZoneName,
                                 @Option(names = {"-bidirectional"}, required = false) boolean isBirectional) throws ModelManipulationException, InvocationTargetException {
            SecurityZone fromSecurityZone = (SecurityZone) CLIContainer.getInstance().getElement(fromZoneName, SecurityZone.class);
            SecurityZone toSecurityZone = (SecurityZone) CLIContainer.getInstance().getElement(toZoneName, SecurityZone.class);
            if(isBirectional) {
                CLIContainer.getInstance().getModifier().removeBidirectionalReachability(fromSecurityZone, toSecurityZone);
            } else {
                CLIContainer.getInstance().getModifier().removeReachability(fromSecurityZone, toSecurityZone);
            }
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two roles")
        public void role_inheritance(@Option(names = {"-junior"}, required = true) String juniorRoleName,
                                     @Option(names = {"-senior"}, required = true) String seniorRoleName) throws ModelManipulationException, InvocationTargetException {
            Role juniorRole = (Role) CLIContainer.getInstance().getElement(juniorRoleName, Role.class);
            Role seniorRole = (Role) CLIContainer.getInstance().getElement(seniorRoleName, Role.class);
            CLIContainer.getInstance().getModifier().removeRoleInheritance(juniorRole, seniorRole);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Set inheritance between two demarcations")
        public void demarcation_inheritance(@Option(names = {"-sub"}, required = true) String subDemarcationName,
                                            @Option(names = {"-sup"}, required = true) String supDemarcationName) throws ModelManipulationException, InvocationTargetException {
            Demarcation subDemarcation = (Demarcation) CLIContainer.getInstance().getElement(subDemarcationName, Demarcation.class);
            Demarcation supDemarcation = (Demarcation) CLIContainer.getInstance().getElement(supDemarcationName, Demarcation.class);
            CLIContainer.getInstance().getModifier().removeDemarcationInheritance(subDemarcation, supDemarcation);
            CLIContainer.getInstance().getAutomaticModifier().getTransformation().getExecutionSchema().startUnscheduledExecution();
        }
    }

    @Command(sortOptions = false, name = "show",mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "show stuff")
    static class ShowCommand implements Runnable {

        public void run() {
            //System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all basic entities and relations (Users, Roles, Demarcations, Permissions, UR, DP)")
        public void basic() throws ModelManipulationException {

            System.out.println("Users:");
            CLIContainer.getInstance().increaseIndentation();
            users();
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("Roles:");
            CLIContainer.getInstance().increaseIndentation();
            roles();
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("UR:");
            CLIContainer.getInstance().increaseIndentation();
            UR(null);
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("Demarcations:");
            CLIContainer.getInstance().increaseIndentation();
            demarcations();
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("RSD:");
            CLIContainer.getInstance().increaseIndentation();
            RSD(null, null);
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("Permissions:");
            CLIContainer.getInstance().increaseIndentation();
            permissions();
            CLIContainer.getInstance().decreaseIdentation();

            System.out.println("DP:");
            CLIContainer.getInstance().increaseIndentation();
            DP(null);
            CLIContainer.getInstance().decreaseIdentation();
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all users")
        public void users() throws ModelManipulationException {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> users = system.getAuthorizationPolicy().getUsers().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(users);
            for(String user: users) {
                System.out.println(CLIContainer.getInstance().getIndentation() + user);
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all roles")
        public void roles() throws ModelManipulationException {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> roles = system.getAuthorizationPolicy().getRoles().stream().map(PolicyValidatorCLI::rolePrettyString).
                    sorted().collect(Collectors.toList());
            for (String role: roles) {
                System.out.println(CLIContainer.getInstance().getIndentation() + role);
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all demarcations")
        public void demarcations() throws ModelManipulationException {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> demarcations = system.getAuthorizationPolicy().getDemarcations().stream().map(PolicyValidatorCLI::demarcationPrettyString)
                    .collect(Collectors.toList());
            Collections.sort(demarcations);
            for(String demarcationName: demarcations) {
                System.out.println(CLIContainer.getInstance().getIndentation() + demarcationName);
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all permissions")
        public void permissions() throws ModelManipulationException {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<String> permissions = system.getAuthorizationPolicy().getPermissions().stream().map(x -> x.getName()).collect(Collectors.toList());
            Collections.sort(permissions);
            for(String permission: permissions) {
                System.out.println(CLIContainer.getInstance().getIndentation() + permission);
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the roles assigned to one or all users")
        public void UR(@Option(names = {"-user"}, required = false) String userName) throws ModelManipulationException {
            List<User> users = new LinkedList<>();
            if(userName == null) {
                users = CLIContainer.getInstance().getSystem().getAuthorizationPolicy().getUsers();
            } else {
                users.add((User) CLIContainer.getInstance().getElement(userName, User.class));
            }
            users = users.stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
            for (User user: users) {
                System.out.println(CLIContainer.getInstance().getIndentation() + user.getName() + "->" +
                        user.getUR().stream().map(Role::getName).sorted().collect(Collectors.toList()));
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the users which are assigned to one or all roles")
        public void RU(@Option(names = {"-role"}, required = false) String roleName) throws ModelManipulationException {
            List<Role> roles = new LinkedList<>();
            if(roleName == null) {
                roles = CLIContainer.getInstance().getSystem().getAuthorizationPolicy().getRoles();
            } else {
                roles.add((Role) CLIContainer.getInstance().getElement(roleName, User.class));
            }
            roles = roles.stream().sorted(Comparator.comparing(Role::getName)).collect(Collectors.toList());
            for (Role role: roles) {
                System.out.println(CLIContainer.getInstance().getIndentation() + role.getName() + "->" +
                        role.getRU().stream().map(User::getName).sorted().collect(Collectors.toList()));
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the demarcations granted to users")
        public void USD(@Option(names = {"-user"}, required = false) String userName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<User, Map<Scenario, Set<Demarcation>>> relation = new HashMap<>();
            User partialMatchUser = null;
            if(userName != null) {
                partialMatchUser = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
            USD.Match partialMatch = USD.Matcher.create().newMatch(partialMatchUser, null, null);
            USD.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(USD.instance());
            Set<USD.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
            for (USD.Match match: matches){
                relation.putIfAbsent(match.getUser(), new HashMap<>());
                if(match.getScenario().containsAll(partialTemporalContexts)) {
                    relation.get(match.getUser()).putIfAbsent(match.getScenario(), new HashSet<>());
                    relation.get(match.getUser()).get(match.getScenario()).add(match.getDemarcation());
                }
            }
            List<User> sortedUsers = relation.keySet().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
            for (User user: sortedUsers) {
                if(sortedUsers.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + user.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(user).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(user).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedUsers.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the permissions granted to users")
        public void USP(@Option(names = {"-user"}, required = false) String userName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<User, Map<Scenario, Set<Permission>>> relation = new HashMap<>();
            User partialMatchUser = null;
            if(userName != null) {
                partialMatchUser = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
            USP.Match partialMatch = USP.Matcher.create().newMatch(partialMatchUser, null, null);
            USP.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(USP.instance());
            Set<USP.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
            for (USP.Match match: matches){
                relation.putIfAbsent(match.getUser(), new HashMap<>());
                if(match.getScenario().containsAll(partialTemporalContexts)) {
                    relation.get(match.getUser()).putIfAbsent(match.getScenario(), new HashSet<>());
                    relation.get(match.getUser()).get(match.getScenario()).add(match.getPermission());
                }
            }
            List<User> sortedUsers = relation.keySet().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
            for (User user: sortedUsers) {
                if(sortedUsers.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + user.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(user).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(user).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedUsers.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the demarcations granted to roles")
        public void RSD(@Option(names = {"-role"}, required = false) String roleName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<Role, Map<Scenario, Set<Demarcation>>> relation = new HashMap<>();
            Role partialMatchRole = null;
            if(roleName != null) {
                partialMatchRole = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
            RSD.Match partialMatch = RSD.Matcher.create().newMatch(partialMatchRole, null, null);
            RSD.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(RSD.instance());
            Set<RSD.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
            for (RSD.Match match: matches){
                relation.putIfAbsent(match.getRole(), new HashMap<>());
                if(match.getScenario().containsAll(partialTemporalContexts)) {
                    relation.get(match.getRole()).putIfAbsent(match.getScenario(), new HashSet<>());
                    relation.get(match.getRole()).get(match.getScenario()).add(match.getDemarcation());
                }
            }
            List<Role> sortedRoles = relation.keySet().stream().sorted(Comparator.comparing(Role::getName)).collect(Collectors.toList());
            for (Role role: sortedRoles) {
                if(sortedRoles.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + role.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(role).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(role).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedRoles.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the demarcations granted to roles")
        public void RSP(@Option(names = {"-role"}, required = false) String roleName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<Role, Map<Scenario, Set<Permission>>> relation = new HashMap<>();
            Role partialMatchRole = null;
            if(roleName != null) {
                partialMatchRole = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
            RSP.Match partialMatch = RSP.Matcher.create().newMatch(partialMatchRole, null, null);
            RSP.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(RSP.instance());
            Set<RSP.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
            for (RSP.Match match: matches){
                relation.putIfAbsent(match.getRole(), new HashMap<>());
                if(match.getScenario().containsAll(partialTemporalContexts)) {
                    relation.get(match.getRole()).putIfAbsent(match.getScenario(), new HashSet<>());
                    relation.get(match.getRole()).get(match.getScenario()).add(match.getPermission());
                }
            }
            List<Role> sortedRoles = relation.keySet().stream().sorted(Comparator.comparing(Role::getName)).collect(Collectors.toList());
            for (Role role: sortedRoles) {
                if(sortedRoles.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + role.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(role).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(role).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedRoles.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the permissions assigned to one or all demarcations")
        public void DP(@Option(names = {"-dem"}, required = false) String demarcationName) throws ModelManipulationException {
            List<Demarcation> demarcations = new LinkedList<>();
            if(demarcationName == null) {
                demarcations = CLIContainer.getInstance().getSystem().getAuthorizationPolicy().getDemarcations();
            } else {
                demarcations.add((Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class));
            }
            demarcations = demarcations.stream().sorted(Comparator.comparing(Demarcation::getName)).collect(Collectors.toList());
            for (Demarcation demarcation: demarcations) {
                System.out.println(CLIContainer.getInstance().getIndentation() + demarcation.getName() + "->" +
                        demarcation.getDP().stream().map(Permission::getName).sorted().collect(Collectors.toList()));
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show the permissions which are assigned to one or all demaracations")
        public void PD(@Option(names = {"-perm"}, required = false) String permissionName) throws ModelManipulationException {
            List<Permission> permissions = new LinkedList<>();
            if(permissionName == null) {
                permissions = CLIContainer.getInstance().getSystem().getAuthorizationPolicy().getPermissions();
            } else {
                permissions.add((Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class));
            }
            permissions = permissions.stream().sorted(Comparator.comparing(Permission::getName)).collect(Collectors.toList());
            for (Permission permission: permissions) {
                System.out.println(CLIContainer.getInstance().getIndentation() + permission.getName() + "->" +
                        permission.getPD().stream().map(Demarcation::getName).sorted().collect(Collectors.toList()));
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show security zones the user is allowed to access")
        public void USO(@Option(names = {"-user"}, required = false) String userName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<User, Map<Scenario, Set<XObject>>> relation = new HashMap<>();
            User partialMatchUser = null;
            if(userName != null) {
                partialMatchUser = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
            USO.Match partialMatch = USO.Matcher.create().newMatch(partialMatchUser, null, null);
            USO.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(USO.instance());
            Set<USO.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
            for (USO.Match match: matches){
                relation.putIfAbsent(match.getUser(), new HashMap<>());
                if(match.getScenario().containsAll(partialTemporalContexts)) {
                    relation.get(match.getUser()).putIfAbsent(match.getScenario(), new HashSet<>());
                    relation.get(match.getUser()).get(match.getScenario()).add(match.getObject());
                }
            }
            List<User> sortedUsers = relation.keySet().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
            for (User user: sortedUsers) {
                if(sortedUsers.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + user.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(user).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(user).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedUsers.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show security zones the user can access")
        public void access(@Option(names = {"-user"}, required = false) String userName,
                        @Option(names = {"-context"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                required = false) List<String> contextNames) throws ModelManipulationException {
            Map<User, Map<Scenario, Set<XObject>>> relation = new HashMap<>();
            User partialMatchUser = null;
            if(userName != null) {
                partialMatchUser = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Set<TemporalContext> partialTemporalContexts = new HashSet<>();
            if(contextNames != null) {
                for (String temporalContextName: contextNames) {
                    partialTemporalContexts.add((TemporalContext) CLIContainer.getInstance().getElement(temporalContextName, TemporalContext.class));
                }
            }
//            SecurityZoneAccessible.Match partialMatch = SecurityZoneAccessible.Matcher.create().newMatch(partialMatchUser, null, null);
//            SecurityZoneAccessible.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(SecurityZoneAccessible.instance());
//            Set<SecurityZoneAccessible.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
//            for (SecurityZoneAccessible.Match match: matches){
//                relation.putIfAbsent(match.getUser(), new HashMap<>());
//                if(match.getScenario().containsAll(partialTemporalContexts)) {
//                    relation.get(match.getUser()).putIfAbsent(match.getScenario(), new HashSet<>());
//                    relation.get(match.getUser()).get(match.getScenario()).add(match.getZone());
//                }
//            }
            List<User> sortedUsers = relation.keySet().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
            for (User user: sortedUsers) {
                if(sortedUsers.size() != 1) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + user.getName() + ":");
                    CLIContainer.getInstance().increaseIndentation();
                }
                List<Scenario> sortedScenarios = relation.get(user).keySet().stream().sorted(Comparator.comparing(Scenario::toString)).collect(Collectors.toList());
                for (Scenario scenario : sortedScenarios) {
                    List<String> sortedNames = relation.get(user).get(scenario).stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
                    System.out.println(CLIContainer.getInstance().getIndentation()
                            + scenario.toString() + " -> " + sortedNames.toString());
                }
                if(sortedUsers.size() != 1) {
                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all security zones")
        public void securityzones(@Option(names = {"-status"}, required = false) boolean showStatus) {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<SecurityZone> zones = system.getSecurityZones().stream().
                    sorted( Comparator.comparing(SecurityZone::getName)).collect(Collectors.toList());
            for (SecurityZone zone: zones) {
                System.out.println(CLIContainer.getInstance().getIndentation() + zone.getName());
                CLIContainer.getInstance().increaseIndentation();
                System.out.println(CLIContainer.getInstance().getIndentation() + "connected to outside: " +
                        zone.isPublic());
                System.out.println(CLIContainer.getInstance().getIndentation() + "connects to: " +
                        zone.getReachable().stream().map(x -> x.getName()).sorted().collect(Collectors.toList()));

                if(showStatus) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + "status: ");
                    CLIContainer.getInstance().increaseIndentation();
                    SecurityZoneAccessStatus.Match partialMatch = SecurityZoneAccessStatus.Matcher.create().
                            newMatch(null, zone, null);
                    SecurityZoneAccessStatus.Matcher matcher = CLIContainer.getInstance().getEngine()
                            .getMatcher(SecurityZoneAccessStatus.instance());
                    Set<SecurityZoneAccessStatus.Match> matches = new HashSet<>(matcher.getAllMatches(partialMatch));
                    matches.stream().sorted(Comparator.comparing(m -> m.getScenario().toString())).
                            forEach(m -> System.out.println(CLIContainer.getInstance().getIndentation()
                                    + m.getScenario().toString() + " -> " + AuthenticationStatus.toName(m.getStatus())));
                    CLIContainer.getInstance().decreaseIdentation();
                }

                System.out.println(CLIContainer.getInstance().getIndentation() + "constrained by: " +
                        zone.getConstrainedBy().stream().map(x -> x.getName()).sorted().collect(Collectors.toList()));

                CLIContainer.getInstance().decreaseIdentation();
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all computed scenarios")
        public void scenarios( @Option(names = {"-example"}, description = "Show an example instance.", required = false) boolean example,
                @Option(names = {"-date"}, description = "Show all scenarios for a specific date", required = false) String dateString
        ) throws ParseException, ModelManipulationException {
            if(dateString == null) {
                Set<Scenarios.Match> matches = CLIContainer.getInstance().getEngine().getMatcher(Scenarios.instance()).getAllMatches().stream().collect(Collectors.toSet());
                List<String> scenarioStrings = new LinkedList();
                for (Scenarios.Match match : matches) {
                    String scenarioString = match.getScenario().toString();
                    // Please forgive me, for I have written horrible code.
                    if (example) {
                        scenarioString += " (ex: " + getExampleDateScheduleTimeRange(match.getScenario()) + ")";
                    }
                    scenarioStrings.add(scenarioString);
                }

                Collections.sort(scenarioStrings);
                for (int i = 0; i < scenarioStrings.size(); i++) {
                    System.out.println(i + ": " + scenarioStrings.get(i));
                }
            } else {
                throw new IllegalArgumentException("TODO: fix this");
                //TODO!
//                if (dateString.split("_").length == 3) {
//                    dateString = addDayToDateString(dateString);
//                }
//                DayOfYearSchedule schedule = (DayOfYearSchedule) CLIContainer.getInstance().getElement(dateString, DayOfYearSchedule.class);
//                DateScheduleInstance_To_Scenario.Match partialMatch = DateScheduleInstance_To_Scenario.Matcher.create().newMatch(schedule, null, null, null);
//                DateScheduleInstance_To_Scenario.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(DateScheduleInstance_To_Scenario.instance());
//                List<DateScheduleInstance_To_Scenario.Match> matches = matcher.getAllMatches(partialMatch).stream().sorted((m1, m2) -> m1.getStarttime().compareTo(m2.getStarttime())).collect(Collectors.toList());
//                for (DateScheduleInstance_To_Scenario.Match match : matches) {
//                    System.out.println( "[" + fromMinutesToHHmm(match.getStarttime()) + "-" + fromMinutesToHHmm(match.getEndtime()) + "]"
//                            + " " + match.getScenario().toString());
//                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show day schedules instances")
        public void dayscheduleinstances(
                @Option(names = {"-week"}, description = "Show all scenarios for a specific date", required = false) boolean showWeek
                ) throws ModelManipulationException {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            if(showWeek) {
                List<DayOfWeekSchedule> dayOfWeekSchedules = new ArrayList();
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Monday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Tuesday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Wednesday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Thursday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Friday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Saturday", DayOfWeekSchedule.class));
                dayOfWeekSchedules.add((DayOfWeekSchedule) CLIContainer.getInstance().getElement("Sunday", DayOfWeekSchedule.class));
                for (DayOfWeekSchedule dayOfWeekSchedule: dayOfWeekSchedules) {
                    System.out.println(CLIContainer.getInstance().getIndentation() + dayOfWeekSchedule.getName());
                    CLIContainer.getInstance().increaseIndentation();

                    List<DayScheduleTimeRange> instances = dayOfWeekSchedule.getInstances().stream().sorted(Comparator.comparingInt(TimeRange::getStart)).collect(Collectors.toList());
                    for (DayScheduleTimeRange instance: instances) {
                        Scenario scenario = new Scenario(instance.getTemporalContextTimeRanges().stream().map(x -> ((TimeRange) x).eContainer())
                                .map(x -> (TemporalContext) x).collect(Collectors.toSet()));

                        System.out.println(CLIContainer.getInstance().getIndentation() + timeTimeRangePrettyString(instance));
                        System.out.println("temporal contexts: " + scenario.toString());
                        System.out.println("temporal context instances: " + instance.getTemporalContextTimeRanges().stream().map(t -> "(" + t.getName() + ", " + timeTimeRangePrettyString(t) + ")").sorted().collect(Collectors.toList()).toString());
                        System.out.println();
                    }

                    CLIContainer.getInstance().decreaseIdentation();
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all temporal contexts")
        public void temporalcontexts() {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = system.getSchedule();
            List<TemporalContext> temporalContexts = schedule.getTemporalContexts().stream().sorted(
                    Comparator.comparing(TemporalContext::getName)
            ).collect(Collectors.toList());
            for(TemporalContext tc: temporalContexts) {
                System.out.println(tc.getName());
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show n temporal context instances (default=5)")
        public void temporalcontextinstances(@Option(names = {"-n"}, required = false) int instanceCount) {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = system.getSchedule();
            List<TemporalContext> temporalContexts = schedule.getTemporalContexts().stream().sorted(
                    Comparator.comparing(TemporalContext::getName)
            ).collect(Collectors.toList());

            if(instanceCount == 0) {
                instanceCount = 5;
            }
            for(TemporalContext context: temporalContexts) {
                if(context.getInstances().size() == 0) {
                    System.out.println(context.getName() + "-> []");
                } else {
                    System.out.println(context.getName() + "->" +
                            context.getInstances().stream().limit(instanceCount)
                                    .map(PolicyValidatorCLI::timeTimeRangePrettyString).collect(Collectors.toList())
                    );
                }
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all temporal grant rules")
        public void temporalgrantrules() {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = system.getSchedule();
            List<TemporalGrantRule> temporalGrantRules = schedule.getTemporalGrantRules().stream().sorted(
                    Comparator.comparing(TemporalGrantRule::getName)
            ).collect(Collectors.toList());
            for (TemporalGrantRule rule: temporalGrantRules) {
                String command = rule.isEnable() ? "grant" : "revoke";
                System.out.println( rule.getName() + " : " + command + " " + rule.getRole().getName() + " access to "
                        + rule.getDemarcation().getName() + " during " + rule.getTemporalContext().getName() + " with priority " + rule.getPriority());
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all temporal authenticaion rules")
        public void temporalauthenticationrules() {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            Schedule schedule = system.getSchedule();
            List<TemporalAuthenticationRule> temporalAuthenticationRules = system.getAuthenticationPolicy().getTemporalAuthenticationRules()
                    .stream().sorted(
                    Comparator.comparing(TemporalAuthenticationRule::getName)
            ).collect(Collectors.toList());
            for (TemporalAuthenticationRule rule: temporalAuthenticationRules) {
                String command = rule.getStatus() == 0 ? "unlocked" :
                        rule.getStatus() == 1 ? "protected" : "locked";
                System.out.println(rule.getName() + " : " + command + " " + rule.getSecurityZone() +
                        " during " + rule.getTemporalContext().getName() + " with priority " + rule.getPriority());
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all constraints")
        public void constraints() {
            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            List<AuthorizationConstraint> authorizationConstraints = system.getAuthorizationConstraints().stream().sorted(
                    Comparator.comparing(AuthorizationConstraint::getName)
            ).collect(Collectors.toList());
            for (AuthorizationConstraint constraint: authorizationConstraints) {
                System.out.println(constraint.getName() + ": " + ConstraintHelper.toString(constraint));
            }
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all constraint violations")
        public void violations(@Option(names = {"-user"}, required = false) String userName,
                               @Option(names = {"-role"}, required = false) String roleName,
                               @Option(names = {"-demarcation"}, required = false) String demarcationName,
                               @Option(names = {"-permission"}, required = false) String permissionName,
                               @Option(names = {"-scenario"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                       required = false) List<String> contextNames) throws ModelManipulationException {
            User user = null;
            if(userName != null) {
                user = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Role role = null;
            if(roleName != null) {
                role = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            }
            Demarcation demarcation = null;
            if(demarcationName != null) {
                demarcation = (Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class);
            }
            Permission permission = null;
            if(permissionName != null) {
                permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            }

            Scenario scenario = null;
            if(contextNames != null) {
                Scenario matchScenario = new Scenario(new HashSet<>());
                for (String contextName: contextNames) {
                    matchScenario.add((TemporalContext) CLIContainer.getInstance().getElement(contextName, TemporalContext.class));
                }
                if(CLIContainer.getInstance().getEngine().getMatcher(Scenarios.instance()).getAllMatches(matchScenario).stream().map(Scenarios.Match::getScenario).collect(Collectors.toSet())
                        .contains(matchScenario)) {
                    scenario = matchScenario;
                } else {
                    System.out.println("Given scenario: " + contextNames.toString() + " does not correspond to an one unique actual scenario!");
                    return;
                }
            }

            CLIContainer.getInstance().getEngine().getMatcher(SoDURPattern.instance()).getAllMatches(null, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(SoDUDPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(SoDUPPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(SoDRDPattern.instance()).getAllMatches(null, scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(SoDRPPattern.instance()).getAllMatches(null, scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(SoDDPPattern.instance()).getAllMatches(null, demarcation).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteURPattern.instance()).getAllMatches(null, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteUDPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteUPPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteRDPattern.instance()).getAllMatches(null, scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteRPPattern.instance()).getAllMatches(null, scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(PrerequisiteDPPattern.instance()).getAllMatches(null, demarcation).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDURPattern.instance()).getAllMatches(null, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDUDPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDUPPattern.instance()).getAllMatches(null, scenario, user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDRDPattern.instance()).getAllMatches(null , scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDRPPattern.instance()).getAllMatches(null, scenario, role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(BoDDPPattern.instance()).getAllMatches(null, demarcation).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityURPattern.instance()).getAllMatches().stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityUDPattern.instance()).getAllMatches(null, scenario, null).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityUPPattern.instance()).getAllMatches(null, scenario, null).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityRDPattern.instance()).getAllMatches(null, scenario, null).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityRPPattern.instance()).getAllMatches(null, scenario, null).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(CardinalityDPPattern.instance()).getAllMatches().stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","").replace("Pattern","") + " - " + c.prettyPrint().replace("\"","")));
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show all policy smells")
        public void smells(@Option(names = {"-user"}, required = false) String userName,
                           @Option(names = {"-role"}, required = false) String roleName,
                           @Option(names = {"-demarcation"}, required = false) String demarcationName,
                           @Option(names = {"-permission"}, required = false) String permissionName,
                           @Option(names = {"-object"}, required = false) String objectName,
                           @Option(names = {"-scenario"}, split = ",", splitSynopsisLabel = ",", paramLabel = "CONTEXT",
                                   required = false) List<String> contextNames) throws ModelManipulationException {
            User user = null;
            if(userName != null) {
                user = (User) CLIContainer.getInstance().getElement(userName, User.class);
            }
            Role role = null;
            if(roleName != null) {
                role = (Role) CLIContainer.getInstance().getElement(roleName, Role.class);
            }
            Demarcation demarcation = null;
            if(demarcationName != null) {
                demarcation = (Demarcation) CLIContainer.getInstance().getElement(demarcationName, Demarcation.class);
            }
            Permission permission = null;
            if(permissionName != null) {
                permission = (Permission) CLIContainer.getInstance().getElement(permissionName, Permission.class);
            }

            SecurityZone zone = null;
            if(objectName != null) {
                zone = (SecurityZone) CLIContainer.getInstance().getElement(objectName, SecurityZone.class);
            }

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

            SiteAccessControlSystem system = (SiteAccessControlSystem) CLIContainer.getInstance().getModel().getContents().get(0);
            CLIContainer.getInstance().getEngine().getMatcher(UnusedRole.instance()).getAllMatches(role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(UnusedDemarcation.instance()).getAllMatches(demarcation).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(UnusedPermission.instance()).getAllMatches(permission).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(ZombieDemarcation.instance()).getAllMatches(demarcation).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(ZombiePermission.instance()).getAllMatches(permission).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            //CLIContainer.getInstance().getEngine().getMatcher(UnreachableZone.instance()).getAllMatches(zone).stream().forEach(c -> System.out.println(
            //        c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(GodUser.instance()).getAllMatches(user).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            CLIContainer.getInstance().getEngine().getMatcher(GodRole.instance()).getAllMatches(role).stream().forEach(c -> System.out.println(
                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));

            if(role == null) {
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredRoleInheritance.instance()).getAllMatches(user, null, null).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.", "") + " - " + c.prettyPrint().replace("\"", "")));
            } else {
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredRoleInheritance.instance()).getAllMatches(user, role, null).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.", "") + " - " + c.prettyPrint().replace("\"", "")));
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredRoleInheritance.instance()).getAllMatches(user, null, role).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.", "") + " - " + c.prettyPrint().replace("\"", "")));
            }

            if(demarcation == null) {
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredDemarcationInheritance.instance()).getAllMatches(role, scenario, null, null).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            } else {
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredDemarcationInheritance.instance()).getAllMatches(role, scenario, demarcation, null).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
                CLIContainer.getInstance().getEngine().getMatcher(IgnoredDemarcationInheritance.instance()).getAllMatches(role, scenario, null, demarcation).stream().forEach(c -> System.out.println(
                        c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
            }
//            CLIContainer.getInstance().getEngine().getMatcher(UserCanGetTrapped.instance()).getAllMatches(user, scenario, zone).stream().forEach(c -> System.out.println(
//                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
//            CLIContainer.getInstance().getEngine().getMatcher(UninvocablePermission.instance()).getAllMatches(user, scenario, permission, zone).stream().forEach(c -> System.out.println(
//                    c.patternName().replace("com.vanderhighway.trbac.patterns.","") + " - " + c.prettyPrint().replace("\"","")));
        }

        @Command(sortOptions = false, mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Show used memory (runs gc first)")
        public void memory() {
            // Get the Java runtime
            Runtime runtime = Runtime.getRuntime();
            // Run the garbage collector
            runtime.gc();
            // Calculate the used memory
            long memory = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            System.out.println("used memory: " + memory + " MB");
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

    public static String fromMinutesToHHmm(int minutes) {
        long hours = TimeUnit.MINUTES.toHours(Long.valueOf(minutes));
        long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%02d:%02d", hours, remainMinutes);
    }

    public static String getExampleDateScheduleTimeRange(Scenario set) {
        DateScheduleInstance_To_Scenario.Match partialMatch1 = DateScheduleInstance_To_Scenario.Matcher.create().newMatch(null, null, null, set);
        DateScheduleInstance_To_Scenario.Matcher matcher1 = CLIContainer.getInstance().getEngine().getMatcher(
                DateScheduleInstance_To_Scenario.instance());
        Optional<DateScheduleInstance_To_Scenario.Match> match1 = matcher1.getOneArbitraryMatch(partialMatch1);
        if(match1.isPresent()) {
            return match1.get().getDaySchedule().getName() + " " +
                    fromMinutesToHHmm(match1.get().getStarttime()) + "-" +
                    fromMinutesToHHmm(match1.get().getEndtime());
        } else {
            DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario.Match partialMatch2 = DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario
                    .Matcher.create().newMatch(null, null, null, set);
            DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario.Matcher matcher = CLIContainer.getInstance().getEngine().getMatcher(
                    DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario.instance());
            Optional<DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario.Match> match2 = matcher.getOneArbitraryMatch(partialMatch2);
            if(match2.isPresent()) {
                return match2.get().getDaySchedule().getName() + " " +
                        fromMinutesToHHmm(match2.get().getStarttime()) + "-" +
                        fromMinutesToHHmm(match2.get().getEndtime());
            } else {
                return "none"; //Should never happen!
            }
        }
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

    public static String timeTimeRangePrettyString(TimeRange timeRange) {
        if(timeRange == null) {
            return "";
        }
        if(timeRange instanceof DayScheduleTimeRange) {
            return fromMinutesToHHmm(timeRange.getStart()) + "-" +
                    fromMinutesToHHmm(timeRange.getEnd());
        } else {
            return timeRange.getDaySchedule().getName() + " " +
                    fromMinutesToHHmm(timeRange.getStart()) + "-" +
                    fromMinutesToHHmm(timeRange.getEnd());
        }
    }
}