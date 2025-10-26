package guard.health;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.reactive.AbstractReactiveTransactionManager;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class ConexaoBanco {
    private DataSource conexao;

    private int idDac;
    private int fkUnidadeDeAtendimento;
    private int fkMedicoesSelecionadas;
    private int fkMedicoesDisponiveis;
    private String nomeDac;

    public ConexaoBanco() {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        driver.setUsername("logan");
        driver.setPassword("senha-segura123");
        driver.setUrl("jdbc:mysql://44.199.59.133:3306/HealthGuard");
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
        String sqlDac = "SELECT idDac, fkUnidadeDeAtendimento, nomeIdentificacao FROM Dac WHERE codigoValidacao = ?";
        String sqlMedicoes = "SELECT ms.idMedicoesSelecionadas AS fkMedicoesSelecionadas,ms.fkMedicoesDisponiveis AS fkMedicoesDisponiveis FROM MedicoesSelecionadas ms WHERE ms.fkDac = ? LIMIT 1";

        String sqlNomeUnidade = "SELECT ua.nomeFantasia FROM UnidadeDeAtendimento ua JOIN Dac d ON ua.idUnidadeDeAtendimento = d.fkUnidadeDeAtendimento WHERE d.codigoValidacao = ?";

        try (Connection conn = conexao.getConnection();
             PreparedStatement stmtDac = conn.prepareStatement(sqlDac)) {

            stmtDac.setString(1, codigoValidacao);
            ResultSet rsDac = stmtDac.executeQuery();

            if (rsDac.next()) {
                this.idDac = rsDac.getInt("idDac");
                this.fkUnidadeDeAtendimento = rsDac.getInt("fkUnidadeDeAtendimento");
                this.nomeDac = rsDac.getString("nomeIdentificacao");

                String nomeUnidade = "Não encontrada";
                try (PreparedStatement stmtUnidade = conn.prepareStatement(sqlNomeUnidade)) {
                    stmtUnidade.setString(1, codigoValidacao);
                    ResultSet rsUnidade = stmtUnidade.executeQuery();

                    if (rsUnidade.next()) {
                        nomeUnidade = rsUnidade.getString("nomeFantasia");
                    }
                }

                System.out.printf("""
                    
                    
                    
                    MONITORAMENTO DE REDE | HEALTHGUARD
                    +--------------------------------------------+
                    |MAQUINA: %s
                    |UNIDADE: %s
                    +____________________________________________
                    |Iniciando captura de medições...
                    %n""", nomeDac, nomeUnidade);


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
                tentarNovamente();
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar informações do DAC e medições.");
            e.printStackTrace();
        }
    }
    public void tentarNovamente() {
        Scanner scanner1 = new Scanner(System.in);
        System.out.println("insira o codigo de validação novamente: ");
         String codigo =scanner1.nextLine();
        iniciarCaptura(codigo);
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
