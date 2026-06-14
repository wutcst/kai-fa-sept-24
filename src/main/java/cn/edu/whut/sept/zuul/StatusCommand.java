package cn.edu.whut.sept.zuul;

public class StatusCommand extends Command
{
    public boolean execute(Game game)
    {
        if(hasSecondWord()) {
            System.out.println("Status does not need another word.");
        } else {
            System.out.println(game.getStatusReport());
        }
        return false;
    }
}
