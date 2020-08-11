/*******************************************************************************
 * Copyright (c) 2010-2015, Zoltan Ujhelyi, Gabor Szarnyas
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

public class PolicyValidatorCLIOld {
//	public static void main(String[] args) throws IOException {
//
//		//Debug output
//		//BasicConfigurator.configure();
//
//		TextIO textIO = TextIoFactory.getTextIO();
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//
//		terminal.setBookmark("fullclear");
//
//		terminal.println("  ____       _ _             ____ _               _             \n" +
//				" |  _ \\ ___ | (_) ___ _   _ / ___| |__   ___  ___| | _____ _ __ \n" +
//				" | |_) / _ \\| | |/ __| | | | |   | '_ \\ / _ \\/ __| |/ / _ \\ '__|\n" +
//				" |  __/ (_) | | | (__| |_| | |___| | | |  __/ (__|   <  __/ |   \n" +
//				" |_|   \\___/|_|_|\\___|\\__, |\\____|_| |_|\\___|\\___|_|\\_\\___|_|   \n" +
//				"                      |___/                                     ");
//		terminal.println("version 0.0.1");
//		terminal.print("Initialize model scope and preparing engine... ");
//
//		// Initializing the EMF package
//		TRBACPackage.eINSTANCE.getName();
//		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
//		Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());
//
//		ResourceSet set = new ResourceSetImpl();
//		//URI uri = URI.createFileURI("models/basic/empty.trbac");
//		URI uri = URI.createFileURI("com.vanderhighway.trbac.verifier/models/basic/empty.trbac");
//		set.getResource(uri, true);
//
//		final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//
//		terminal.println("done!");
//
//		terminal.println("model entities: XXX, model relations: XXXX");
//		terminal.println("type 'help' to get more information a each specific prompt");
//		terminal.println("\n");
//		PolicyValidator validator = new PolicyValidator(engine);
//		validator.addChangeListeners(engine);
//		PolicyModifier modifier = new PolicyModifier(engine);
//
//		PolicyValidatorCLI.addTerminalChangeListeners(engine, terminal);
//
//		//validator.initialize();
//		//validator.execute();
//		//validator.dispose();
//
//		//SwingTextTerminal terminalswing = (SwingTextTerminal) terminal;
//		//terminalswing.setPromptFontSize(20);
//		//terminalswing.setInputFontSize(20);
//
//		boolean editing = true;
//		while (editing) {
//			boolean commandGiven = false;
//			List<String> commands;
//			while (!commandGiven) {
//
//				commands = new LinkedList<>(Arrays.asList(textIO.newStringInputReader()
//						.read("command").split(" ")));
//				String command = commands.get(0);
//				commands.remove(0);
//
//
//				switch (command) {
//					case "add":
//						PolicyValidatorCLI.processAddCommand(textIO, modifier, commands);
//						commandGiven = true;
//						break;
//					case "remove":
//						PolicyValidatorCLI.processRemoveCommand(textIO, modifier, commands);
//						commandGiven = true;
//						break;
//					case "assign":
//						processAssignCommand(textIO, modifier, commands);
//						commandGiven = true;
//						break;
//					case "deassign":
//						//processDeassignCommand(textIO, modifier, commands);
//						commandGiven = true;
//						break;
//					case "help":
//						terminal.println("an usefull help message");
//						commandGiven = true;
//						break;
//					case "clear":
//						terminal.resetToBookmark("fullclear");
//						break;
//					default:
//						terminal.println("unrecognized command:" + command);
//						break;
//				}
//				terminal.println("");
//			}
//		}
//
//
//		modifier.dispose();
//
//		System.out.println("Done!");
//	}
//
//
//	public static void processAddCommand(TextIO textIO, PolicyModifier modifier, List<String> commands){
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//		boolean entityTypeGiven = false;
//		while (!entityTypeGiven) {
//			String entityType = requestInput(textIO, commands, "type");
//			switch (entityType) {
//				case "user":
//					modifier.execute(modifier.addUser(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "role":
//					modifier.execute(modifier.addRole(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "demarcation":
//					modifier.execute(modifier.addDemarcation(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "permission":
//					modifier.execute(modifier.addPermission(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				default:
//					terminal.println("unrecognized entity type:" + entityType);
//					break;
//			}
//		}
//	}
//
//	public static void processRemoveCommand(TextIO textIO, PolicyModifier modifier, List<String> commands){
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//		boolean entityTypeGiven = false;
//		while (!entityTypeGiven) {
//			String entityType = requestInput(textIO, commands, "type");
//			switch (entityType) {
//				case "user":
//					modifier.execute(modifier.removeUser(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "role":
//					modifier.execute(modifier.removeRole(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "demarcation":
//					modifier.execute(modifier.removeDemarcation(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				case "permission":
//					modifier.execute(modifier.removePermission(requestInput(textIO, commands, "name")));
//					entityTypeGiven = true;
//					break;
//				default:
//					terminal.println("unrecognized entity type:" + entityType);
//					break;
//			}
//		}
//	}
//
//	public static void processAssignCommand(TextIO textIO, PolicyModifier modifier, List<String> commands){
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//		boolean entityTypeGiven = false;
//		while (!entityTypeGiven) {
//			String entityType = requestInput(textIO, commands, "type");
//			switch (entityType) {
//				case "role":
//					modifier.execute(modifier.assignRoleToUser(
//							requestInput(textIO, commands, "user name"),
//							requestInput(textIO, commands, "role name")
//					));
//					entityTypeGiven = true;
//					break;
//				case "demarcation":
//					modifier.execute(modifier.assignDemarcationToRole(
//							requestInput(textIO, commands, "role name"),
//							requestInput(textIO, commands, "demarcation name")
//					));
//					entityTypeGiven = true;
//					break;
//				case "permission":
//					modifier.execute(modifier.assignPermissionToDemarcation(
//							requestInput(textIO, commands, "demarcation name"),
//							requestInput(textIO, commands, "permission name")
//					));
//					entityTypeGiven = true;
//					break;
//				default:
//					terminal.println("unrecognized entity type:" + entityType);
//					break;
//			}
//		}
//	}
//
//	public static String requestInput(TextIO textIO, List<String> commands, String prompt){
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//		if(commands.isEmpty()) {
//			return textIO.newStringInputReader()
//					.read(prompt);
//		} else {
//			String input = commands.get(0);
//			commands.remove(0);
//			return input;
//		}
//	}
//
//	public static String requestString(TextIO textIO, String prompt){
//		TextTerminal<?> terminal = textIO.getTextTerminal();
//		return textIO.newStringInputReader()
//				.read(prompt);
//	}
//
//	public static void addTerminalChangeListeners(AdvancedViatraQueryEngine engine, TextTerminal terminal) {
//		//engine.addMatchUpdateListener(RoleName.Matcher.on(engine), TerminalListenerFactory.getTerminalRoleNameMatchUpdateListener(terminal), true);
//	}
}
