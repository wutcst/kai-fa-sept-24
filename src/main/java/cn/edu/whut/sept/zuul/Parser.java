package cn.edu.whut.sept.zuul;

import java.util.Collection;
import java.util.Scanner;

/**
 * 解析玩家在终端输入的命令。
 */
public class Parser {
    private final CommandWords commands;
    private final Scanner reader;

    /**
     * 创建解析器。
     *
     * @param commandWords 游戏支持的命令词
     */
    public Parser(Collection<String> commandWords) {
        commands = new CommandWords(commandWords);
        reader = new Scanner(System.in);
    }

    /**
     * 从终端读取并解析命令。
     *
     * @return 解析后的命令对象
     */
    public Command getCommand() {
        String word1 = null;
        String word2 = null;

        System.out.print("> ");
        String inputLine = reader.nextLine();

        Scanner tokenizer = new Scanner(inputLine);
        if (tokenizer.hasNext()) {
            word1 = tokenizer.next();
            if (tokenizer.hasNext()) {
                word2 = tokenizer.next();
            }
        }
        tokenizer.close();

        if (commands.isCommand(word1)) {
            return new Command(word1, word2);
        }
        return new Command(null, word2);
    }

    /**
     * 输出当前支持的命令。
     */
    public void showCommands() {
        commands.showAll();
    }
}
