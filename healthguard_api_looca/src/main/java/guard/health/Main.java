package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException, SQLException {
        Looca looca = new Looca();
        ConexaoBanco banco = new ConexaoBanco();
        SlackNotifier slack = new SlackNotifier(
                "https://hooks.slack.com/services/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"
        );
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
        boolean primeiraExecucao = true;
        boolean conexao = false;
        boolean enviouConectado = false;

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
                        System.out.println("Rede (" + ip + ") encontrada. Iniciando monitoramento.");
                        primeiraExecucao = false;
                    } else {
                        System.out.println("Reconectado na rede.");
                        slack.enviarMensagem(
                                ":white_check_mark: *Rede voltou!*\n" +
                                        "Maquina: `" + banco.getNomeDac() + "`"
                        );
                    }
                    conexao = true;
                    enviouConectado = false; 
                    enviadosAnteriores = enviados;
                    recebidosAnteriores = recebidos;
                    ipMonitorado = ip;
                } else {
                    if (enviados > enviadosAnteriores || recebidos > recebidosAnteriores) {
                        System.out.println("Rede (" + ip + ") está conectada. Houve tráfego.");
                        conexao = true;

                        if (!enviouConectado) {
                            slack.enviarMensagem(
                                    ":information_source: *Rede conectada*\n" +
                                            "Maquina: `" + banco.getNomeDac() + "`\n" +
                                            "IP: `" + ip + "`"
                            );
                            enviouConectado = true;
                        }

                        enviadosAnteriores = enviados;
                        recebidosAnteriores = recebidos;
                    } else {
                        System.out.println("ALERTA: Rede desconectada (sem tráfego).");
                        conexao = false;
                        enviouConectado = false; 
                        enviadosAnteriores = -1;
                        recebidosAnteriores = -1;
                        ipMonitorado = "";

                        slack.enviarMensagem(
                                ":rotating_light: *ALERTA! Rede caiu*\n" +
                                        "Maquina: `" + banco.getNomeDac() + "`"
                        );
                    }
                }
            } else {
                if (enviadosAnteriores != -1) {
                    System.out.println("Nenhuma interface de Rede Local ativa foi encontrada. Conexão perdida.");
                    conexao = false;
                    enviouConectado = false;
                    enviadosAnteriores = -1;
                    recebidosAnteriores = -1;
                    ipMonitorado = "";

                    slack.enviarMensagem(
                            ":rotating_light: *Rede completamente perdida!*\n" +
                                    "Maquina: `" + banco.getNomeDac() + "`"
                    );
                }
            }

            try {
                banco.inserirBanco(conexao);
            } catch (Exception e) {
                System.out.println("Tentando reconectar com banco de dados...");
            }

            Thread.sleep(5000);
        }
    }
}
