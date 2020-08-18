package picocli.shell.jline3.example;

import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.PrerequisiteURPattern;
import com.vanderhighway.trbac.patterns.PrerequisteURPattern;

public class ConstraintHelper {

    public static String toString(AuthorizationConstraint constraint) {
        String constraintName = constraint.getClass().getSimpleName();
        switch (constraintName) {
            case "SoDURConstraintImpl":
                SoDURConstraint c1 = (SoDURConstraint) constraint;
                return "SoD Users/Roles role1=" + c1.getLeft().getName() + ", role2=" + c1.getRight().getName();
            case "SoDUDConstraintImpl":
                SoDUDConstraint c2 = (SoDUDConstraint) constraint;
                return "SoD Users/Demarcations dem1=" + c2.getLeft().getName() + ", dem2=" + c2.getRight().getName();
            case "SoDUPConstraintImpl":
                SoDUPConstraint c3 = (SoDUPConstraint) constraint;
                return "SoD Users/Permissions perm1=" + c3.getLeft().getName() + ", perm2=" + c3.getRight().getName();
            case "SoDRDConstraintImpl":
                SoDRDConstraint c4 = (SoDRDConstraint) constraint;
                return "SoD Roles/Demarcations dem1=" + c4.getLeft().getName() + ", dem2=" + c4.getRight().getName();
            case "SoDRPConstraintImpl":
                SoDRPConstraint c5 = (SoDRPConstraint) constraint;
                return "SoD Roles/Permissions perm1=" + c5.getLeft().getName() + ", perm2=" + c5.getRight().getName();
            case "SoDDPConstraintImpl":
                SoDDPConstraint c6 = (SoDDPConstraint) constraint;
                return "SoD Demarcations/Permissions perm1=" + c6.getLeft().getName() + ", perm2=" + c6.getRight().getName();

            case "PrerequisiteURConstraintImpl":
                PrerequisiteURConstraint c7 = (PrerequisiteURConstraint) constraint;
                return "Prerequisite Users/Roles role1=" + c7.getLeft().getName() + ", role2=" + c7.getRight().getName();
            case "PrerequisiteUDConstraintImpl":
                PrerequisiteUDConstraint c8 = (PrerequisiteUDConstraint) constraint;
                return "Prerequisite Users/Demarcations dem1=" + c8.getLeft().getName() + ", dem2=" + c8.getRight().getName();
            case "PrerequisiteUPConstraintImpl":
                PrerequisiteUPConstraint c9 = (PrerequisiteUPConstraint) constraint;
                return "Prerequisite Users/Permissions perm1=" + c9.getLeft().getName() + ", perm2=" + c9.getRight().getName();
            case "PrerequisiteRDConstraintImpl":
                PrerequisiteRDConstraint c10 = (PrerequisiteRDConstraint) constraint;
                return "Prerequisite Roles/Demarcations dem1=" + c10.getLeft().getName() + ", dem2=" + c10.getRight().getName();
            case "PrerequisiteRPConstraintImpl":
                PrerequisiteRPConstraint c11 = (PrerequisiteRPConstraint) constraint;
                return "Prerequisite Roles/Permissions perm1=" + c11.getLeft().getName() + ", perm2=" + c11.getRight().getName();
            case "PrerequisiteDPConstraintImpl":
                PrerequisiteDPConstraint c12 = (PrerequisiteDPConstraint) constraint;
                return "Prerequisite Demarcations/Permissions perm1=" + c12.getLeft().getName() + ", perm2=" + c12.getRight().getName();
            default:
                return "unkown constraint";
        }
    }
}
