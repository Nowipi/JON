public class Main {

    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException, InstantiationException {

        JON jon = new JON();

        Cow cow = new Cow("Bob", 15);

        String cowJon = jon.toJON(cow);

        Cow desCow = jon.fromJON(cowJon, Cow.class);

        System.out.println(desCow.getName());

    }

}
