from __future__ import print_function
import requests
import json
import sys

def menu_opcoes():
    print('---------------MENU DE OPCOES---------------')
    print('- 0 para sair do programa')
    print('- 1 para encontrar a melhor oferta')
    print('- 2 para liberar o recurso sendo utilizado')
    print('--------------------------------------------\n')
    print('Opcao:', end=' ')


menu_opcoes()
op = int(raw_input())

recursos_atuais = []
url_atual = ''
if sys.argv[1]=='local':
    urlDestino = 'http://localhost:5000/cliente'
else:
    urlDestino = 'https://sd-cloudbroker.herokuapp.com/cliente'

while True:
    if op==1:
        if url_atual!='':
            print('Voce ja esta utilizando o recurso de algum provedor.\n')
        else:
            recursos = []
            while True:
                print('Quantidade de vCPUs na maquina:', end=' ')
                cpu = int(raw_input())
                print('Quantidade de RAM (em GB) na maquina:', end=' ')
                ram = int(raw_input())
                print('Quantidade de disco (em GB) na maquina:', end=' ')
                hd = int(raw_input())
                recurso = { "cpu": cpu, "ram": ram, "hd": hd }
                recursos.append(recurso)
                print('Deseja adicionar outro recurso [S/N]?:', end=' ')
                op = raw_input()
                if(op=='N' or op=='n'):
                    break
            req_json = {
                "operation": "request",
                "recursos": recursos
            }

            payload = {"request": json.dumps(req_json)}

            r = requests.get(urlDestino, params=payload)

            response = r.json()

            if response['msg']=='Nao foram encontrados recursos que atendam a requisicao.':
                print(response['msg']+'\n')
            else:
                print('O provedor '+ response['nome'] +' oferece os recursos por R$'+ str(response['preco']) +'/hora. Deseja utilizar [S/N]?', end=' ')
                op = raw_input()
                if op=='S' or op=='s':
                    recursos_atuais = response['recursos']
                    req_json = {
                        "operation": "get",
                        "recursos": recursos_atuais
                    }
                    payload = {"request": json.dumps(req_json)}
                    url_atual = response['urlBase']+'/cliente'
                    print(url_atual)
                    r = requests.get(url_atual, params=payload)
                    print(r.json()['msg']+'\n')
                else:
                    recursos_atuais = []
                    url_atual = ''
                    print('Operacao cancelada.\n')

    elif op==2:
        if url_atual=='':
            print('Voce nao esta utilizando nenhum recurso.\n')
        else:
            req_json = {
                "operation": "end",
                "recursos": recursos_atuais
            }
            payload = {"request": json.dumps(req_json)}
            r = requests.get(url_atual, params=payload)
            recursos_atuais = []
            url_atual = ''
            print(r.json()['msg']+'\n')

    elif op==0:
        if url_atual!='':
            req_json = {
                "operation": "end",
                "recursos": recursos_atuais
            }
            payload = {"request": json.dumps(req_json)}
            r = requests.get(url_atual, params=payload)
            recursos_atuais = []
            url_atual = ''
            print(r.json()['msg']+'\n')
        break

    else:
        print('Operacao nao suportada\n')

    menu_opcoes()
    op = int(raw_input())
