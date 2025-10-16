package guard.health;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
    public void inserirBanco(Boolean conexaoAtiva) throws SQLException {
        String sql = "INSERT INTO HealthGuard (dataCapturada, medidaCapturada) VALUES (?, ?)";

        Connection conn = this.conexao.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setBoolean(2, conexaoAtiva);

        stmt.executeUpdate();

        stmt.close();
        conn.close();

        System.out.println("Inserido com sucesso ");
    }
}

