package br.poli.ecomp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RequisicaoHttp implements Runnable {

	// texto em caso de erro 404, pagina n encontrada.
	static String html404 = "<!doctype html>\n<html>\n<body>\n<h1>ERRO 404 : Pagina nao encontrada</h1>\n</body>\n</html>";

	// caminho do arquivo requisitado
	static Path caminho;
	Socket socket;
	private String html;
	private String status;
	private String host;

	public void setIs(InputStream is) {
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public RequisicaoHttp(Socket socket) {
		this.socket = socket;
	}

	public void setHost(String host) {
		this.host = host;
	}

	private void processaRequisicao() throws IOException {

		String html404 = "<!doctype html>\n<html>\n<body>\n<h1>ERRO 404 : Pagina nao encontrada</h1>\n</body>\n</html>";

		// recebe o pedido
		setIs(socket.getInputStream());

		// saida d cliente
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// criação do buffer
		BufferedReader br;
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// linha q contem as informações de requisição
		String linhaReq = br.readLine();
		String[] dados = linhaReq.split(" ");

		// caminho do arquivo
		String caminhoArq = dados[1];

		// protocolo
		String protocolo = dados[2];

		System.out.println("Protocolo: ".concat(protocolo));
		System.out.println("Requisição vindo do IP: " + socket.getInetAddress());
		System.out.println("Hostname: " + socket.getInetAddress().getHostName());

		if (caminhoArq.equals("/")) {
			caminhoArq = "\\index.html";
		}

		// nome do arquivo requisitado
		String arq = caminhoArq.substring(1);

//		pasta onde o arq do proj está sendo executado
		Path path = Paths.get(System.getProperty("user.dir"));

//		retorna se o aqr requisitado foi encontrado
		boolean flag = procurarArquivo(path, arq);

		setHtml("");
		setStatus("");

//		flag para sinalizar se o arquivo foi encontrado
		if (flag) {
			status = protocolo + (HttpURLConnection.HTTP_OK);
			String linha = null;

			try (BufferedReader r = Files.newBufferedReader(caminho)) {
				while ((linha = r.readLine()) != null) {
					html += linha;
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error: ".concat(e.toString()));
			}
		} else {
			status = protocolo + (HttpURLConnection.HTTP_NOT_FOUND);
			setHtml(html404);
		}

		// Cria formato de data padrao http
		SimpleDateFormat formatador = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss", Locale.ENGLISH);
		formatador.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date data = new Date();

		// Formata a data para o padrao
		String dataFormatada = formatador.format(data) + " GMT";

		// Cabecalho padrao da resposta HTTP
		String header = status + "Location: http://" + host + "\r\n" + "Date: " + dataFormatada + "\r\n"
				+ "Server: ServidorWeb/1.0\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + html.length()
				+ "\r\n" + "Connection: close\r\n" + "\r\n";

		os.writeBytes(header);
		os.writeBytes(html);
		os.flush();
		os.close();
	}

	/**
	 * Encontrar arquivo no projeto
	 * 
	 * @param path
	 * @param arquivo
	 * @return
	 */
	public static boolean procurarArquivo(Path path, String arquivo) {
		if (Files.isRegularFile(path)) {

			if (path.toAbsolutePath().endsWith(arquivo)) {
				caminho = path;
				return true;
			}
		} else {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				for (Path p : stream) {
					boolean encontrou = procurarArquivo(p, arquivo);
					if (encontrou) {
						return true;
					}
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		return false;
	}

	@Override
	public void run() {
		try {
			// Inicia a funcao na qual recebe o pedido e devolve ao cliente
			processaRequisicao();
		} catch (Exception e) {
			System.err.println("ERROR RUN: " + e.toString());
			return;
		}
	}
}
