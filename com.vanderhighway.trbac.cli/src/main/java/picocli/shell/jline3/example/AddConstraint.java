package picocli.shell.jline3.example;

import com.vanderhighway.trbac.model.trbac.model.Demarcation;
import com.vanderhighway.trbac.model.trbac.model.Permission;
import com.vanderhighway.trbac.model.trbac.model.Role;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import picocli.CommandLine;

@CommandLine.Command(name="constraint", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
        description = "Add an authorization constraint")
public class AddConstraint implements Runnable {

    @Override
    public void run() {

    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Roles")
    public void SoDUR(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-role1"}, required = true) String role1Name,
                      @CommandLine.Option(names = {"-role2"}, required = true) String role2Name) throws ModelManipulationException {
        Role role1 = (Role) CLIContainer.getInstance().getModel().getEObject(role1Name);
        Role role2 = (Role) CLIContainer.getInstance().getModel().getEObject(role2Name);
        CLIContainer.getInstance().getModifier().addSoDURConstraint(name, role1, role2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Demarcations ")
    public void SoDUD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                      @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addSoDUDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Users / Permissions ")
    public void SoDUP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addSoDUPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Roles / Demarcations")
    public void SoDRD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                      @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addSoDRDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Roles / Permissions")
    public void SoDRP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addSoDRPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Separation of Duty on the level of Demarcations / Permissions")
    public void SoDDP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addSoDDPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Users / Roles")
    public void PreReqUR(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-role1"}, required = true) String role1Name,
                         @CommandLine.Option(names = {"-role2"}, required = true) String role2Name) throws ModelManipulationException {
        Role role1 = (Role) CLIContainer.getInstance().getModel().getEObject(role1Name);
        Role role2 = (Role) CLIContainer.getInstance().getModel().getEObject(role2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteURConstraint(name, role1, role2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Users / Demarcations ")
    public void PreReqUD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                         @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteUDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Users / Permissions ")
    public void PreReqUP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                         @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteUPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Roles / Demarcations")
    public void PreReqRD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                         @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteRDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Roles / Permissions")
    public void PreReqRP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                         @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteRPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a prerequisite on the level of Demarcations / Permissions")
    public void PreReqDP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                         @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addPrerequisiteDPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Users / Roles")
    public void BoDUR(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-role1"}, required = true) String role1Name,
                      @CommandLine.Option(names = {"-role2"}, required = true) String role2Name) throws ModelManipulationException {
        Role role1 = (Role) CLIContainer.getInstance().getModel().getEObject(role1Name);
        Role role2 = (Role) CLIContainer.getInstance().getModel().getEObject(role2Name);
        CLIContainer.getInstance().getModifier().addBoDURConstraint(name, role1, role2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Users / Demarcations ")
    public void BoDUD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                      @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addBoDUDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Users / Permissions ")
    public void BoDUP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addBoDUPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Roles / Demarcations")
    public void BoDRD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-dem1"}, required = true) String demarcation1Name,
                      @CommandLine.Option(names = {"-dem2"}, required = true) String demarcation2Name) throws ModelManipulationException {
        Demarcation dem1 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation1Name);
        Demarcation dem2 = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcation2Name);
        CLIContainer.getInstance().getModifier().addBoDRDConstraint(name, dem1, dem2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Roles / Permissions")
    public void BoDRP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addBoDRPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a Binding of Duty on the level of Demarcations / Permissions")
    public void BoDDP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                      @CommandLine.Option(names = {"-perm1"}, required = true) String permission1Name,
                      @CommandLine.Option(names = {"-perm2"}, required = true) String permission2Name) throws ModelManipulationException {
        Permission perm1 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission1Name);
        Permission perm2 = (Permission) CLIContainer.getInstance().getModel().getEObject(permission2Name);
        CLIContainer.getInstance().getModifier().addBoDDPConstraint(name, perm1, perm2);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Users / Roles")
    public void CardUR(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-role"}, required = true) String roleName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Role role = (Role) CLIContainer.getInstance().getModel().getEObject(roleName);
        CLIContainer.getInstance().getModifier().addCardinalityURConstraint(name, role, bound);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Users / Demarcations ")
    public void CardUD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-dem"}, required = true) String demarcationName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcationName);
        CLIContainer.getInstance().getModifier().addCardinalityUDConstraint(name, demarcation, bound);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Users / Permissions ")
    public void CardUP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm"}, required = true) String permissionName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
        CLIContainer.getInstance().getModifier().addCardinalityUPConstraint(name, permission, bound);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Roles / Demarcations")
    public void CardRD(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-dem"}, required = true) String demarcationName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Demarcation demarcation = (Demarcation) CLIContainer.getInstance().getModel().getEObject(demarcationName);
        CLIContainer.getInstance().getModifier().addCardinalityRDConstraint(name, demarcation, bound);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Roles / Permissions")
    public void CardRP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm"}, required = true) String permissionName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
        CLIContainer.getInstance().getModifier().addCardinalityRPConstraint(name, permission, bound);
    }

    @CommandLine.Command( mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Add a cardinality on the level of Demarcations / Permissions")
    public void CardDP(@CommandLine.Option(names = {"-name"}, required = true) String name,
                         @CommandLine.Option(names = {"-perm"}, required = true) String permissionName,
                         @CommandLine.Option(names = {"-bound"}, required = true) int bound) throws ModelManipulationException {
        Permission permission = (Permission) CLIContainer.getInstance().getModel().getEObject(permissionName);
        CLIContainer.getInstance().getModifier().addCardinalityDPConstraint(name, permission, bound);
    }
}
