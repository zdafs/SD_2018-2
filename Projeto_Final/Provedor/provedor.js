const https = require('https')
const http = require('http')
const url = require('url')
const mongo = require('mongodb')
const mongoClient = mongo.MongoClient
const objectId = mongo.ObjectID

if(process.argv.length < 4 || process.argv.length > 7){
    console.log('Forneça apenas o nome do provedor ou o nome e as caracteristicas do recurso a ser adicionado.')
    return
}

if(process.argv.length != 4){
    const url = 'mongodb://127.0.0.1:27017/'

    mongoClient.connect(url, {useNewUrlParser: true}, function(err, db){
        if(err)
            throw err
        const dbo = db.db('cloud_broker')
        const obj = {
            providerName: process.argv[2],
            cpu: parseInt(process.argv[3]),
            ram: parseInt(process.argv[4]),
            hd: parseInt(process.argv[5]),
            status: "liberado",
            preco: parseInt(process.argv[6]),
        }
        dbo.collection('resources')
           .insertOne(obj, (err, res) => {
                if(err)
                    throw err
                console.log('Recurso adicionado com sucesso.')
                db.close()
           })
    })
    return
}

const basePort = 8000
const nome = process.argv[2]
const urlDestino = (process.argv[3]=='local'?'http://localhost:5000/provedor':'https://sd-cloudbroker.herokuapp.com/provedor')
let id

const recursosDisponiveisNoBD = (callback) => {
    const url = 'mongodb://127.0.0.1:27017/'

    mongoClient.connect(url, {useNewUrlParser: true}, function(err, db){
        if(err)
            throw err
        const dbo = db.db('cloud_broker')
        const query = {
            providerName: nome,
            status: "liberado"
        }
        dbo.collection('resources')
           .find(query)
           .toArray(function(err, result){
                if(err)
                    throw err
                db.close()
                console.log('Recursos sendo enviados:\n'+JSON.stringify(result))
                callback(result)
           })
    })
}

const trocaDisponibilidadeDosRecursos = (vecIdRec, statusDest, callbackFunction, id) => {
    const url = 'mongodb://127.0.0.1:27017/'

    mongoClient.connect(url, {useNewUrlParser: true}, function(err, db){
        let objIdVec = []
        vecIdRec.forEach(function(item){
            objIdVec.push(objectId(item))
        })
        console.log('Recursos requisitados:\n'+objIdVec.toString())
        console.log('Status destino: '+statusDest)
        if(err)
            throw err
        const dbo = db.db('cloud_broker')
        const query = {
            providerName: nome,
            _id: {$in: objIdVec}
        }
        const newValues = {
            $set: {status: statusDest}
        }
        dbo.collection('resources')
           .updateMany(query, newValues, function(err, res){
                if(err)
                    throw err
                console.log('Disponibilidade alterada com sucesso.')
                db.close()
                callbackFunction(id)
           })
    })
}

const atualizaCloudBroker = (id) => {
    const callbackFunction = (recursos) => {
        const params = {
            operation: 'update',
            idProvedor: id,
            recursos: recursos
        }

        const protocol = (urlDestino=='http://localhost:5000/provedor'?http:https)

        protocol.get(urlDestino+'?request='+JSON.stringify(params), (res) => {
            let data=''

            res.on('data', (chunck) => {
                data+=chunck
            })

            res.on('end', () => {
                console.log(JSON.parse(data).msg)
            })
        })
    }
    recursosDisponiveisNoBD(callbackFunction)
}

const iniciaProvedor = () => {
    const params = {
        operation: 'start',
        nome: nome,
        urlBase: 'http://localhost'
    }

    const protocol = (urlDestino=='http://localhost:5000/provedor'?http:https)

    protocol.get(urlDestino+'?request='+JSON.stringify(params), (res) => {
        let data = ''

        res.on('data', (chunck) => {
            data += chunck
        })

        res.on('end', () => {
            id = JSON.parse(data).returnId

            console.log(id)

            let server = http.createServer((req, res) => {
                res.writeHead(200, {'Content-Type': 'application/json'})
                const parsedUrl = url.parse(req.url, true)

                let response = {}
                response.msg = 'Operacao realizada com sucesso.'

                if(parsedUrl.pathname=='/cliente'){
                    const request = JSON.parse(parsedUrl.query.request)

                    if(request.operation=='get' || request.operation=='end'){
                        const recursos = request.recursos

                        trocaDisponibilidadeDosRecursos(recursos, (request.operation=='get'?'ocupado':'liberado'), atualizaCloudBroker, id)
                    }
                    else
                        response.msg = 'Operacao nao suportada.'
                }
                else
                    response.msg = 'Operacao nao suportada.'

                const s = JSON.stringify(response)

                res.end(s)
            }).listen(basePort+id, 'localhost', () => {
                console.log('Provedor rodando em http://localhost:'+(basePort+id))
                atualizaCloudBroker(id)
            })
        })
    })
}

process.on('SIGINT', () => {
    const protocol = (urlDestino=='http://localhost:5000/provedor'?http:https)

    const params = {
        operation: 'end',
        idProvedor: id
    }

    protocol.get(urlDestino+'?request='+JSON.stringify(params), (res) => {
        let data=''

        res.on('data', (chunck) => {
            data+=chunck
        })

        res.on('end', () => {
            console.log('\n'+JSON.parse(data).msg)
            process.exit();
        })
    })
})

iniciaProvedor()

