package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Java {
    public static void main(String[] args) throws InterruptedException, SQLException {

        SlackNotifier slack = new SlackNotifier(
                "https://hooks.slack.com/services/SEU/WEBHOOK/AQUI"
        );

        Looca looca = new Looca();
        ConexaoBanco banco = new ConexaoBanco();
        Scanner scanner = new Scanner(System.in);

        System.out.println("""
                +---------------------------------------------------------------------+
                |                       MONITORAMENTO DE REDE                         |
                +---------------------------------------------------------------------+
                |Digite o código de identificação do DAC:                             |""");

        String codigoDac = scanner.nextLine();
        banco.iniciarCaptura(codigoDac);

        long enviadosAnteriores = -1;
        long recebidosAnteriores = -1;
        String ipMonitorado = "";
        boolean primeiraExecucao = true;
        boolean conexao = false;

        while (true) {

            List<RedeInterface> interfaces = looca.getRede().getGrupoDeInterfaces().getInterfaces();
            RedeInterface melhorCandidata = null;
            long maxBytes = -1;

            for (RedeInterface rede : interfaces) {

                List<String> ipv4 = rede.getEnderecoIpv4();
                if (ipv4.isEmpty()) continue;

                String ip = ipv4.get(0);

                if (ip.startsWith("172.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {

                    if (rede.getBytesRecebidos() > maxBytes) {
                        maxBytes = rede.getBytesRecebidos();
                        melhorCandidata = rede;
                    }
                }
            }

            if (melhorCandidata != null) {

                String ip = melhorCandidata.getEnderecoIpv4().get(0);
                long enviados = melhorCandidata.getBytesEnviados();
                long recebidos = melhorCandidata.getBytesRecebidos();

                if (enviadosAnteriores == -1 || !ip.equals(ipMonitorado)) {

                    if (primeiraExecucao) {
                        System.out.println("Rede (" + ip + ") encontrada.");
                        primeiraExecucao = false;
                    } else {
                        System.out.println("Reconectado na rede.");

                        slack.enviarMensagem(
                                ":white_check_mark: *Rede voltou!*\n" +
                                        "Maquina: `" + banco.getNomeDac() + "`"
                        );
                    }

                    conexao = true;
                    enviadosAnteriores = enviados;
                    recebidosAnteriores = recebidos;
                    ipMonitorado = ip;

                } else {
                    if (enviados > enviadosAnteriores || recebidos > recebidosAnteriores) {
                        System.out.println("Rede ativa com tráfego.");
                        conexao = true;
                        enviadosAnteriores = enviados;
                        recebidosAnteriores = recebidos;

                    } else {

                        System.out.println("ALERTA: Rede desconectada.");
                        conexao = false;

                        slack.enviarMensagem(
                                ":rotating_light: *ALERTA! Rede caiu*\n" +
                                        "Maquina: `" + banco.getNomeDac() + "`"
                        );

                        enviadosAnteriores = -1;
                        recebidosAnteriores = -1;
                        ipMonitorado = "";
                    }
                }

            } else {
                if (enviadosAnteriores != -1) {

                    System.out.println("Sem interface de rede ativa.");
                    conexao = false;

                    slack.enviarMensagem(
                            ":rotating_light: *Rede completamente perdida!*\n" +
                                    "Maquina: `" + banco.getNomeDac() + "`"
                    );

                    enviadosAnteriores = -1;
                    recebidosAnteriores = -1;
                    ipMonitorado = "";
                }
            }

            try {
                banco.inserirBanco(conexao);

            } catch (Exception e) {
                System.out.println("Tentando reconectar ao banco...");
            }

            Thread.sleep(5000);
        }
    }
}
