package cn.edu.whut.sept.zuul;

import java.util.Scanner;

public class Parser
{
    private CommandWords commands;  // holds all valid command words
    private Scanner reader;         // source of command input

    public Parser()
    {
        commands = new CommandWords();
        reader = new Scanner(System.in);
    }

    public Command getCommand()
    {
        String inputLine;   // will hold the full input line
        String word1 = null;
        String argument = null;

        System.out.print("> ");     // print prompt

        inputLine = reader.nextLine();

        inputLine = inputLine.trim();
        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();      // get first word
            int argumentStart = word1.length();
            if(inputLine.length() > argumentStart) {
                argument = inputLine.substring(argumentStart).trim();
            }
        }

        Command command = commands.get(word1);
        if(command != null) {
            command.setArgument(argument);
        }
        return command;
    }

    public void showCommands()
    {
        commands.showAll();
    }
}
