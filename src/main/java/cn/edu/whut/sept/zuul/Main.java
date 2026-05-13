package cn.edu.whut.sept.zuul;

/**
 * 游戏入口。
 */
public class Main {
    /**
     * 启动 World-of-Zuul 游戏。
     *
     * @param args 命令行参数，本程序暂不使用
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}
