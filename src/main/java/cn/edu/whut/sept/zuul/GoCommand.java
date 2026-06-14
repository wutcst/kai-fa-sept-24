package cn.edu.whut.sept.zuul;

public class GoCommand extends Command
{
    public boolean execute(Game game)
    {
        if(!hasSecondWord()) {
            System.out.println("Go where?");
            return false;
        }

        String direction = getSecondWord();

        if(!game.goRoom(direction)) {
            System.out.println("There is no door!");
        }
        else if(!game.isFinished()) {
            System.out.println(game.getCurrentRoom().getLongDescription());
        }

        return false;
    }
}
