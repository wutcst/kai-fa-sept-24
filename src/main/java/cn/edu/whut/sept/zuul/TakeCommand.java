package cn.edu.whut.sept.zuul;

public class TakeCommand extends Command
{
    public boolean execute(Game game)
    {
        if(!hasSecondWord()) {
            System.out.println("Take what?");
            return false;
        }

        Game.TakeResult result = game.takeItem(getSecondWord());
        if(result.isTaken()) {
            System.out.println("You picked up " + result.getItem().getName() + ".");
        } else if(result.isTooHeavy()) {
            System.out.println(result.getItem().getName() + " is too heavy to carry.");
        } else {
            System.out.println("There is no " + getSecondWord() + " here.");
        }
        return false;
    }
}
