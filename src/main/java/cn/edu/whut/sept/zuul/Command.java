package cn.edu.whut.sept.zuul;

/**
 * 表示玩家输入的一条命令。
 */
public class Command {
    private final String commandWord;
    private final String secondWord;

    /**
     * 创建命令。
     *
     * @param firstWord 命令词
     * @param secondWord 命令参数
     */
    public Command(String firstWord, String secondWord) {
        commandWord = firstWord;
        this.secondWord = secondWord;
    }

    /**
     * 获取命令词。
     *
     * @return 命令词
     */
    public String getCommandWord() {
        return commandWord;
    }

    /**
     * 获取第二个单词。
     *
     * @return 命令参数
     */
    public String getSecondWord() {
        return secondWord;
    }

    /**
     * 判断命令是否未知。
     *
     * @return 命令词为空时返回 true
     */
    public boolean isUnknown() {
        return commandWord == null;
    }

    /**
     * 判断命令是否带参数。
     *
     * @return 有第二个单词时返回 true
     */
    public boolean hasSecondWord() {
        return secondWord != null;
    }
}
