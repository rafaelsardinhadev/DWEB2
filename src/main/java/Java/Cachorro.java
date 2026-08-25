package Java;

public class Cachorro extends Pet {

    private String raca;
    private String nome;

    public Cachorro(String raca, String nome) {
        super(raca, nome);
        this.raca = raca;
        this.nome = nome;
    }

    protected String latir() {
        return "au au";
    }

}
