package Java;

public class Coelho extends Pet {

    public Coelho(String nome, String raca) {
        super(nome, raca);
    }

    @Override
    public String getSom() {
        return "Squeak";
    }
}
