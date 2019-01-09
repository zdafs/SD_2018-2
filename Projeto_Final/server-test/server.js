const http = require('http')
const url = require('url')
const LinkedList = require('./LinkedList.js')

let provedores = new LinkedList()
let idCounter = 0

let server = http.createServer((req, res) => {
    res.writeHead(200, {'Content-Type': 'application/json'});
    const parsedUrl = url.parse(req.url, true)

    let response = {}
    response.msg = 'Operacao realizada  com sucesso.'

    if(parsedUrl.pathname=='/provedor'){
        const request = JSON.parse(parsedUrl.query.request)

        if(request.operation=='start'){
            const idProvedor = idCounter

            provedores.add({
                idProvedor: idProvedor,
                nome: request.nome,
                urlBase: request.urlBase+':'+(8000+idProvedor),
                recursos: []
            })

            idCounter++
            response.returnId = idProvedor
        }
        else if(request.operation=='update'){
            const idProvedor = request.idProvedor

            let [, provedor] = provedores.findByAttr('idProvedor', idProvedor)

            provedor.recursos = request.recursos

            console.log(provedor.recursos)
        }
        else if(request.operation=='end'){
            const idProvedor = request.idProvedor

            let [i, ] = provedores.findByAttr('idProvedor', idProvedor)

            provedores.remove(i)
        }
        else
            response.msg = 'Operacao nao suportada.'
    }
    else if(parsedUrl.pathname=='/cliente'){
        const request = JSON.parse(parsedUrl.query.request)

        if(request.operation=='request'){
            const recursos = request.recursos
            let responseIndex = -1
            let minPrice = -1
            let recIndexVec = []

            for(let i=0; i<provedores.getLength(); i++){
                let provedor = provedores.getByIndex(i)
                let auxPrice = 0
                let auxIndexVec = []
                let comparaPreco = true
                let disponivel = new Array(provedor.recursos.length).fill(true)
                for(let j=0; j<recursos.length; j++){
                    let reqRecurso = recursos[j]
                    let encontrouRec = false
                    for(let k=0; k<provedor.recursos.length; k++){
                        let recurso = provedor.recursos[k]
                        if(disponivel[k] && reqRecurso.cpu==recurso.cpu && reqRecurso.ram==recurso.ram && reqRecurso.hd==recurso.hd){
                            disponivel[k] = false
                            auxPrice+=recurso.preco
                            auxIndexVec.push(recurso._id)
                            encontrouRec = true
                            break
                        }
                    }
                    if(!encontrouRec){
                        comparaPreco = false
                        break
                    }
                }
                if(comparaPreco && (minPrice==-1 || minPrice > auxPrice)){
                    minPrice = auxPrice
                    responseIndex = i
                    recIndexVec = auxIndexVec
                }
            }

            if(responseIndex==-1)
                response.msg = 'Nao foram encontrados recursos que atendam a requisicao.'
            else{
                let provedor = provedores.getByIndex(responseIndex)
                response.nome = provedor.nome
                response.preco = minPrice
                response.urlBase = provedor.urlBase
                response.recursos = recIndexVec
            }
        }
        else
            response.msg = 'Operacao nao suportada.'
    }
    else
        response.msg = 'Operacao nao suportada.'

    const s = JSON.stringify(response)

    res.end(s)
})

server.listen(5000, 'localhost', () => {
    console.log('Servidor rodando em http://localhost:5000')
})
