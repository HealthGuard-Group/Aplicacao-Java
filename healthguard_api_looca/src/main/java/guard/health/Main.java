package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Looca looca = new Looca();
        Scanner leitorInicial = new Scanner(System.in);
        List<RedeInterface> interfaces = looca.getRede().getGrupoDeInterfaces().getInterfaces();
        Boolean redeConectada = false;

        System.out.println("""
                                     _ _   _       ___                     _\s
                     /\\  /\\___  __ _| | |_| |__   / _ \\_   _  __ _ _ __ __| |
                    / /_/ / _ \\/ _` | | __| '_ \\ / /_\\/ | | |/ _` | '__/  _`|
                   / __  /  __/ (_| | | |_| | | / /_\\\\ | |_| | (_| | |  | (_||
                   \\/ /_/ \\___|\\__,_|_|\\__|_| |_\\____/ \\__,_|\\__,_|_|  \\__,_|
                
                
                __________________________________________________________________
                """);

        for (int i = 0; i < interfaces.size(); i++) {
            RedeInterface rede = interfaces.get(i);
            List<String> ipv4 = rede.getEnderecoIpv4();

            for (int i1 = 0; i1 < ipv4.size(); i1++) {
                String ip = ipv4.get(i1);
                if (ip.startsWith("192.168")) {
                    long enviados = rede.getBytesEnviados();
                    long recebidos = rede.getBytesRecebidos();

                    if (enviados > 0 || recebidos > 0) {
                        System.out.println(" A rede (" + ip + ") está conectada.");
                        redeConectada = true;
                    } else {
                        System.out.println("A rede local (" + ip + ") não está conectada.");
                    }
                }
            }
        }
        if (!redeConectada) {
            System.out.println(" Nenhuma interface de Rede Local ativa foi encontrada.");
        }
    }
}