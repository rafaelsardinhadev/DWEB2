package Java;

public class Cachorro extends Pet {

    public Cachorro(String nome, String raca) {
        super(nome, raca);
    }

    @Override
    public String getSom() {
        return "Au au";
    }
}
