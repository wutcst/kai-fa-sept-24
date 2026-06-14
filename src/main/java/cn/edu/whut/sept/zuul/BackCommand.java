package cn.edu.whut.sept.zuul;

public class BackCommand extends Command
{
    public boolean execute(Game game)
    {
        if(hasSecondWord()) {
            System.out.println("Back does not need another word.");
        } else if(game.goBack()) {
            System.out.println(game.getCurrentRoom().getLongDescription());
        } else {
            System.out.println("You are already at the starting point.");
        }
        return false;
    }
}
