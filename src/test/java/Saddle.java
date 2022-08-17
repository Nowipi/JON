public class Saddle {

    private final Leather leather;
    private final Bel bel;

    private final Cow owner;

    public Saddle(Cow owner) {
        this.owner = owner;
        leather = new Leather("good");
        bel = new Bel(owner, "ding");
    }

    public Leather getLeather() {
        return leather;
    }

    public Bel getBel() {
        return bel;
    }

    public Cow getOwner() {
        return owner;
    }
}
