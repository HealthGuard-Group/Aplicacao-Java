package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Looca looca = new Looca();
        Scanner leitorInicial = new Scanner(System.in);

        List<RedeInterface> interfaces = looca.getRede().getGrupoDeInterfaces().getInterfaces();
        Boolean conexao;
        System.out.println("""
                                     _ _   _       ___                     _\s
                     /\\  /\\___  __ _| | |_| |__   / _ \\_   _  __ _ _ __ __| |
                    / /_/ / _ \\/ _` | | __| '_ \\ / /_\\/ | | |/ _` | '__/  _`|
                   / __  /  __/ (_| | | |_| | | / /_\\\\ | |_| | (_| | |  | (_||
                   \\/ /_/ \\___|\\__,_|_|\\__|_| |_\\____/ \\__,_|\\__,_|_|  \\__,_|
                
                    Verificação de Rede...
                __________________________________________________________________
                """);

        // Variáveis para guardar o estado da captura anterior.
        long enviadosAnteriores = -1;
        long recebidosAnteriores = -1;
        String ipMonitorado = "";
        Boolean primeiraExecucao = true;
        while(true) {
            boolean redeConectada = false;

            searchLoop:
            for (int i = 0; i < interfaces.size(); i++) {
                RedeInterface rede = interfaces.get(i);
                List<String> ipv4 = rede.getEnderecoIpv4();

                for (int i1 = 0; i1 < ipv4.size(); i1++) {
                    String ip = ipv4.get(i1);

                    if (ip.startsWith("172.") || ip.startsWith("192.168.") || ip.startsWith("10.") ) {
                        redeConectada = true;
                        long enviados = rede.getBytesEnviados();
                        long recebidos = rede.getBytesRecebidos();

                        if ((enviadosAnteriores == -1 || !ip.equals(ipMonitorado)) && primeiraExecucao) {

                            System.out.println("Rede (" + ip + ") Encontrada. Iniciando monitoramento.");
                            primeiraExecucao = false;
                            enviadosAnteriores = enviados;
                            recebidosAnteriores = recebidos;
                            ipMonitorado = ip;

                        }  else if(enviadosAnteriores == -1 || !ip.equals(ipMonitorado)) {
                            System.out.println("Rede (" + ip + ")desconectada - Tentando reconectar-se... ");
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
                                System.out.println("ALERTA: Rede (" + ip + ") está Desconectada.");
                                conexao = false;
                                 enviadosAnteriores = -1;
                                 recebidosAnteriores = -1;
                                 ipMonitorado = "";
                            }
                        }


                        break searchLoop; // esse searchLoop no break faz os dois for pararem
                    }
                }
            }


            if (!redeConectada) {

                if (enviadosAnteriores != -1) {
                    System.out.println("Nenhuma interface de Rede Local ativa foi encontrada. Conexão perdida.");
                    enviadosAnteriores = -1;
                    recebidosAnteriores = -1;
                    ipMonitorado = "";
                }
            }

            Thread.sleep(5000);
            // inserir no banco dentro desta chave
            // para a cada captura, mandar ao banco a variavel conexao(boleana) e ter no banco cada status de conexao
        }
    }
}
