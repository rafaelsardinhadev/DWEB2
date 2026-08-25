package Java;

public class Coelho extends Pet {

    private String raca;
    private String nome;

    public Coelho(String raca, String nome) {
        super(raca, nome);
        this.raca = raca;
        this.nome = nome;
    }

    protected String latir() {
        return "miau";
    }
}
