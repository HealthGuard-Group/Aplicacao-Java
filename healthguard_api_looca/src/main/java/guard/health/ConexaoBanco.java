package guard.health;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ConexaoBanco{
    private final JdbcTemplate jdbcTemplate;
    private  final BasicDataSource basicDataSource;

    public ConexaoBanco(){
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl("jdbc:h2:mem:filmes");
        basicDataSource.setUsername("sa");
        basicDataSource.setPassword("");

        this.basicDataSource = basicDataSource;
        this.jdbcTemplate = new JdbcTemplate(basicDataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public BasicDataSource getBasicDataSource() {
        return basicDataSource;
    }
}
