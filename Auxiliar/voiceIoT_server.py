# Universidade Ferderal de Minas Gerais
# DCC604 - Projeto Orientado em Computacao

# Autor Alison de Oliveira Souza - 2012049316

# Servidor de interface entre VoicIoT e ManIoT.
# Para executar basta rodar python voiceIoT_server.py.

# NAO ESQUECA DE LIBERAR A PORTA UTILIZADA NO FIREWALL!

# python -m SimpleHTTPServer

import socket
import sys
import struct
import mysql.connector

def getHostPort():
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect(("google.com", 80))
	return s.getsockname()


if __name__ == '__main__':
	# Definicao da porta e endereco. Inicializacao do socket e contador global.
	PORT = 51515
	HOST = getHostPort()[0]
	contador = 0
	contador_global = 0
	tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	orig = (HOST, PORT)

	# Abertura passiva.
	tcp.bind(orig)
	tcp.listen(1)

	print 'START in', HOST, ':', PORT

	while True:
		contador = contador_global
		# Abertura completa, aguardando conexao de algum cliente.
		con, cliente = tcp.accept()
		
		# Definindo timeout de 5 segundos.
		#con.settimeout(5)
		print 'Conectado por', cliente
		
		# Recebendo mensagem (pode combinar codigos para as mensagens).
		data = con.recv(1024)
		
		print '\t',data,'.'

		# Divide a mensagem recebida em 3 partes.
		(user, password, msg) = data.split("#")
		
		# Podemos enviar uma mensagem de erro pro VoicIoT
		print '\n\tUser:', user, '.'
		print '\tPassword:', password, '.'
		print '\tMensagem recebida:', msg, '.'
		
		'''
		# Conexao com o BD ainda nao funciona...
		# Implementar requisicoes via REST na mao.

		msg = msg.lower();
		
		db = mysql.connector.connect(user=user, password=password, host='127.0.0.1', database='maniot')

		query = ("SELECT name FROM things WHERE name = %s")
		cursor = db.cursor()

		if(msg.find("lampada") > -1):
			cursor.execute(query, ("lampada"))

		'''
		
		con.send(msg)

		print '\nConexao encerrada com', cliente
		con.close()
		continue
