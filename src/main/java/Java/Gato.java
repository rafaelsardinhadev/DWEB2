package Java;

public class Gato extends Pet {

    public Gato(String nome, String raca) {
        super(nome, raca);
    }

    @Override
    public String getSom() {
        return "Miau";
    }
}
