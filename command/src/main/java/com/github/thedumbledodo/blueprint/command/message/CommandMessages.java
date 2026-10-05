package com.github.thedumbledodo.blueprint.command.message;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommandMessages {

    private String playerOnlyCommand = "<#fa4943>This can only be done as a player!";
    private String noPermission = "<#fa4943>No permission to execute this command.";
    private String invalidSyntax = "<#fa4943>Invalid command syntax!";
    private String unknownCommand = "<#fa4943>There is no command like that.";

    private String playerNotFound = "<#fa4943>Player not found!";
    private String notFound = "<#fa4943>Could not find <white><input></white>.";

    private String invalidArgument = "<#fa4943>Invalid value <white><input></white>.";
    private String invalidChoice = "<#fa4943><white><input></white> is not valid. Try one of: <white><values>";

    private String commandError = "<#fa4943>An error occurred while executing this command!";
}
