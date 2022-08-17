public class Bel {

    private final Cow owner;
    private String sound;

    public Bel(Cow owner, String sound) {
        this.owner = owner;
        this.sound = sound;
    }

    public Cow getOwner() {
        return owner;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
