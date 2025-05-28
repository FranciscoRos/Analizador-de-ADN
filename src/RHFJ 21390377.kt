package randoms
//Programa por Francisco Rosales Huey
sealed class ArbolBinario {
    object Vacio : ArbolBinario()

    data class Nodo(
        val valor: Int,
        val izquierdo: ArbolBinario = Vacio,
        val derecho: ArbolBinario = Vacio
    ) : ArbolBinario()

    fun insertarNumero(valor: Int): ArbolBinario = when(this) {
        is Vacio -> Nodo(valor)
        is Nodo -> when {
            valor <= this.valor -> copy(izquierdo = izquierdo.insertarNumero(valor))
            else -> copy(derecho = derecho.insertarNumero(valor))
        }
    }

    fun buscar(valor: Int): Boolean = when(this) {
        is Vacio -> false
        is Nodo -> when {
            valor == this.valor -> true
            valor < this.valor -> izquierdo.buscar(valor)
            else -> derecho.buscar(valor)
        }


    }

    fun imprimirEnOrden(): String = when(this) {
        is Vacio -> ""
        is Nodo -> izquierdo.imprimirEnOrden() + "$valor " + derecho.imprimirEnOrden()
    }

}

fun main() {
    var arbol: ArbolBinario = ArbolBinario.Vacio

    arbol = arbol.insertarNumero(4)
    arbol = arbol.insertarNumero(2)
    arbol = arbol.insertarNumero(6)
    arbol = arbol.insertarNumero(3)
    arbol = arbol.insertarNumero(1)
    arbol = arbol.insertarNumero(5)
    arbol = arbol.insertarNumero(7)

println("¿Está 5 en el árbol?: ${arbol.buscar(5)}" )
println("¿Está 8 en el árbol? ${arbol.buscar(8)}" )
println( arbol.imprimirEnOrden() )
}