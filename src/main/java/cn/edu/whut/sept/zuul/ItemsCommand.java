package cn.edu.whut.sept.zuul;

public class ItemsCommand extends Command
{
    public boolean execute(Game game)
    {
        if(hasSecondWord()) {
            System.out.println("Items does not need another word.");
        } else {
            System.out.println(game.getCurrentRoom().getLongDescription());
            System.out.println(game.getPlayer().getInventoryDescription());
        }
        return false;
    }
}
