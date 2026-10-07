package com.github.thedumbledodo.blueprint.command.message;

public interface CommandMessages {

    CommandMessages DEFAULT = new CommandMessages() {
    };

    default String getPlayerOnlyCommand() {
        return "<#fa4943>This can only be done as a player!";
    }

    default String getNoPermission() {
        return "<#fa4943>No permission to execute this command.";
    }

    default String getInvalidSyntax() {
        return "<#fa4943>Invalid command syntax!";
    }

    default String getUnknownCommand() {
        return "<#fa4943>There is no command like that.";
    }

    default String getPlayerNotFound() {
        return "<#fa4943>Player not found!";
    }

    default String getNotFound() {
        return "<#fa4943>Could not find <white><input></white>.";
    }

    default String getInvalidArgument() {
        return "<#fa4943>Invalid value <white><input></white>.";
    }

    default String getInvalidChoice() {
        return "<#fa4943><white><input></white> is not valid. Try one of: <white><values>";
    }

    default String getCommandError() {
        return "<#fa4943>An error occurred while executing this command!";
    }
}
