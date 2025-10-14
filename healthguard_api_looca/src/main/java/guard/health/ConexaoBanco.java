package guard.health;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConexaoBanco {
    private DataSource conexao;

    public ConexaoBanco() {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        driver.setUsername("USUARIO");
        driver.setPassword("SENHA");
        driver.setUrl("jdbc:mysql://localhost:3306/NOME-BANCO");
        driver.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.conexao = driver;

    }

    public DataSource getConexao() {
        return this.conexao;
    }

    public boolean verificarConexao() {
        try (Connection verConexao = conexao.getConnection()) {
            if (verConexao != null && !verConexao.isClosed()) {
                System.out.println("Conexão realizada com sucesso no banco de dados!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Falha ao conectar com o banco de dados");
            e.printStackTrace();
        }
        return false;
    }
}

