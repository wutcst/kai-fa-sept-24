package cn.edu.whut.sept.zuul;

public class DropCommand extends Command
{
    public boolean execute(Game game)
    {
        if(!hasSecondWord()) {
            System.out.println("Drop what?");
            return false;
        }

        Item item = game.dropItem(getSecondWord());
        if(item == null) {
            System.out.println("You are not carrying " + getSecondWord() + ".");
        } else {
            System.out.println("You dropped " + item.getName() + ".");
        }
        return false;
    }
}
