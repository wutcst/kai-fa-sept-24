package cn.edu.whut.sept.zuul;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 保存游戏支持的有效命令词。
 */
public class CommandWords {
    private final Set<String> validCommands;

    /**
     * 根据命令词集合创建命令表。
     *
     * @param commandWords 命令词集合
     */
    public CommandWords(Collection<String> commandWords) {
        validCommands = new LinkedHashSet<>(commandWords);
    }

    /**
     * 判断字符串是否为有效命令。
     *
     * @param commandWord 待检查命令词
     * @return 有效时返回 true
     */
    public boolean isCommand(String commandWord) {
        return validCommands.contains(commandWord);
    }

    /**
     * 输出全部有效命令。
     */
    public void showAll() {
        for (String command : validCommands) {
            System.out.print(command + "  ");
        }
        System.out.println();
    }
}
