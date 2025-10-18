package guard.health;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

public class ConexaoBanco {
    private DataSource conexao;

    private int idDac;
    private int fkUnidadeDeAtendimento;
    private int fkMedicoesSelecionadas;
    private int fkMedicoesDisponiveis;
    private String nomeDac;

    public ConexaoBanco() {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        driver.setUsername("USUARIO");
        driver.setPassword("SENHA");
        driver.setUrl("jdbc:mysql://localhost:3306/HealthGuard");
        driver.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.conexao = driver;
    }

    public boolean verificarConexao() {
        try (Connection verConexao = conexao.getConnection()) {
            if (verConexao != null && !verConexao.isClosed()) {
                System.out.println("Conexão com o banco realizada com sucesso!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Falha ao conectar com o banco de dados.");
            e.printStackTrace();
        }
        return false;
    }

    public void iniciarCaptura(String codigoValidacao) {
        String sqlDac = "SELECT idDac, fkUnidadeDeAtendimento, nomeDeIdentificacao FROM Dac WHERE codigoValidacao = ?";

        String sqlMedicoes = "SELECT ms.idMedicoesSelecionadas AS fkMedicoesSelecionadas,ms.fkMedicoesDisponiveis AS fkMedicoesDisponiveis FROM MedicoesSelecionadas ms WHERE ms.fkDac = ? LIMIT 1";

        try (Connection conn = conexao.getConnection();
             PreparedStatement stmtDac = conn.prepareStatement(sqlDac)) {

            stmtDac.setString(1, codigoValidacao);
            ResultSet rsDac = stmtDac.executeQuery();

            if (rsDac.next()) {
                this.idDac = rsDac.getInt("idDac");
                this.fkUnidadeDeAtendimento = rsDac.getInt("fkUnidadeDeAtendimento");
                this.nomeDac = rsDac.getString("nomeDeIdentificacao");

                System.out.println("Olá, " + nomeDac + "! Iniciando captura de medições...");

                try (PreparedStatement stmtMedicoes = conn.prepareStatement(sqlMedicoes)) {
                    stmtMedicoes.setInt(1, idDac);
                    ResultSet rsMedicoes = stmtMedicoes.executeQuery();

                    if (rsMedicoes.next()) {
                        this.fkMedicoesSelecionadas = rsMedicoes.getInt("fkMedicoesSelecionadas");
                        this.fkMedicoesDisponiveis = rsMedicoes.getInt("fkMedicoesDisponiveis");

                    }
                }

            } else {
                System.out.println("Código de validação não encontrado no banco.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar informações do DAC e medições.");
            e.printStackTrace();
        }
    }

    public void inserirBanco(boolean conexaoAtiva) {
        String sql = "INSERT INTO Leitura (fkMedicoesDisponiveis, fkMedicoesSelecionadas, fkDac, fkUnidadeDeAtendimento, medidaCapturada, dataCaptura) VALUES (?, ?, ?, ?, ?, ?)";

        String medida = conexaoAtiva ? "Conectado" : "Desconectado";

        try (Connection conn = conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fkMedicoesDisponiveis);
            stmt.setInt(2, fkMedicoesSelecionadas);
            stmt.setInt(3, idDac);
            stmt.setInt(4, fkUnidadeDeAtendimento);
            stmt.setString(5, medida);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().now()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(" Erro ao inserir leitura no banco de dados.");
            e.printStackTrace();
        }
    }


    public int getIdDac() {
        return idDac;
    }

    public int getFkUnidadeDeAtendimento() {
        return fkUnidadeDeAtendimento;
    }

    public int getFkMedicoesSelecionadas() {
        return fkMedicoesSelecionadas;
    }

    public int getFkMedicoesDisponiveis() {
        return fkMedicoesDisponiveis;
    }

    public String getNomeDac() {
        return nomeDac;
    }
}
