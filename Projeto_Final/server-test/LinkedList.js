class LinkedList {
    constructor(){
        this.head = null
        this.tail = null
        this.length = 0
    }

    add(value){
        if(typeof(value)!='object')
            return this
        const node = {value: value, next: null}
        if(this.head==null){
            this.head = node
            this.tail = node
        }
        else{
            this.tail.next = node
            this.tail = this.tail.next
        }
        this.length++
        return this
    }

    remove(index){
        if(this.length==0 || index>this.length-1 || index.length<0)
            return this
        if(index==0){
            this.head = this.head.next
            if(this.head==null)
                this.tail = null
        }
        else{
            let node = this.head
            let i = 0
            while(i<index-1){
                i++
                node = node.next
            }
            node.next = node.next.next
            if(index==this.length-1)
                this.tail = node
        }
        this.length--
        return this
    }

    findByAttr(attrName, value){
        for(let i=0, node = this.head; i<this.length; i++, node = node.next){
            if(typeof(node['value'][attrName])!='undefined' && node['value'][attrName]===value)
                return [i, node['value']]
        }

        return [-1, null]
    }

    getByIndex(index){
        let node = this.head
        let i = 0

        while(i<index){
            node = node.next
            index++
        }

        return node['value']
    }

    getLength(){
        return this.length
    }
}

module.exports = LinkedList
