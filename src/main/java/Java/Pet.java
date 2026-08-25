package Java;

public class Pet {

    private String raca;
    private String nome;

    public Pet(String raca, String nome) {
        this.raca = raca;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getRaca() {
        return raca;
    }

    protected String latir() {
        return "";
    }
}
