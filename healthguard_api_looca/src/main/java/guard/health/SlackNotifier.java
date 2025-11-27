package guard.health;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SlackNotifier {

    private String webhookUrl;

    public SlackNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void enviarMensagem(String mensagem) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

            conexao.setRequestMethod("POST");
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json");

            
            String json = "{ \"text\": \"" + mensagem.replace("\"", "'") + "\" }";

            try (OutputStream os = conexao.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int resposta = conexao.getResponseCode();
            System.out.println("Slack retornou HTTP " + resposta);

        } catch (Exception e) {
            System.out.println("Erro ao enviar notificação para o Slack:");
            e.printStackTrace();
        }
    }
}
