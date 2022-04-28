package br.poli.ecomp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorWeb {

	private static ServerSocket servidor;
	private static Integer porta;

	public static void main(String[] args) throws IOException {
		
		System.out.println("Iniciando Servidor Web...");
		
		//endere�o atual do servidor
		System.out.println("IP do ServidorWeb: ".concat(InetAddress.getLocalHost().getHostAddress()));
		
		System.out.println("Servidor Web carregado.");
		
		// declara��o da porta
		porta = 80;
		
		//iniciando a vari�vel servidor do tipo socket
		servidor = new ServerSocket(porta, 50);
		while (true) {

			// aceitando pedido do servdor
			Socket socket = servidor.accept();

			// iniciando uma nova requisi��o
			RequisicaoHttp requisicao = new RequisicaoHttp(socket);

			// a cada nova requisi��o, 1 nova thread
			Thread thread = new Thread(requisicao);
			thread.start();
		}
	}
}