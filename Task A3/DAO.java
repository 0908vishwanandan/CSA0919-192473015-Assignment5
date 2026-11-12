import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {

    void insert(T entity) throws SQLException;

    Optional<T> findById(K id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean deleteById(K id) throws SQLException;
}
