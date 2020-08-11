//import com.vanderhighway.trbac.patterns.RoleName;
//import org.beryx.textio.TextTerminal;
//import org.eclipse.viatra.query.runtime.api.IMatchUpdateListener;
//
//public class TerminalListenerFactory {
//
//    public static IMatchUpdateListener<RoleName.Match> getTerminalRoleNameMatchUpdateListener(TextTerminal terminal) {
//        return new IMatchUpdateListener<RoleName.Match>() {
//            @Override
//            public void notifyAppearance(RoleName.Match match) {
//                terminal.printf("[ADD RoleName Match] %s %n", match.prettyPrint());
//            }
//
//            @Override
//            public void notifyDisappearance(RoleName.Match match) {
//                terminal.printf("[REM RoleName Match] %s %n", match.prettyPrint());
//            }
//        };
//    }
//
//}
