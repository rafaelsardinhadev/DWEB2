package Java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * "Banco de dados" em memoria
 */
public class AnimalRepositorio {

    private static final AnimalRepositorio INSTANCIA = new AnimalRepositorio();

    private final List<Pet> animais = new ArrayList<>();
    private final AtomicInteger proximoId = new AtomicInteger(1);

    private AnimalRepositorio() {
    }

    public static AnimalRepositorio getInstancia() {
        return INSTANCIA;
    }

    public synchronized void salvar(Pet pet) {
        pet.setId(proximoId.getAndIncrement());
        animais.add(pet);
    }

    public synchronized List<Pet> listar() {
        return new ArrayList<>(animais);
    }

    public synchronized void remover(int id) {
        animais.removeIf(pet -> pet.getId() == id);
    }
}
