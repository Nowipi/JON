public class Cow {

    private final String name;
    private int age;
    private Saddle saddle;
    private Bel bel;

    public Cow(String name, int age) {
        this.name = name;
        this.age = age;

        saddle = new Saddle(this);
        bel = saddle.getBel();
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Saddle getSaddle() {
        return saddle;
    }

    public void setSaddle(Saddle saddle) {
        this.saddle = saddle;
    }

    public Bel getBel() {
        return bel;
    }

    public void setBel(Bel bel) {
        this.bel = bel;
    }
}
