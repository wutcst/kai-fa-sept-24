package cn.edu.whut.sept.zuul;

public class LookCommand extends Command
{
    public boolean execute(Game game)
    {
        if(hasSecondWord()) {
            System.out.println("Look does not need another word.");
        } else {
            System.out.println(game.getCurrentRoom().getLongDescription());
        }
        return false;
    }
}
