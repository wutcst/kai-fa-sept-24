package cn.edu.whut.sept.zuul;

public class EatCommand extends Command
{
    public boolean execute(Game game)
    {
        if(!hasSecondWord()) {
            System.out.println("Eat what?");
            return false;
        }

        if(!getSecondWord().equals("cookie")) {
            System.out.println("You cannot eat " + getSecondWord() + ".");
            return false;
        }

        Item cookie = game.eatCookie();
        if(cookie == null) {
            System.out.println("You do not have a magic cookie.");
        } else {
            System.out.println("You eat the magic cookie. Your carry limit is now "
                    + game.getPlayer().getMaxCarryWeight() + "kg.");
        }
        return false;
    }
}
