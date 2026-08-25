package Java;

public class Gato extends Pet{

    private String raca;
    private String nome;

    public Gato(String raca, String nome) {
        super(raca, nome);
        this.raca = raca;
        this.nome = nome;
    }

    protected String latir() {
        return "miau";
    }

}
