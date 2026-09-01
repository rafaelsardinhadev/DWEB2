package Java;

/**
 * Classe base dos animais do cadastro.
 */
public abstract class Pet {

    private int id;
    private String nome;
    private String raca;

    public Pet(String nome, String raca) {
        this.nome = nome;
        this.raca = raca;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    /*
    * Tipo do animal exibido na tela (Cachorro, Gato, Coelho...).
    */
    public String getTipo() {
        return getClass().getSimpleName();
    }

    /**
     *  Som que o animal emite. Cada subclasse define o seu.
     */
    public abstract String getSom();
}
