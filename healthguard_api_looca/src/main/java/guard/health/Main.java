package guard.health;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.RedeInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Looca looca = new Looca();
        Scanner leitorInicial = new Scanner(System.in);
        List<RedeInterface> interfaces = looca.getRede().getGrupoDeInterfaces().getInterfaces();

        System.out.println("Bem-vindo(a) a HealthGuard!");
        System.out.print("Dar inicio ao monitoramento de rede? (s/n): ");
        String respostaUsuario = leitorInicial.nextLine();

        if (respostaUsuario.equalsIgnoreCase("s")){
            System.out.println("\nIniciando monitoramento da rede...\n");

            long brAntesCaptura = 0;
            long beAntesCaptura = 0;

            for (int i = 0; i < interfaces.size(); i++) {
                RedeInterface byteAnterior = interfaces.get(i);
                brAntesCaptura += byteAnterior.getBytesRecebidos();
                beAntesCaptura += byteAnterior.getBytesEnviados();
            }

            while (true){
                long brNaCaptura = 0;
                long beNaCaptura = 0;

                List<RedeInterface> bytesRedes = looca.getRede().getGrupoDeInterfaces().getInterfaces();
                for (int i = 0; i < bytesRedes.size(); i++) {
                    RedeInterface byteAgora = interfaces.get(i);
                    brNaCaptura += byteAgora.getBytesRecebidos();
                    beNaCaptura += byteAgora.getBytesEnviados();
                }

                long diferencaBytesRecebidos = brNaCaptura - brAntesCaptura;
                long diferencaBytesEnviados = beNaCaptura - beAntesCaptura;

                Double dbr_Megabytes = diferencaBytesEnviados / (1024.0 * 1024.0);
                Double dbe_Megabytes = diferencaBytesRecebidos / (1024.0 * 1024.0);

                LocalDateTime dtHoraCaptura = LocalDateTime.now();
                DateTimeFormatter formatacao_dtHoraCaptura = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

                System.out.printf("[%s] Últimos 5s - Recebidos: %.2f MB | Enviados: %.2f MB%n",
                        dtHoraCaptura.format(formatacao_dtHoraCaptura),
                        dbr_Megabytes,
                        dbe_Megabytes);

                brAntesCaptura = brNaCaptura;
                beAntesCaptura = beNaCaptura;

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (respostaUsuario.equalsIgnoreCase("n")) {
            System.out.println("Bye");
        } else{
            System.out.println("Comando não encontrado. Encerrando programa.");
        }
    }
}