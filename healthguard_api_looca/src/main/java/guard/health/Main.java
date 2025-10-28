package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException, SQLException {
        Looca looca = new Looca();
        ConexaoBanco banco = new ConexaoBanco();
        Scanner scanner = new Scanner(System.in);

        System.out.println("""
                +---------------------------------------------------------------------+
                |                       MONITORAMENTO DE REDE                         |
                +---------------------------------------------------------------------+
                |     _    _            _ _   _        _____                     _\s   |
                |    | |  | |          | | | | |      / ____|                   | |   |
                |    | |__| | ___  __ _| | |_| |__   | |  __ _   _  __ _ _ __ __| |   |
                |    |  __  |/ _ \\/ _` | | __| '_ \\  | | |_ | | | |/ _` | '__/ _` |   |
                |    | |  | |  __/ (_| | | |_| | | | | |__| | |_| | (_| | | | (_| |   |
                |    |_|  |_|\\___|\\__,_|_|\\__|_| |_|  \\_____|\\__,_|\\__,_|_|  \\__,_|   |
                |                                                                     |
                +---------------------------------------------------------------------+
                |Digite o código de identificação do DAC:                             |""");

        String codigoDac = scanner.nextLine();
        banco.iniciarCaptura(codigoDac);

        long enviadosAnteriores = -1;
        long recebidosAnteriores = -1;
        String ipMonitorado = "";
        Boolean primeiraExecucao = true;
        boolean conexao = false;

        while (true) {
            List<RedeInterface> interfaces = looca.getRede().getGrupoDeInterfaces().getInterfaces();
            // a cada ciclo ira atualizar todos os dados puxados
            // agora faço um ranking e o que tiver a maior quantidade de bytes será insirido nas variaveis a baixo
            RedeInterface melhorCandidata = null;
            long maxBytes = -1;

            // Este loop agora serve APENAS para avaliar e encontrar a "melhor" interface e atribui nas variaveis acima
            for (RedeInterface rede : interfaces) {
                List<String> ipv4 = rede.getEnderecoIpv4();

                // esse if faz o for pular sua vez caso o ipv4 esteja vazio
                if (ipv4.isEmpty()) {
                    continue;
                }

                String ip = ipv4.get(0);
                // aqui, é para fazer ficar nas variaveis a rede que tiver mais bytes e ja faz uma validação eliminando as q não são internet
                if (ip.startsWith("172.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {

                    if (rede.getBytesRecebidos() > maxBytes) {
                        maxBytes = rede.getBytesRecebidos();
                        melhorCandidata = rede;
                    }
                }
            }

            if (melhorCandidata != null) {
                // Se o ranking encontrou um vencedor, executamos a lógica de monitoramento.
                String ip = melhorCandidata.getEnderecoIpv4().get(0);
                long enviados = melhorCandidata.getBytesEnviados();
                long recebidos = melhorCandidata.getBytesRecebidos();

                if (enviadosAnteriores == -1 || !ip.equals(ipMonitorado)) {
                    if (primeiraExecucao) {
                        System.out.println("Rede (" + ip + ") Encontrada. Iniciando monitoramento.");
                        primeiraExecucao = false;
                    } else {
                        System.out.println("Rede desconectada - Tentando reconectar-se...");
                    }
                    conexao = true;
                    enviadosAnteriores = enviados;
                    recebidosAnteriores = recebidos;
                    ipMonitorado = ip;
                } else {
                    if (enviados > enviadosAnteriores || recebidos > recebidosAnteriores) {
                        System.out.println("Rede (" + ip + ") está conectada. Houve tráfego.");
                        conexao = true;
                        enviadosAnteriores = enviados;
                        recebidosAnteriores = recebidos;
                    } else {
                        System.out.println("ALERTA: Rede Desconectada (sem tráfego).");
                        conexao = false;
                        enviadosAnteriores = -1;
                        recebidosAnteriores = -1;
                        ipMonitorado = "";
                    }
                }
            } else {
                // Se, após o ranking, nenhuma interface foi escolhida, significa que não há conexão.
                if (enviadosAnteriores != -1) {
                    System.out.println("Nenhuma interface de Rede Local ativa foi encontrada. Conexão perdida.");
                    conexao = false;
                    enviadosAnteriores = -1;
                    recebidosAnteriores = -1;
                    ipMonitorado = "";
                }
            }
            try {
                // A variável 'conexao' tem o status final e está pronta para ser usada.
                banco.inserirBanco(conexao);
            } catch (Exception e) {
                System.out.println("tentando re-conectar com banco de dados...");

            }

            Thread.sleep(5000);
        }
    }
}