package cn.edu.whut.sept.zuul;

public abstract class Command
{
    private String secondWord;

    public Command()
    {
        secondWord = null;
    }

    public String getSecondWord()
    {
        return secondWord;
    }

    public boolean hasSecondWord()
    {
        return secondWord != null;
    }

    public void setSecondWord(String secondWord)
    {
        this.secondWord = secondWord;
    }

    public void setArgument(String argument)
    {
        this.secondWord = argument;
    }

    public abstract boolean execute(Game game);
}
