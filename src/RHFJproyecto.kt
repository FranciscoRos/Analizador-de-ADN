package randoms



import java.io.File
import java.nio.file.Path


data class ArchivoFasta(
    val nombreArchivo: String,
    val secuenciaADN: String,
    val contienePlasmidos: Boolean
)

// Función que recibe la ruta a un archivo y devuelve un objeto ArchivoFasta ya validado
fun leerArchivoFasta(ruta: Path): ArchivoFasta {
    //sacamos las lineas del archivo
    val uriDelArchivo = ruta.toUri()
    val objetoArchivo = File(uriDelArchivo)
    val lineas: List<String> = objetoArchivo.readLines()

    //Separamos en titulos y contenidos, ambos son List<String>
    val partesSeparadas = filtrarPartes(lineas)
    val cabeceras = partesSeparadas.first
    val lineasSecuencia = partesSeparadas.second

    //Se convierte en una sola linea y se borran caracteres erroneos
    val secuenciaConcatenada = lineasSecuencia
        .joinToString(separator = "")
        .uppercase()
        .filter { it in listOf('A', 'T', 'C', 'G') }


    val contienePlasmidos = cabeceras.size > 1
    return ArchivoFasta(ruta.toString(), secuenciaConcatenada, contienePlasmidos)
}


fun filtrarPartes( cuerpoArchivo: List<String> ): Pair<List<String>,List<String>> {
    val cabeceras = mutableListOf<String>()
    val lineasSecuencia = mutableListOf<String>()
    for (linea in cuerpoArchivo){
        if (linea.startsWith(">")){
            cabeceras.add(linea)
        }else{
            lineasSecuencia.add(linea)
        }
    }

    return Pair(cabeceras,lineasSecuencia)
}


// Función que genera todas las subcadenas
fun generarKPalabras(secuencia: String, k: Int): List<String> {
    return if (secuencia.length < k) {
        emptyList()
    } else {
        (0..(secuencia.length - k) ).map { indice ->
            secuencia.substring(indice, (indice + k) )
        }
    }
}



fun contarFrecuencias(listaPalabras: List<String>): Map<String, Int> =
    listaPalabras.groupingBy { it }.eachCount()

fun ordenarFrecuencias(frecuencias: Map<String, Int>): List<Pair<String, Int>> =
    frecuencias.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .map { it.toPair() }


fun fusionarFrecuencias(frecuenciasPorArchivo: Map<String, Map<String, Int>>): Map<String, Int> {
    return frecuenciasPorArchivo.values //quitamos nombres, solo trabajamos con Collection<Map<String, Int>> como [ {A→3, B→5}, {A→1, C→4}, {B→2, C→1} ]
        .flatMap { it.entries }         //forzamos a todas las listas a ser una Map.Entry<String, Int> [Entry(A,3), Entry(B,5), Entry(A,1), Entry(C,4), Entry(B,2), Entry(C,1)]
        .groupBy { it.key }             //se agrupa por clave Map<String,List <String,Int> > { "A"→[Entry(A,3), Entry(A,1)], "B"→[Entry(B,5), Entry(B,2)], "C"→[Entry(C,4), Entry(C,1)] }
        .mapValues { par ->
            // par.key es la clave "A"
            val grupo = par.value
            // par.value es la lista [Entry(A,3), Entry(A,1)]
            grupo.sumOf { it.value }
        }
}



fun imprimirTabla(titulo: String, filas: List<Pair<String, Int>>) {
    println("\n== $titulo ==")
    println("Palabra   Frecuencia")
    println("-------------------------")
    filas.forEach { (palabra, frecuencia) ->
        println(("$palabra   $frecuencia"))
    }
}

fun main() {

    print("Introduce rutas de archivos FASTA/FNA (5 o más) separadas por espacio: ")
    //rutasArchivos es una lista de Strings List<String>
    val rutasArchivos = readln().trim().split(" ").filter { it.isNotBlank() } // Separa por espacios y filtra vacíos

    if( rutasArchivos.size < 5){
        println("Deben ser al menos 5 archivos.")
        return
    }

    //Lista de ojbetos archivoFasta List<ArchivoFasta>
    val archivosFasta: List<ArchivoFasta> = rutasArchivos.map { leerArchivoFasta(Path.of(it)) }

    // Informar si algún archivo contiene plásmidos
    val contienenPlasmidos = archivosFasta.filter { it.contienePlasmidos }
    contienenPlasmidos.forEach { println("[Notificación:] El archivo ${it.nombreArchivo} contiene plásmidos, ya se unieron para su evaluación integral.") }

    // Pedir tamaño de la palabray validarlo
    var tamañoPalabra: Int
    while (true) {
        print("Introduce el tamaño de la palabra a buscar (6–17): ")
        tamañoPalabra = readln().toIntOrNull() ?: -1                    // Convierte entrada a entero o -1 si es inválido
        if (tamañoPalabra >5  && tamañoPalabra < 17 ){
            break
        }
        else println("Valor inválido. Intenta de nuevo.")
    }

    //Verificamos si es ARN


    // Calcular frecuencias de k‑palabras por archivo
    val frecuenciasPorArchivo: Map<String, Map<String, Int>> = archivosFasta.associate { archivo ->
        archivo.nombreArchivo to contarFrecuencias(  generarKPalabras(archivo.secuenciaADN, tamañoPalabra)   )
    }

    // Generar e imprimir tabla de frecuencias globales
    val frecuenciaGlobal = fusionarFrecuencias(frecuenciasPorArchivo)
    imprimirTabla("Frecuencias globales", ordenarFrecuencias(frecuenciaGlobal))

    //Imprimir frecuencias individuales por archivo
    frecuenciasPorArchivo.forEach { (nombre, frecuencia) ->
        imprimirTabla("Archivo: $nombre", ordenarFrecuencias(frecuencia))
    }

    //Búsqueda interactiva de palabras
    println("\nPuedes buscar palabras de longitud $tamañoPalabra. Teclea 'salir' para terminar.")
    while (true) {
        print("buscar> ")
        val consulta = readln().uppercase()
        if (consulta == "SALIR") break
        if (consulta.length != tamañoPalabra) {
            println("[Error] La palabra debe tener longitud $tamañoPalabra.")
            continue
        }
        val frecuenciaTotal = frecuenciaGlobal[consulta] ?: 0
        if (frecuenciaTotal == 0) {
            println("No se encontró la palabra $consulta en ningún archivo.")
        } else {
            val archivosCoincidentes = frecuenciasPorArchivo.filter { (_, mapa) -> consulta in mapa } // Archivos donde aparece
            println("La palabra $consulta aparece $frecuenciaTotal veces en ${archivosCoincidentes.size} archivos:")
            archivosCoincidentes.forEach { (nombre, mapa) -> println(" - $nombre: ${mapa[consulta]} veces") }
        }
    }

    println("\nPrograma finalizado. ¡Hasta luego!")
}
